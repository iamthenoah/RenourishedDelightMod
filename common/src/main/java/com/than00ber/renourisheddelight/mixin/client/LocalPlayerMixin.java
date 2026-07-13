package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.network.MaxHealthShrinkAware;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "hurtTo", at = @At("TAIL"))
    private void clearMaxHealthShrinkTilt(float health, CallbackInfo callback) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (((MaxHealthShrinkAware) player).consumeRecentMaxHealthShrink()) {
            player.hurtTime = 0;
            player.hurtDuration = 0;
        }
    }
}
