package fr.juke.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.juke.util.Paths;
import fr.juke.util.ResourceIds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The config is generated from the registries of the running Minecraft version, so the
 * {@code yes_entity} block contains an entry for every entity type and the
 * {@code no_entity} block contains every vanilla damage type. Modded entity types work too:
 * the death handler looks them up by registry id even when they are not listed in the file.
 */
public final class DeathMessagesConfig {
    private static final Logger LOGGER = LogManager.getLogger("death-messages");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final String FILE_NAME = "death-messages.json";

    public static final DeathMessagesConfig INSTANCE = new DeathMessagesConfig();

    public int configVersion;
    public boolean enabled = true;
    public Death death = new Death();
    public Actions join = new Actions();
    public Actions leave = new Actions();
    public Actions kill_entity = new Actions();
    public Actions kill_player = new Actions();

    private DeathMessagesConfig() {
    }

    public static final class Death {
        public boolean broadcast_to_everyone = true;
        public String default_no_entity = "";
        public String default_yes_entity = "";
        public Map<String, String> no_entity = new TreeMap<>();
        public Map<String, String> yes_entity = new TreeMap<>();
    }

    public static final class Actions {
        public List<String> actions = new ArrayList<>();
        public boolean broadcast_to_everyone = true;

        public Actions() {
        }

        public Actions(List<String> actions, boolean broadcastToEveryone) {
            this.actions = new ArrayList<>(actions);
            this.broadcast_to_everyone = broadcastToEveryone;
        }
    }

    public void load() {
        Path config = path();
        try {
            if (Files.isDirectory(config)) {
                Files.delete(config);
            }

            if (Files.notExists(config)) {
                applyDefaults();
                return;
            }

            DeathMessagesConfig read;
            try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
                read = GSON.fromJson(reader, DeathMessagesConfig.class);
            }
            if (read == null) {
                throw new IOException("Configuration file is empty");
            }

            this.configVersion = read.configVersion;
            this.enabled = read.enabled;
            this.death = read.death;
            this.join = read.join;
            this.leave = read.leave;
            this.kill_entity = read.kill_entity;
            this.kill_player = read.kill_player;

            sanitize();
        } catch (Exception e) {
            LOGGER.error("Could not load {}, using defaults", config, e);
            backup(config);
            applyDefaults();
        }
    }

    public void save() throws IOException {
        Path config = path();
        Files.createDirectories(config.getParent());

        Path temp = config.resolveSibling(FILE_NAME + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        }

        try {
            Files.move(temp, config, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, config, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void applyDefaults() {
        this.configVersion = 1;
        this.enabled = true;

        this.death = new Death();
        this.death.broadcast_to_everyone = true;
        this.death.default_no_entity = "";
        this.death.default_yes_entity = "";
        this.death.no_entity = new TreeMap<>();
        for (String id : damageTypeIds()) {
            this.death.no_entity.put(id, "");
        }
        this.death.no_entity.put("minecraft:cactus", "${player} was pricked to death");
        this.death.no_entity.put("minecraft:cramming", "${player} was squished too much");
        this.death.no_entity.put("minecraft:drown", "${player} drowned");
        this.death.no_entity.put("minecraft:falling_anvil", "${player} was squashed by a falling anvil");
        this.death.no_entity.put("minecraft:falling_block", "${player} was squashed by a falling block");
        this.death.no_entity.put("minecraft:falling_stalactite", "${player} was impaled by a falling stalactite");
        this.death.no_entity.put("minecraft:fly_into_wall", "${player} experienced kinetic energy");
        this.death.no_entity.put("minecraft:freeze", "${player} froze to death");
        this.death.no_entity.put("minecraft:hot_floor", "${player} discovered the floor was lava");
        this.death.no_entity.put("minecraft:in_fire", "${player} burned to death");
        this.death.no_entity.put("minecraft:in_wall", "${player} suffocated in a wall");
        this.death.no_entity.put("minecraft:indirect_magic", "${player} was killed by magic");
        this.death.no_entity.put("minecraft:lava", "${player} tried to swim in lava");
        this.death.no_entity.put("minecraft:lightning_bolt", "${player} was struck by lightning");
        this.death.no_entity.put("minecraft:magic", "${player} was killed by magic");
        this.death.no_entity.put("minecraft:on_fire", "${player} burned to death");
        this.death.no_entity.put("minecraft:out_of_world", "${player} fell out of the world");
        this.death.no_entity.put("minecraft:sonic_boom", "${player} was obliterated by a sonically-charged shriek");
        this.death.no_entity.put("minecraft:stalagmite", "${player} was impaled on a stalagmite");
        this.death.no_entity.put("minecraft:starve", "${player} starved to death");
        this.death.no_entity.put("minecraft:sweet_berry_bush", "${player} was poked to death by a sweet berry bush");
        this.death.no_entity.put("minecraft:wind_charge", "${player} was blown away by a wind charge");
        this.death.no_entity.put("minecraft:wither", "${player} withered away");

        this.death.yes_entity = new TreeMap<>();
        for (String id : entityTypeIds()) {
            this.death.yes_entity.put(id, "");
        }
        this.death.yes_entity.put("minecraft:bee", "${player} was stung to death");
        this.death.yes_entity.put("minecraft:creeper", "${player} was blown up by a creeper");
        this.death.yes_entity.put("minecraft:ender_dragon", "${player} was slain by the Ender Dragon");
        this.death.yes_entity.put("minecraft:iron_golem", "${player} was slain by an iron golem");
        this.death.yes_entity.put("minecraft:warden", "${player} was slain by a warden");
        this.death.yes_entity.put("minecraft:wither", "${player} was slain by a wither");

        this.join = new Actions(List.of("Welcome ${player}!"), true);
        this.leave = new Actions(List.of("Goodbye ${player}!"), true);
        this.kill_entity = new Actions(List.of("${player} killed ${killedEntity}"), true);
        this.kill_player = new Actions(List.of("${player} killed ${killedPlayer}"), true);

        saveQuietly();
        LOGGER.info("Created default config with {} entity types and {} damage types",
                this.death.yes_entity.size(), this.death.no_entity.size());
    }

    private void sanitize() {
        if (this.configVersion < 1) {
            this.configVersion = 1;
        }
        if (this.death == null) {
            this.death = new Death();
        }
        if (this.death.no_entity == null) {
            this.death.no_entity = new TreeMap<>();
        }
        if (this.death.yes_entity == null) {
            this.death.yes_entity = new TreeMap<>();
        }
        if (this.death.default_no_entity == null) {
            this.death.default_no_entity = "";
        }
        if (this.death.default_yes_entity == null) {
            this.death.default_yes_entity = "";
        }
        if (this.join == null) {
            this.join = new Actions();
        }
        if (this.leave == null) {
            this.leave = new Actions();
        }
        if (this.kill_entity == null) {
            this.kill_entity = new Actions();
        }
        if (this.kill_player == null) {
            this.kill_player = new Actions();
        }
        sanitizeActions(this.join);
        sanitizeActions(this.leave);
        sanitizeActions(this.kill_entity);
        sanitizeActions(this.kill_player);
    }

    private static void sanitizeActions(Actions actions) {
        if (actions.actions == null) {
            actions.actions = new ArrayList<>();
        }
        actions.actions.removeIf(action -> action == null);
    }

    private static List<String> damageTypeIds() {
        TreeSet<String> ids = new TreeSet<>();
        for (Field field : DamageTypes.class.getFields()) {
            if (field.getType() != ResourceKey.class) {
                continue;
            }
            try {
                ResourceKey<?> key = (ResourceKey<?>) field.get(null);
                ids.add(ResourceIds.of(key));
            } catch (ReflectiveOperationException ignored) {
                // A field we cannot read is simply not listed; the damage type still works.
            }
        }
        if (ids.isEmpty()) {
            ids.addAll(List.of(
                    "minecraft:cactus", "minecraft:cramming", "minecraft:drown", "minecraft:fall",
                    "minecraft:fly_into_wall", "minecraft:freeze", "minecraft:hot_floor",
                    "minecraft:in_fire", "minecraft:in_wall", "minecraft:lava",
                    "minecraft:lightning_bolt", "minecraft:magic", "minecraft:on_fire",
                    "minecraft:out_of_world", "minecraft:starve", "minecraft:sweet_berry_bush",
                    "minecraft:wither"));
        }
        return new ArrayList<>(ids);
    }

    private static List<String> entityTypeIds() {
        TreeSet<String> ids = new TreeSet<>();
        for (var key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            ids.add(key.toString());
        }
        return new ArrayList<>(ids);
    }

    private static void backup(Path config) {
        try {
            if (Files.exists(config)) {
                Files.move(config, config.resolveSibling(FILE_NAME + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Could not back up {}", config, e);
        }
    }

    private static Path path() {
        return Paths.configDir().resolve(FILE_NAME);
    }

    private void saveQuietly() {
        try {
            save();
        } catch (IOException e) {
            LOGGER.error("Could not save {}", path(), e);
        }
    }
}
