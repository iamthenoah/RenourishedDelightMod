package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.network.SuppressHurtFlashPayload;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "hurtTo", at = @At("TAIL"))
    private void hurtTo(float health, CallbackInfo callback) {
        if (SuppressHurtFlashPayload.isSuppressed()) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            player.hurtTime = 0;
            player.hurtDuration = 0;
        }
    }
}
