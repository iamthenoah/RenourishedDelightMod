package com.than00ber.renourisheddelight.config.datapack;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class FoodPresetsPackSource implements RepositorySource {

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        Path root = resolvePackRoot();

        if (root != null) {
            String location = "mod/" + RenourishedDelightMod.MOD_ID + ":presets";
            Component name = Component.translatable("pack." + RenourishedDelightMod.MOD_ID + ".presets");
            PackLocationInfo info = new PackLocationInfo(location, name, PackSource.BUILT_IN, Optional.empty());
            Pack.ResourcesSupplier supplier = getResourcesSupplier(info, root);
            PackSelectionConfig packet = new PackSelectionConfig(true, Pack.Position.TOP, false);
            Pack pack = Pack.readMetaAndCreate(info, supplier, PackType.SERVER_DATA, packet);

            if (pack != null) {
                consumer.accept(pack);
            }
        }
    }

    private Pack.ResourcesSupplier getResourcesSupplier(PackLocationInfo info, Path root) {
        PackResources resources = new PathPackResources(info, root);
        return new Pack.ResourcesSupplier() {
            @Override
            public @NotNull PackResources openPrimary(PackLocationInfo location) {
                return resources;
            }

            @Override
            public @NotNull PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return resources;
            }
        };
    }

    private Path resolvePackRoot() {
        URL url = RenourishedDelightMod.class.getResource("/datapacks/food_presets/pack.mcmeta");

        try {
            return url != null ? Path.of(url.toURI()).getParent() : null;
        } catch (Exception exception) {
            RenourishedDelightMod.LOGGER.warn("Failed to resolve built-in pack from URL {}", url, exception);
            return null;
        }
    }
}
