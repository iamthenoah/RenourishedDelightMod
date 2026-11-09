package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.DietRestrictions;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    // 1.20.1 also has a private static overload, so target the public one explicitly
    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At("TAIL"))
    private void renourisheddelight$updateRegistryTags(CallbackInfo callback) {
        DietRestrictions.server().onUpdateTags();
    }
}
