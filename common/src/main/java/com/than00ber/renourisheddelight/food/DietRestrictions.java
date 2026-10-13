package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.stream.Collectors;

public final class DietRestrictions {

    private static final String TAG_NAMESPACE = "c";
    private static final String TAG_PREFIX = "foods/";
    private static final Set<String> IGNORED_TAGS = Set.of("foods/edible_when_placed", "foods/food_poisoning", "foods/golden");
    private static final TagKey<Item> EDIBLE_WHEN_PLACED = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(TAG_NAMESPACE, "foods/edible_when_placed"));
    private static final int MAX_DEPTH = 8;

    private static final DietRestrictions SERVER = new DietRestrictions();
    private static final DietRestrictions CLIENT = new DietRestrictions();

    private volatile Map<Item, Set<ResourceLocation>> restrictions = Map.of();
    private volatile boolean dirty = true;

    public static DietRestrictions server() {
        return SERVER;
    }

    public static DietRestrictions client() {
        return CLIENT;
    }

    public static DietRestrictions of(Level level) {
        return level.isClientSide() ? CLIENT : SERVER;
    }

    public Set<ResourceLocation> get(Item item) {
        return restrictions.getOrDefault(item, Set.of());
    }

    public Map<Item, Set<ResourceLocation>> all() {
        return restrictions;
    }

    public void set(Map<Item, Set<ResourceLocation>> restrictions) {
        this.restrictions = Map.copyOf(restrictions);
        this.dirty = false;
    }

    public void onUpdateTags() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public static MutableComponent name(ResourceLocation restriction) {
        String path = restriction.getPath();
        String last = path.substring(path.lastIndexOf('/') + 1);
        String fallback = Arrays.stream(last.split("_"))
                .filter(x -> !x.isEmpty())
                .map(x -> Character.toUpperCase(x.charAt(0)) + x.substring(1))
                .collect(Collectors.joining(" "));
        return Component.translatableWithFallback("tag.item." + restriction.getNamespace() + "." + path.replace('/', '.'), fallback);
    }

    public void rebuild(MinecraftServer server) {
        long startNanos = System.nanoTime();
        set(new Resolver(server).resolveAll());
        RenourishedDelightMod.LOGGER.info("Resolved diet restrictions for {} foods in {} ms", restrictions.size(), (System.nanoTime() - startNanos) / 1_000_000L);
    }

    private static boolean isRestrictionTag(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        return id.getNamespace().equals(TAG_NAMESPACE) && id.getPath().startsWith(TAG_PREFIX) && !IGNORED_TAGS.contains(id.getPath());
    }

    private static final class Resolver {

        private final Map<TagKey<Item>, Set<Item>> members = new HashMap<>();
        private final Map<Item, Set<TagKey<Item>>> own = new HashMap<>();
        private final Map<Item, List<List<Set<Item>>>> producers = new HashMap<>();
        private final Map<Item, Set<TagKey<Item>>> resolved = new HashMap<>();
        // recipe cycles (e.g. storage blocks) stop at the item's own tags
        private final Set<Item> visiting = new HashSet<>();

        private Resolver(MinecraftServer server) {
            BuiltInRegistries.ITEM.getTagNames().filter(DietRestrictions::isRestrictionTag).forEach(tag -> {
                Set<Item> items = BuiltInRegistries.ITEM.getTag(tag).stream()
                        .flatMap(x -> x.stream().map(Holder::value))
                        .filter(x -> x != Items.AIR)
                        .collect(Collectors.toSet());

                if (!items.isEmpty()) {
                    members.put(tag, items);
                    items.forEach(item -> own.computeIfAbsent(item, x -> new HashSet<>()).add(tag));
                }
            });
            own.replaceAll((item, tags) -> mostSpecific(tags));
            HolderLookup.Provider registries = server.registryAccess();

            for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
                Recipe<?> recipe = holder.value();
                if (recipe.isSpecial()) continue;
                ItemStack result;

                try {
                    result = recipe.getResultItem(registries);
                } catch (Exception exception) {
                    continue;
                }
                if (result.isEmpty()) continue;
                List<Set<Item>> inputs = new ArrayList<>();

                for (Ingredient ingredient : recipe.getIngredients()) {
                    if (ingredient.isEmpty()) continue;
                    Set<Item> options = Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).filter(x -> x != Items.AIR).collect(Collectors.toSet());
                    if (!options.isEmpty() && !options.contains(result.getItem())) inputs.add(options);
                }
                if (!inputs.isEmpty()) {
                    producers.computeIfAbsent(result.getItem(), x -> new ArrayList<>()).add(inputs);
                }
            }
        }

        private Map<Item, Set<ResourceLocation>> resolveAll() {
            Map<Item, Set<ResourceLocation>> restrictions = new HashMap<>();

            for (Item item : BuiltInRegistries.ITEM) {
                if (!item.components().has(DataComponents.FOOD) && !item.builtInRegistryHolder().is(EDIBLE_WHEN_PLACED)) continue;
                Set<TagKey<Item>> tags = resolve(item, 0);

                if (!tags.isEmpty()) {
                    restrictions.put(item, tags.stream().map(TagKey::location).collect(Collectors.toUnmodifiableSet()));
                }
            }
            return restrictions;
        }

        private Set<TagKey<Item>> resolve(Item item, int depth) {
            Set<TagKey<Item>> cached = resolved.get(item);
            if (cached != null) return cached;
            Set<TagKey<Item>> tags = new HashSet<>(own.getOrDefault(item, Set.of()));
            if (depth >= MAX_DEPTH || !visiting.add(item)) return tags;

            for (List<Set<Item>> recipe : producers.getOrDefault(item, List.of())) {
                for (Set<Item> options : recipe) {
                    tags.addAll(resolveIngredient(options, depth + 1));
                }
            }
            visiting.remove(item);
            Set<TagKey<Item>> result = mostSpecific(tags);
            resolved.put(item, result);
            return result;
        }

        // an ingredient accepting several items (usually a tag) only counts the restrictions they all share,
        // falling back to the narrowest restriction tag covering every option ("any raw meat")
        private Set<TagKey<Item>> resolveIngredient(Set<Item> options, int depth) {
            Set<TagKey<Item>> shared = null;

            for (Item option : options) {
                Set<TagKey<Item>> tags = resolve(option, depth);
                if (shared == null) shared = new HashSet<>(tags);
                else shared.retainAll(tags);
                if (shared.isEmpty()) break;
            }
            if (shared != null && !shared.isEmpty()) return shared;

            Set<TagKey<Item>> covering = members.entrySet().stream()
                    .filter(x -> x.getValue().containsAll(options))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            return mostSpecific(covering);
        }

        // drops tags that strictly contain another of the given tags, so a steak counts as
        // "cooked beef" rather than also "cooked meat" when a mod provides the finer tag
        private Set<TagKey<Item>> mostSpecific(Set<TagKey<Item>> tags) {
            if (tags.size() < 2) return tags;
            Set<TagKey<Item>> result = new HashSet<>(tags);

            for (TagKey<Item> tag : tags) {
                Set<Item> items = members.getOrDefault(tag, Set.of());

                for (TagKey<Item> other : tags) {
                    Set<Item> narrower = members.getOrDefault(other, Set.of());

                    if (other != tag && narrower.size() < items.size() && items.containsAll(narrower)) {
                        result.remove(tag);
                        break;
                    }
                }
            }
            return result;
        }
    }
}
