package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.registry.EffectRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final Identifier FARMERS_DELIGHT_NOURISHMENT = Identifier.fromNamespaceAndPath("farmersdelight", "nourishment");

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void addEffect(MobEffectInstance instance, CallbackInfoReturnable<Boolean> callback) {
        if (FARMERS_DELIGHT_NOURISHMENT.equals(BuiltInRegistries.MOB_EFFECT.getKey(instance.getEffect().value()))) {
            callback.setReturnValue(true);
            LivingEntity self = (LivingEntity) (Object) this;
            self.addEffect(new MobEffectInstance(
                    EffectRegistry.NOURISHMENT,
                    instance.getDuration(),
                    instance.getAmplifier(),
                    instance.isAmbient(),
                    instance.isVisible(),
                    instance.showIcon()));
        }
    }
}