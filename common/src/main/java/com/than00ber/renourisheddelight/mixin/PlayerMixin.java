package com.than00ber.renourisheddelight.mixin;

import com.than00ber.renourisheddelight.food.Diet;
import com.than00ber.renourisheddelight.food.DietHolder;
import com.than00ber.renourisheddelight.registry.GameRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
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
    @Unique private static final int NIGHT_DURATION_TICKS = 10917;

    @Unique private static long renourisheddelight$lastSleepDrainTime = -1L;

    @Unique private long renourisheddelight$sleepStartDayTime = -1L;
    @Unique private long renourisheddelight$sleepStartGameTime = -1L;

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
    private void renourisheddelight$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo callback) {
        builder.define(DIET_ACCESSOR, new Diet());
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void renourisheddelight$actuallyHurt(CallbackInfo callback) {
        getDiet().onDamaged();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void renourisheddelight$tick(CallbackInfo callback) {
        if (!((Object) this instanceof ServerPlayer player) || !player.gameMode.isSurvival()) return;

        int hearts = player.level().getGameRules().getInt(GameRuleRegistry.STARTING_HEARTS);
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) maxHealth.setBaseValue(Math.clamp(hearts, 2, 40));
        Diet diet = getDiet();

        if (isDeadOrDying()) {
            diet.clearModifiers(player);
            updateDiet();
        } else if (diet.tick(player)) {
            updateDiet();
        }
    }

    @Override
    public void startSleeping(BlockPos pos) {
        super.startSleeping(pos);

        if ((Object) this instanceof ServerPlayer player) {
            renourisheddelight$sleepStartDayTime = player.level().getDayTime();
            renourisheddelight$sleepStartGameTime = player.level().getGameTime();
        }
    }

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void renourisheddelight$stopSleepInBed(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo callback) {
        if (!((Object) this instanceof ServerPlayer player)) return;
        if (renourisheddelight$sleepStartDayTime == -1L) return;
        long slept = player.level().getGameTime() - renourisheddelight$sleepStartGameTime;
        long skipped = (player.level().getDayTime() - renourisheddelight$sleepStartDayTime) - slept;
        long gameTime = player.level().getGameTime();
        renourisheddelight$sleepStartDayTime = -1L;
        renourisheddelight$sleepStartGameTime = -1L;

        MinecraftServer server = player.getServer();
        if (skipped <= 0 || server == null) return;
        if (!player.level().getGameRules().getBoolean(GameRuleRegistry.DO_SLEEP_FOOD_DRAIN)) return;
        if (renourisheddelight$lastSleepDrainTime == gameTime) return;
        renourisheddelight$lastSleepDrainTime = gameTime;

        double fraction = Math.min(1.0, skipped / (double) NIGHT_DURATION_TICKS);
        int drain = (int) Math.round(Diet.SLEEP_DRAIN * fraction);

        for (ServerPlayer other : server.getPlayerList().getPlayers()) {
            if (other.gameMode.isSurvival() && other instanceof DietHolder holder && holder.getDiet().drain(other, drain)) {
                holder.updateDiet();
            }
        }
    }

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void renourisheddelight$addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callback) {
        compoundTag.put("Diet", Diet.save(getDiet()));
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("TAIL"))
    private void renourisheddelight$readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo callback) {
        entityData.set(DIET_ACCESSOR, Diet.load(compoundTag.getCompound("Diet")), true);
    }
}
