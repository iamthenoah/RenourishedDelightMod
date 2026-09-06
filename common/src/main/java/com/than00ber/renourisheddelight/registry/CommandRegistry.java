package com.than00ber.renourisheddelight.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.than00ber.renourisheddelight.RenourishedDelightMod;
import com.than00ber.renourisheddelight.command.ClearFoodCommand;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class CommandRegistry {

    public static void init() {
        CommandRegistrationEvent.EVENT.register(CommandRegistry::register);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal(RenourishedDelightMod.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(ClearFoodCommand.build()));
    }
}
