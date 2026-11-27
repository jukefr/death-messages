package fr.juke.death;

import fr.juke.config.DeathMessagesConfig;
import fr.juke.util.ResourceIds;
import fr.juke.util.TextParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class DeathMessageService {
    private static final ThreadLocal<Boolean> CUSTOM_DEATH_PENDING = ThreadLocal.withInitial(() -> false);

    private DeathMessageService() {
    }

    public static void onJoin(ServerPlayer player) {
        DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
        if (!config.enabled) {
            return;
        }
        runActions(config.join, player, null, null);
    }

    public static void onLeave(ServerPlayer player) {
        DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
        if (!config.enabled) {
            return;
        }
        runActions(config.leave, player, null, null);
    }

    /**
     * Called once per living-entity death. Only player kills produce a kill message.
     */
    public static void onDeath(LivingEntity victim, DamageSource source) {
        DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
        if (!config.enabled || victim.level().isClientSide()) {
            return;
        }

        Entity killer = source.getEntity();
        if (killer == victim || !(killer instanceof ServerPlayer killerPlayer)) {
            return;
        }

        if (victim instanceof ServerPlayer victimPlayer) {
            runActions(config.kill_player, killerPlayer, victimPlayer, source);
        } else {
            runActions(config.kill_entity, killerPlayer, victim, source);
        }
    }

    /**
     * Builds a replacement for the vanilla death message, or returns {@code null} to keep it.
     */
    public static Component buildDeathMessage(DamageSource source, LivingEntity victim) {
        DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
        if (!config.enabled || !(victim instanceof ServerPlayer player)) {
            return null;
        }

        Entity attacker = source.getEntity();
        if (attacker == victim) {
            attacker = null;
        }
        if (attacker == null) {
            Entity direct = source.getDirectEntity();
            if (direct != victim) {
                attacker = direct;
            }
        }

        String damageType = damageTypeId(source);
        String template;
        if (attacker != null) {
            template = config.death.yes_entity.get(entityId(attacker));
            if (!hasText(template)) {
                template = config.death.default_yes_entity;
            }
        } else {
            template = config.death.no_entity.get(damageType);
            if (!hasText(template)) {
                template = config.death.default_no_entity;
            }
        }

        if (!hasText(template)) {
            return null;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName().getString());
        placeholders.put("damageType", damageType);
        placeholders.put("killedEntity", attacker == null ? "" : attacker.getDisplayName().getString());
        placeholders.put("killedPlayer", "");

        CUSTOM_DEATH_PENDING.set(true);
        return TextParser.parse(TextParser.expand(template, placeholders));
    }

    public static boolean consumeCustomDeathFlag() {
        boolean pending = CUSTOM_DEATH_PENDING.get();
        CUSTOM_DEATH_PENDING.set(false);
        return pending;
    }

    public static void clearCustomDeathFlag() {
        CUSTOM_DEATH_PENDING.set(false);
    }

    public static void sendCustomDeath(ServerPlayer victim, PlayerList playerList, Component message) {
        DeathMessagesConfig config = DeathMessagesConfig.INSTANCE;
        if (config.death.broadcast_to_everyone) {
            playerList.broadcastSystemMessage(message, false);
        } else {
            victim.sendSystemMessage(message);
        }
    }

    private static void runActions(DeathMessagesConfig.Actions actions, ServerPlayer actor,
                                   LivingEntity victim, DamageSource source) {
        if (actions == null || actions.actions == null || actions.actions.isEmpty()) {
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", actor.getName().getString());
        placeholders.put("killedPlayer", victim instanceof ServerPlayer ? victim.getName().getString() : "");
        placeholders.put("killedEntity", victim == null ? "" : victim.getDisplayName().getString());
        placeholders.put("damageType", source == null ? "" : damageTypeId(source));

        MinecraftServer server = actor.level().getServer();

        for (String raw : actions.actions) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String expanded = TextParser.expand(raw, placeholders);
            if (expanded.startsWith("/")) {
                if (server != null) {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), expanded);
                }
                continue;
            }

            Component message = TextParser.parse(expanded);
            if (actions.broadcast_to_everyone && server != null) {
                server.getPlayerList().broadcastSystemMessage(message, false);
            } else {
                actor.sendSystemMessage(message);
            }
        }
    }

    private static String entityId(Entity entity) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key == null ? "" : key.toString();
    }

    private static String damageTypeId(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(ResourceIds::of)
                .orElse(source.getMsgId());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
