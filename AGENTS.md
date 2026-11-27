# AGENTS.md

Instructions for future agents working on this repository.

## Project overview

Death Messages (`death-messages`) is a **server-side only** mod for **Fabric and NeoForge**
that replaces the vanilla death message with configurable ones and adds configurable join,
leave, kill-mob, and kill-player actions.

- Display/project name: Death Messages
- Internal Fabric mod id: `death-messages`; internal NeoForge mod id: `death_messages`
  (NeoForge ids cannot contain `-`)
- Fabric main class: `fr.juke.DeathMessages`
- NeoForge main class: `fr.juke.DeathMessages` (annotated with `@Mod`)
- Config file: `config/death-messages.json` (same file on both loaders)
- GitHub: https://github.com/jukefr/death-messages
- Modrinth: https://modrinth.com/mod/death-messages
  (project id `ApuOy844`, slug `death-messages`, license MIT)
- Package root: `fr.juke`

The project was rebuilt from the old "Fabric Death Messages" Modrinth page. The old source repo
(`jukefr/fabric-death-messages`) is gone. Do not try to restore it; this repository is the source
of truth.

## The most important rule

**Do not change the config keys or their meaning without a migration.**

Current config is version 1:

```json
{
  "configVersion": 1,
  "enabled": true,
  "death": {
    "broadcast_to_everyone": true,
    "default_no_entity": "",
    "default_yes_entity": "",
    "no_entity": {},
    "yes_entity": {}
  },
  "join": { "actions": [], "broadcast_to_everyone": true },
  "leave": { "actions": [], "broadcast_to_everyone": true },
  "kill_entity": { "actions": [], "broadcast_to_everyone": true },
  "kill_player": { "actions": [], "broadcast_to_everyone": true }
}
```

Invariants:

- `death.no_entity` is keyed by **damage type id** (`minecraft:fall`).
- `death.yes_entity` is keyed by **entity type id** (`minecraft:zombie`, `mymod:my_monster`).
- An empty string means **use the vanilla death message**. Do not treat empty as disabled.
- `death.default_no_entity` / `death.default_yes_entity` are fallbacks when no keyed entry matched.
- `join`, `leave`, `kill_entity`, and `kill_player` contain a list of actions. An action starting
  with `/` is a command run by the server console; anything else is a chat message.
- `broadcast_to_everyone` is per-section.
- Placeholders are `${player}`, `${killedEntity}`, `${killedPlayer}`, `${damageType}`.

If a schema change is ever required, bump `configVersion`, keep the old keys readable, and add a
migration path in `DeathMessagesConfig.sanitize()`.

## Supported versions and why

Statistics checked in November 2026:

- `mc-servers.com`: 26.3 36.2%, 26.2 20.4%, 1.21.11 13.8%, 26.1.2 4.9%, 1.21.1 4.7%,
  1.20.1 3.4%.
- bStats (2 Nov 2026, 191,920 servers): 26.3 28.4%, 1.21.11 21.8%, 26.2 20.0%,
  26.1.2 6.5%, 1.21.4 3.4%.

The first release targets the versions that cover roughly 80% of servers:

| Loader | Versions | Build branch |
|---|---|---|
| Fabric | 1.21.1 | `1.21.1` |
| Fabric | 1.21.11 | `1.21.11` |
| Fabric | 26.1, 26.1.1, 26.1.2 | `26.1.2` |
| Fabric | 26.2, 26.3 | `main` (26.3) |
| NeoForge | 1.21.1 | `neoforge-1.21.1` |
| NeoForge | 1.21.11 | `neoforge-1.21.11` |
| NeoForge | 26.1.2 | `neoforge-26.1.2` |
| NeoForge | 26.2, 26.3 | `neoforge-26.3` |

Fabric's technical minimum is 1.16.5 and NeoForge's is 1.20.1, but those versions are only a few
percent of servers. Do not add them just to say they are supported; add a branch only when there
is demand and the port is tested.

## Branch layout

`main` tracks the latest supported Minecraft version (currently 26.3). One branch per supported
line:

| Branch | Minecraft | Notes |
|---|---|---|
| `1.21.1` | 1.21.1 | Fabric, Mojang mappings |
| `1.21.11` | 1.21.11 | Fabric, Mojang mappings |
| `26.1.2` | 26.1–26.1.2 | Fabric, unobfuscated |
| `main` | 26.2–26.3 | Fabric, unobfuscated |
| `neoforge-1.21.1` | 1.21.1 | NeoForge |
| `neoforge-1.21.11` | 1.21.11 | NeoForge |
| `neoforge-26.1.2` | 26.1.2 | NeoForge |
| `neoforge-26.3` | 26.2–26.3 | NeoForge |

Use **Mojang official mappings** on every Fabric branch. It keeps the shared code close to the
NeoForge version.

## Toolchains

| Branch range | Gradle | Build JDK | Loom / plugin | Java target |
|---|---|---|---|---|
| 1.21.1 – 1.21.11 Fabric | 9.8.2 | 25 | Loom 1.19.4, Mojang mappings | 21 |
| 26.x Fabric | 9.8.2 | 25 | Loom 1.19.4, no mappings | 25 |
| NeoForge 1.21.x | 9.2.1 | 21 | ModDevGradle 2.0.148 | 21 |
| NeoForge 26.x | 9.2.1 | 25 | ModDevGradle 2.0.148 | 25 |

Build commands:

```bash
# Fabric 1.21.x and 26.x
JAVA_HOME=/usr/lib/jvm/java-25-openjdk \
PATH=/usr/lib/jvm/java-25-openjdk/bin:$PATH \
./gradlew build --no-daemon

# NeoForge 1.21.x
JAVA_HOME=/usr/lib/jvm/java-21-openjdk \
PATH=/usr/lib/jvm/java-21-openjdk/bin:$PATH \
./gradlew build --no-daemon

# NeoForge 26.x
JAVA_HOME=/usr/lib/jvm/java-25-openjdk \
PATH=/usr/lib/jvm/java-25-openjdk/bin:$PATH \
./gradlew build --no-daemon
```

Loom 1.19.4 requires **Java 25 to run Gradle**, even on branches whose compiled release target is
Java 21.

NeoForge builds take longer on the first run because ModDevGradle decompiles and patches
Minecraft for each NeoForge version.

## Code layout

Shared code lives in `fr.juke`:

- `DeathMessages` — loader entrypoint (Fabric `ModInitializer`; NeoForge `@Mod`)
- `config.DeathMessagesConfig` — config load/save, registry-generated defaults, sanitizing
- `death.DeathMessageService` — death-message lookup, placeholders, join/leave/kill actions
- `command.DeathMessagesCommand` — `/deathmessages reload` and `/deathmessages status`
- `util.TextParser` — placeholder expansion and `&`/`§` formatting
- `util.Paths` — loader-specific config directory
- `mixin.DamageSourceMixin` — replaces `getLocalizedDeathMessage`
- `mixin.ServerPlayerMixin` — honors `broadcast_to_everyone` and clears the per-death flag

Only `Paths`, `PermissionUtil`, `ResourceIds`, and the entrypoint should differ between Fabric and
NeoForge or between 1.21.x and 26.x. If you find yourself forking more files, move the difference
behind a small platform helper.

### Platform/version differences

- **1.21.11 and 26.x** renamed `ResourceKey#location()` to `ResourceKey#identifier()`.
  Use `fr.juke.util.ResourceIds.of(key)`; do not call either directly in shared code.
- **1.21.11 and 26.x** removed `CommandSourceStack#hasPermission(int)`. Use
  `fr.juke.util.PermissionUtil.isOperator(source)`, which uses
  `permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)` on 1.21.11+ and
  `hasPermission(2)` on 1.21.1.
- **1.21.11 and 26.x** removed `Entity#getServer()`. Use `entity.level().getServer()`, which works
  on every branch.
- **1.21.11 and 26.x** use `Identifier` where 1.21.1 used `ResourceLocation`. Use `var` for
  registry keys.
- **Authlib** differences do not matter here; there is no profile code.
- Fabric uses `ServerPlayConnectionEvents.JOIN`/`DISCONNECT` and
  `ServerLivingEntityEvents.AFTER_DEATH`.
- NeoForge uses `PlayerEvent.PlayerLoggedInEvent`, `PlayerEvent.PlayerLoggedOutEvent`, and
  `LivingEvent.LivingDeathEvent`.
- NeoForge uses `FMLPaths.CONFIGDIR.get()` in `Paths`; Fabric uses
  `FabricLoader.getInstance().getConfigDirectory().toPath()`.
- NeoForge mod id is `death_messages`; the config file name stays `death-messages.json`.

### Mixin compatibility level

- 26.x: `JAVA_25`
- 1.21.x: `JAVA_21`

Do not lower these without testing; Mixin rejects class files newer than the configured level.

### How the death-message replacement works

1. `DamageSourceMixin` injects at the head of `DamageSource#getLocalizedDeathMessage`.
2. `DeathMessageService.buildDeathMessage` returns `null` (keep vanilla) or a `Component`
   (replace vanilla), and sets a thread-local flag when it replaces the message.
3. `ServerPlayerMixin` clears that flag at the head of `ServerPlayer#die`, then redirects the
   three death-message broadcast calls. If the flag is set, it sends the custom message according
   to `death.broadcast_to_everyone`; otherwise it calls the original method so vanilla team
   visibility is preserved.

This design means the content hook works for every creature and every damage source without
special cases. Do not replace it with an "after death" event that only adds a second message:
that would leave the vanilla message in place.

## Testing

The fastest smoke test is a real dedicated server:

```bash
mkdir -p run
printf 'eula=true\n' > run/eula.txt
printf 'online-mode=false\nserver-port=25599\nwhite-list=false\nnetwork-compression-threshold=-1\nlevel-type=flat\n' > run/server.properties
JAVA_HOME=/usr/lib/jvm/java-25-openjdk \
PATH=/usr/lib/jvm/java-25-openjdk/bin:$PATH \
./gradlew runServer --no-daemon
```

Check the log for:

```text
Created default config with <N> entity types and <N> damage types
Loaded death-messages (<N> entity entries, <N> damage entries)
```

`config/death-messages.json` should contain every entity type for the running version. Mixins are
only applied when their target classes load. A useful temporary check is to call `Class.forName`
on `DamageSource` and `ServerPlayer` during startup, then remove the check before committing.

Test at least:

- a config with a custom `yes_entity` message for a common mob;
- a config with an empty entry (vanilla fallback);
- `broadcast_to_everyone` true and false;
- join and leave actions;
- a command action (`/say ...`);
- `/deathmessages reload`.

## Git workflow

1. Make the change on `main` first.
2. Backport to every affected branch, keeping each era's mappings and Java target intact.
3. Build every branch.
4. Bump `mod_version` in `gradle.properties` on every affected branch.
5. Update the Minecraft dependency range in `fabric.mod.json` / `neoforge.mods.toml`.
6. Tag and release.

Use temporary worktrees for isolated builds:

```bash
git worktree add -f /tmp/opencode/dm-1.21.11 1.21.11
rm -rf /tmp/opencode/dm-1.21.11/build /tmp/opencode/dm-1.21.11/.gradle
git worktree remove --force /tmp/opencode/dm-1.21.11
```

## Release ranges

| Release key | Versions | Built from |
|---|---|---|
| `1.21.1` | 1.21.1 | `1.21.1` |
| `1.21.11` | 1.21.11 | `1.21.11` |
| `26.1` | 26.1, 26.1.1, 26.1.2 | `26.1.2` |
| `26.2` | 26.2, 26.3 | `main` |

Jar naming:

```
death-messages-<modversion>+mc<range>.jar
death-messages-<modversion>+mc<version>-neoforge.jar
```

Examples: `death-messages-1.0.0+mc1.21.11.jar`,
`death-messages-1.0.0+mc26.2-26.3.jar`,
`death-messages-1.0.0+mc1.21.1-neoforge.jar`.

GitHub releases use one tag per Fabric range, with NeoForge jars uploaded to the matching
release. Modrinth needs separate versions per loader because the loader field differs.

## Modrinth

- Project id: `ApuOy844`
- Project slug: `death-messages` (do not change)
- Title: `Death Messages`
- Environment: `server_only`
- License: MIT
- Project body mirrors `README.md`
- Loaders: `["fabric"]` or `["neoforge"]`
- Use exact game versions for NeoForge; list every supported game version in Modrinth's
  `game_versions`.

The token lives in `.env` (`MODRINTH_TOKEN=...`), must stay `chmod 600`, and must never be
printed, committed, or pasted into logs.

Load it without echoing it:

```bash
set -a
. ./.env
set +a
```

### Create a version

`POST https://api.modrinth.com/v2/version` is a multipart request:

- `data`: JSON metadata
- one or more file fields listed in `file_parts`
- `Authorization: <token>` header
- a descriptive `User-Agent`

Example:

```bash
UA="jukefr/death-messages-publisher/1.0 (code@juke.fr)"
DATA='{
  "project_id": "ApuOy844",
  "name": "1.0.0 for 26.2 - 26.3",
  "version_number": "1.0.0+26.2",
  "changelog": "First release: custom death messages for every creature, join/leave/kill messages, and Fabric + NeoForge support.",
  "game_versions": ["26.2", "26.3"],
  "version_type": "release",
  "loaders": ["fabric"],
  "environment": "server_only",
  "featured": false,
  "status": "listed",
  "file_parts": ["file"],
  "primary_file": "file",
  "dependencies": []
}'

curl -X POST "https://api.modrinth.com/v2/version" \
  -A "$UA" \
  -H "Authorization: $MODRINTH_TOKEN" \
  -F "data=$DATA;type=application/json" \
  -F "file=@death-messages-1.0.0+mc26.2-26.3.jar;type=application/java-archive"
```

Rules:

- `version_number` must be unique per project. Use `<modversion>+<range>` for Fabric and
  `<modversion>+<mcversion>-neoforge` for NeoForge.
- Each version lists every supported game version in `game_versions`. NeoForge versions use exact
  game versions, not ranges.
- `loaders` is `["fabric"]` for Fabric and `["neoforge"]` for NeoForge.
- `environment` is `server_only`.
- `version_type` is `release`.
- Do not publish alpha/beta/snapshot Minecraft versions.

### Update the project page

When the README changes, update the Modrinth project body with
`PATCH /v2/project/ApuOy844` and a JSON field named `body`. Keep the slug. Also update the source
URL to `https://github.com/jukefr/death-messages`.

### Archive superseded versions

After publishing a new release series, archive every Modrinth version older than the new series.
Do **not** delete them: archived versions remain downloadable by direct link. Archiving needs the
`VERSION_WRITE` scope.

```bash
curl -X PATCH "https://api.modrinth.com/v2/version/<version-id>" \
  -A "jukefr/death-messages-publisher/1.0 (code@juke.fr)" \
  -H "Authorization: $MODRINTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"archived"}'
```

A successful request returns `204 No Content`.

The old 1.0.0 Fabric version from 2021 (`u4m1cYOg`) must be archived after the new series is
published. Its files can stay downloadable.

## Release checklist

1. Confirm `main` is at the latest supported version (`26.3`).
2. Confirm every branch builds with its era's JDK.
3. Bump `mod_version` in `gradle.properties` on every branch being released.
4. Make sure each loader metadata file has the correct Minecraft version/range and Java
   requirement (`fabric.mod.json` for Fabric, `neoforge.mods.toml` for NeoForge).
5. Build each Fabric range endpoint and each NeoForge version, and verify the jar metadata:
   ```bash
   unzip -p build/libs/death-messages-<ver>.jar fabric.mod.json
   unzip -p build/libs/death-messages-<ver>.jar META-INF/neoforge.mods.toml
   ```
6. Commit and push all branches.
7. Create and push grouped tags for Fabric ranges.
8. Create GitHub releases for every Fabric range, and upload the matching NeoForge jars to those
   releases.
9. Create the equivalent Modrinth versions for both loaders.
10. Archive every Modrinth version older than the new release series.
11. Verify both release pages: only the newest GitHub release is marked latest; only versions
    from the newest release series are `listed` on Modrinth.

## Common pitfalls

- **Empty string means vanilla.** Do not skip empty entries when looking up a message; treat them
  as "no custom message" and fall through to the default template.
- **Entity vs damage key.** A zombie hit uses `yes_entity` (`minecraft:zombie`), not
  `no_entity` (`minecraft:mob_attack`). Falling uses `no_entity` (`minecraft:fall`).
- **Projectiles.** Use `source.getEntity()` first (the shooter), then `source.getDirectEntity()`
  (the arrow). This is why a skeleton's arrow says "slain by a skeleton".
- **`Entity#getServer()` is gone in 1.21.11+.** Use `level().getServer()`.
- **`ResourceKey#location()` is gone in 1.21.11+.** Use `ResourceIds.of`.
- **`hasPermission(int)` is gone in 1.21.11+.** Use `PermissionUtil.isOperator`.
- **Mixin levels.** 1.21.x uses `JAVA_21`; 26.x uses `JAVA_25`.
- **NeoForge mod id** is `death_messages`, not `death-messages`.
- **NeoForge mixins** must be registered in `neoforge.mods.toml` with `[[mixins]]`.
- **Wrong JDK** — Loom 1.19.4 needs JDK 25 to run Gradle, even for Java 21 targets.
- **26.x mappings** — no Yarn, no `mappings` line, use `net.fabricmc.fabric-loom` and
  `implementation`, not `modImplementation`.
- **Config reload** — `/deathmessages reload` is operator-only.
- **`.env` and `run/`** — both are gitignored. Keep them out of commits.
