package com.than00ber.renourisheddelight.mixin.client;

import com.than00ber.renourisheddelight.network.MaxHealthShrinkAware;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAttributeMixin implements MaxHealthShrinkAware {

    @Unique private static final long SHRINK_WINDOW_MILLIS = 500L;

    @Unique private double previousMaxHealth = -1;
    @Unique private long maxHealthShrinkTimestamp = -1L;

    @Inject(method = "onAttributeUpdated", at = @At("HEAD"))
    private void onAttributeUpdated(Holder<Attribute> attribute, CallbackInfo callback) {
        if ((Object) this instanceof LocalPlayer player) {
            if (attribute.is(Attributes.MAX_HEALTH)) {
                double current = player.getAttributeValue(attribute);

                if (previousMaxHealth >= 0 && current < previousMaxHealth) {
                    maxHealthHasShrunk();
                }
                previousMaxHealth = current;
            }
        }
    }

    @Override
    public void maxHealthHasShrunk() {
        maxHealthShrinkTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean consumeRecentMaxHealthShrink() {
        return maxHealthShrinkTimestamp >= 0 && System.currentTimeMillis() - maxHealthShrinkTimestamp <= SHRINK_WINDOW_MILLIS;
    }
}
