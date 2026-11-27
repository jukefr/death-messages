package fr.juke.command;

import com.mojang.brigadier.CommandDispatcher;
import fr.juke.config.DeathMessagesConfig;
import fr.juke.util.PermissionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class DeathMessagesCommand {
    private DeathMessagesCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("deathmessages")
                .then(Commands.literal("reload")
                        .requires(PermissionUtil::isOperator)
                        .executes(context -> {
                            DeathMessagesConfig.INSTANCE.load();
                            context.getSource().sendSystemMessage(
                                    Component.literal("Death Messages config reloaded.")
                                            .withStyle(ChatFormatting.GREEN));
                            return 1;
                        }))
                .then(Commands.literal("status")
                        .requires(PermissionUtil::isOperator)
                        .executes(context -> {
                            DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
                            context.getSource().sendSystemMessage(
                                    Component.literal("Death Messages 1.0.0")
                                            .withStyle(ChatFormatting.AQUA));
                            context.getSource().sendSystemMessage(
                                    Component.literal("enabled: " + config.enabled
                                                    + ", entity entries: " + config.death.yes_entity.size()
                                                    + ", damage entries: " + config.death.no_entity.size())
                                            .withStyle(ChatFormatting.GRAY));
                            return 1;
                        })));
    }
}
