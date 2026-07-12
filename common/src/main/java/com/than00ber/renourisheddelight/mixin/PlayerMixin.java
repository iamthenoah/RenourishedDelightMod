package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.AttributeModifierInstance;
import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    @Unique private static final int NIGHT_DURATION_TICKS = 10917;

    @Unique private long sleepStartDayTime = -1L;

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

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void actuallyHurt(CallbackInfo callback) {
        getDiet().onDamaged();
    }
    
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            if (player.gameMode.isSurvival()) {
                int hearts = player.level().getGameRules().getInt(GameRuleRegistry.PLAYER_STARTING_HEARTS);
                AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealth != null) maxHealth.setBaseValue(Math.clamp(hearts, 2, 40));
                Diet diet = getDiet();

                if (isDeadOrDying()) {
                    for (ConsumableFoodInstance instance : diet.getSlots()) {
                        for (AttributeModifierInstance bonus : instance.attributes()) {
                            AttributeInstance attribute = player.getAttribute(bonus.attribute());
                            if (attribute != null) attribute.removeModifier(bonus.modifier());
                        }
                    }
                    updateDiet();
                } else if (diet.tick(player)) {
                    updateDiet();
                }
            }
        }
    }
    
    @Override
    public void startSleeping(BlockPos pos) {
        super.startSleeping(pos);

        if ((Object) this instanceof ServerPlayer player) {
            sleepStartDayTime = player.level().getDayTime();
        }
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void stopSleepInBed(boolean something, boolean another, CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            if (player.gameMode.isSurvival() && sleepStartDayTime != -1L) {
                long elapsed = player.level().getDayTime() - sleepStartDayTime;
                sleepStartDayTime = -1L;
    
                if (elapsed > 0) {
                    double fraction = Math.min(1.0, elapsed / (double) NIGHT_DURATION_TICKS);
                    int sleepFoodDrain = player.level().getGameRules().getInt(GameRuleRegistry.SLEEP_FOOD_DRAIN);
                    int drain = (int) Math.round(sleepFoodDrain * fraction);
    
                    if (getDiet().drain(player, drain)) {
                        updateDiet();
                    }
                }
            }
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void attack(Entity target, CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            if (player.gameMode.isSurvival()) {
                int drain = player.level().getGameRules().getInt(GameRuleRegistry.ATTACK_FOOD_DRAIN);

                if (getDiet().drain(player, drain)) {
                    updateDiet();
                }
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void jumpFromGround(CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player) {
            if (player.gameMode.isSurvival()) {
                int drain = player.level().getGameRules().getInt(GameRuleRegistry.JUMP_FOOD_DRAIN);

                if (getDiet().drain(player, drain)) {
                    updateDiet();
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