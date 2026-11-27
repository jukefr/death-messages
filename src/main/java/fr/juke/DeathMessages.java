package fr.juke;

import fr.juke.command.DeathMessagesCommand;
import fr.juke.config.DeathMessagesConfig;
import fr.juke.death.DeathMessageService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathMessages implements ModInitializer {
    public static final String MOD_ID = "death-messages";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        DeathMessagesConfig.INSTANCE.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                DeathMessagesCommand.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                DeathMessageService.onJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                DeathMessageService.onLeave(handler.player));
        ServerLivingEntityEvents.AFTER_DEATH.register(DeathMessageService::onDeath);

        LOGGER.info("Loaded death-messages ({} entity entries, {} damage entries)",
                DeathMessagesConfig.INSTANCE.death.yes_entity.size(),
                DeathMessagesConfig.INSTANCE.death.no_entity.size());
    }
}
