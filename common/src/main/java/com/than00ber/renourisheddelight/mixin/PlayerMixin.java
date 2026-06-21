package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements DietHolder {

    @Unique private static final EntityDataAccessor<Diet> DIET_ACCESSOR = SynchedEntityData.defineId(Player.class, Diet.DATA_SERIALIZER);

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public Diet getDiet() {
        return entityData.get(DIET_ACCESSOR);
    }

    @Override
    public void updateDiet() {
        entityData.set(DIET_ACCESSOR, getDiet(), true);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo callback) {
        builder.define(DIET_ACCESSOR, new Diet());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
            int hearts = player.level().getGameRules().getInt(GameRuleRegistry.PLAYER_STARTING_HEARTS);
            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            
            if (maxHealth != null) {
                maxHealth.setBaseValue(Math.clamp(hearts, 2, 40));

                if (attribute != null) {
                    Diet diet = getDiet();

                    if (isDeadOrDying()) {
                        for (ConsumableFoodInstance instance : diet.getSlots()) {
                            attribute.removeModifier(instance.hearts);
                        }
                        updateDiet();
                    } else if (diet.tick(player)) {
                        updateDiet();
                    }
                }
            }
        }
    }
    
    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callback) {
        compoundTag.put("Diet", Diet.save(getDiet()));
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callback) {
        entityData.set(DIET_ACCESSOR, Diet.load(compoundTag.getCompound("Diet")), true);
    }
}