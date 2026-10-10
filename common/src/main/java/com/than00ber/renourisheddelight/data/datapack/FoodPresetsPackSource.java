package com.than00ber.renourisheddelight.data.datapack;

import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.data.FoodConfigReloadListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class FoodPresetsPackSource implements RepositorySource {

    @Override
    public void loadPacks(Consumer<Pack> consumer) {
        Path root = getPackRoot();

        if (root != null) {
            String location = "mod/" + FoodConfigReloadListener.PRESETS_KEY;
            Component name = Component.translatable("pack." + RenourishedDelightMod.MOD_ID + ".foodpresets");
            Pack.ResourcesSupplier supplier = getResourcesSupplier(location, root);
            Pack pack = Pack.readMetaAndCreate(location, name, false, supplier, PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);

            if (pack != null) {
                consumer.accept(pack);
            }
        }
    }

    private Pack.ResourcesSupplier getResourcesSupplier(String location, Path root) {
        return id -> new PathPackResources(location, root, true);
    }

    private Path getPackRoot() {
        URL url = RenourishedDelightMod.class.getResource("/datapacks/foodpresets/pack.mcmeta");

        try {
            return url != null ? Path.of(url.toURI()).getParent() : null;
        } catch (Exception exception) {
            RenourishedDelightMod.LOGGER.warn("Failed to resolve built-in pack from URL {}", url, exception);
            return null;
        }
    }
}
