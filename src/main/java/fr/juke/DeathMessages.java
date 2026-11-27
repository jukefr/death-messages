package fr.juke;

import fr.juke.command.DeathMessagesCommand;
import fr.juke.config.DeathMessagesConfig;
import fr.juke.death.DeathMessageService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DeathMessages.MOD_ID)
public class DeathMessages {
    public static final String MOD_ID = "death_messages";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public DeathMessages(IEventBus modEventBus, ModContainer modContainer) {
        DeathMessagesConfig.INSTANCE.load();

        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::onPlayerJoin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);

        LOGGER.info("Loaded death-messages ({} entity entries, {} damage entries)",
                DeathMessagesConfig.INSTANCE.death.yes_entity.size(),
                DeathMessagesConfig.INSTANCE.death.no_entity.size());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        DeathMessagesCommand.register(event.getDispatcher());
    }

    private void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathMessageService.onJoin(player);
        }
    }

    private void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeathMessageService.onLeave(player);
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {
        DeathMessageService.onDeath(event.getEntity(), event.getSource());
    }
}
