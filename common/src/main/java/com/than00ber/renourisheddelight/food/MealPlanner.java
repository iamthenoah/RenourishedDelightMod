package com.than00ber.renourisheddelight.food;

import com.than00ber.renourisheddelight.config.data.FoodConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class MealPlanner {

    // bounds the search on huge modpacks, the best set found so far is used past it
    private static final int MAX_NODES = 200_000;

    public record Food(Item item, double health, int duration) {
    }

    public record Meal(List<Food> foods) {

        public double health() {
            return foods.stream().mapToDouble(Food::health).sum();
        }
    }

    public static Meal plan(Diet diet, FoodConfig config, DietRestrictions restrictions) {
        int slots = Math.max(1, diet.getCapacity());
        List<Food> candidates = candidates(diet, config);
        Search search = new Search(candidates, slots, diet.isRestricted() ? restrictions : null);
        search.run(0, new ArrayList<>(), 0.0, 0L);
        return new Meal(search.best);
    }

    private static List<Food> candidates(Diet diet, FoodConfig config) {
        List<Food> foods = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            if (!item.isEdible() && config.entry(item) == null) continue;
            ConsumableFoodInstance instance = ConsumableFoodInstance.create(item, config, diet.nutritionDecay(item));
            double health = 0.0;
            int duration = 0;

            for (AttributeModifierInstance bonus : instance.attributes()) {
                if (bonus.attribute() == Attributes.MAX_HEALTH && bonus.modifier().getOperation() == AttributeModifier.Operation.ADDITION) {
                    health += bonus.modifier().getAmount();
                    duration = Math.max(duration, bonus.duration());
                }
            }
            if (health > 0.0) foods.add(new Food(item, health, duration));
        }
        foods.sort(Comparator.comparingDouble(Food::health).thenComparingInt(Food::duration).reversed());
        return foods;
    }

    private static final class Search {

        private final List<Food> candidates;
        private final int slots;
        private final DietRestrictions restrictions;
        private final int[] longestFrom;
        private List<Food> best = List.of();
        private double bestHealth = -1.0;
        private long bestDuration = -1L;
        private int nodes;

        private Search(List<Food> candidates, int slots, DietRestrictions restrictions) {
            this.candidates = candidates;
            this.slots = slots;
            this.restrictions = restrictions;
            this.longestFrom = new int[candidates.size() + 1];

            for (int i = candidates.size() - 1; i >= 0; i--) {
                longestFrom[i] = Math.max(longestFrom[i + 1], candidates.get(i).duration());
            }
        }

        // candidates are sorted by health, so the next free slots filled from index onward bound what is left
        private void run(int index, List<Food> picked, double health, long duration) {
            if (better(health, duration)) {
                best = List.copyOf(picked);
                bestHealth = health;
                bestDuration = duration;
            }
            int remaining = slots - picked.size();
            if (remaining == 0 || index >= candidates.size() || ++nodes > MAX_NODES) return;

            double healthBound = health;
            for (int i = index; i < Math.min(candidates.size(), index + remaining); i++) healthBound += candidates.get(i).health();
            long durationBound = duration + (long) remaining * longestFrom[index];
            if (healthBound < bestHealth || (healthBound == bestHealth && durationBound <= bestDuration)) return;

            for (int i = index; i < candidates.size(); i++) {
                Food food = candidates.get(i);
                if (!fits(picked, food)) continue;
                picked.add(food);
                run(i + 1, picked, health + food.health(), duration + food.duration());
                picked.remove(picked.size() - 1);
                if (nodes > MAX_NODES) return;
            }
        }

        private boolean better(double health, long duration) {
            return health > bestHealth || (health == bestHealth && duration > bestDuration);
        }

        private boolean fits(List<Food> picked, Food food) {
            if (restrictions == null) return true;
            Set<ResourceLocation> tags = restrictions.get(food.item());
            if (tags.isEmpty()) return true;
            return picked.stream().noneMatch(x -> restrictions.get(x.item()).stream().anyMatch(tags::contains));
        }
    }
}
