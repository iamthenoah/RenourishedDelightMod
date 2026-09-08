package com.than00ber.renourisheddelight.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.than00ber.renourisheddelight.food.DietHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ClearFoodCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("clear")
                .executes(c -> clear(c.getSource(), List.of(c.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(c -> clear(c.getSource(), EntityArgument.getPlayers(c, "targets"))));
    }

    private static int clear(CommandSourceStack source, Collection<ServerPlayer> targets) {
        List<ServerPlayer> cleared = new ArrayList<>();

        for (ServerPlayer player : targets) {
            if (player instanceof DietHolder holder && holder.getDiet().clear(player)) {
                holder.updateDiet();
                cleared.add(player);
            }
        }
        if (cleared.isEmpty()) {
            source.sendFailure(Component.translatable("commands.renourisheddelight.clear.failed"));
            return 0;
        }
        if (cleared.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.renourisheddelight.clear.single", cleared.getFirst().getDisplayName()), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.renourisheddelight.clear.multiple", cleared.size()), true);
        }
        return cleared.size();
    }
}
