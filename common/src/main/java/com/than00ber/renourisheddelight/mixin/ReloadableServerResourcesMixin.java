package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.FoodCategories;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {

    @Inject(method = "updateRegistryTags*", at = @At("TAIL"))
    private void renourisheddelight$updateRegistryTags(CallbackInfo callback) {
        FoodCategories.server().onUpdateTags();
    }
}
