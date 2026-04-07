package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.food.EatingOutcome;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements DietHolder {

    @Shadow public abstract void die(DamageSource arg);

    @Unique private static final EntityDataAccessor<Diet> DIET_ACCESSOR = SynchedEntityData.defineId(Player.class, Diet.DATA_SERIALIZER);

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }
    
    @Unique
    private Player self() {
        return (Player) (Object) this;
    }

    @Override
    public Diet getDiet() {
        return entityData.get(DIET_ACCESSOR);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineSynchedData(CallbackInfo callback) {
        entityData.define(DIET_ACCESSOR, new Diet());
    }

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    public void canEat(boolean invulnerable, CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(true);
    }
    
    @Inject(method = "eat", at = @At("HEAD"))
    public void eat(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> callback) {
        if (self() instanceof ServerPlayer player) {
            Diet diet = getDiet();
            EatingOutcome outcome = diet.toOutcome(player, stack);

            if (outcome.isSuccess()) {
                outcome.process(player, diet, stack);
                entityData.set(DIET_ACCESSOR, diet, true);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo callback) {
        if (self() instanceof ServerPlayer player) {
            AttributeInstance attribute = self().getAttribute(Attributes.MAX_HEALTH);
            int hearts = player.level().getGameRules().getInt(GameRuleRegistry.PLAYER_STARTING_HEARTS);
            player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(2, Math.min(40, hearts)));
            
            if (attribute != null) {
                Diet diet = getDiet();

                if (isDeadOrDying()) {
                    for (ConsumableFoodInstance instance : diet.getSlots()) {
                        attribute.removeModifier(instance.hearts);
                    }
                    entityData.set(DIET_ACCESSOR, diet, true);
                } else if (diet.tick(player)) {
                    entityData.set(DIET_ACCESSOR, diet, true);
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