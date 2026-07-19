package com.than00ber.renourisheddelight.fabric.mixin;

import com.than00ber.renourisheddelight.config.datapack.FoodPresetsPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;

@Mixin(ServerPacksSource.class)
public abstract class ServerPacksSourceMixin {

    @Redirect(method = "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;", at = @At(value = "NEW", target = "net/minecraft/server/packs/repository/PackRepository"))
    private static PackRepository renourisheddelight$createPackRepository(RepositorySource[] sources) {
        RepositorySource[] extended = Arrays.copyOf(sources, sources.length + 1);
        extended[sources.length] = new FoodPresetsPackSource();
        return new PackRepository(extended);
    }
}
