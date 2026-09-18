package com.than00ber.renourisheddelight.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NutritionDecayCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build(CommandBuildContext registry) {
        return Commands.literal("decay")
                .then(Commands.literal("reset")
                        .executes(context -> reset(context.getSource(), List.of(context.getSource().getPlayerOrException()), null))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> reset(context.getSource(), EntityArgument.getPlayers(context, "targets"), null))
                                .then(Commands.argument("item", ItemArgument.item(registry))
                                        .executes(context -> reset(context.getSource(), EntityArgument.getPlayers(context, "targets"), ItemArgument.getItem(context, "item").getItem())))));
    }

    private static int reset(CommandSourceStack source, Collection<ServerPlayer> targets, @Nullable Item item) {
        List<ServerPlayer> reset = new ArrayList<>();

        for (ServerPlayer player : targets) {
            if (player instanceof DietHolder holder && (item != null ? holder.getDiet().resetDecay(item) : holder.getDiet().resetDecay())) {
                holder.updateDiet();
                reset.add(player);
            }
        }
        if (reset.isEmpty()) {
            source.sendFailure(Component.translatable("commands.renourisheddelight.decay.failed"));
            return 0;
        }
        Component scope = item != null ? Component.translatable(item.getDescriptionId()) : Component.translatable("commands.renourisheddelight.decay.all");

        if (reset.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.renourisheddelight.decay.single", reset.getFirst().getDisplayName(), scope), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.renourisheddelight.decay.multiple", reset.size(), scope), true);
        }
        return reset.size();
    }
}
