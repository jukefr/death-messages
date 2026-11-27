# Death Messages

**Custom death, join, leave, and kill messages for Minecraft servers — on Fabric and NeoForge.**

[![Modrinth](https://img.shields.io/modrinth/dt/death-messages?style=flat-square&logo=modrinth&label=Modrinth)](https://modrinth.com/mod/death-messages)
[![GitHub release](https://img.shields.io/github/v/release/jukefr/death-messages?style=flat-square&logo=github)](https://github.com/jukefr/death-messages/releases)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](https://github.com/jukefr/death-messages/blob/main/LICENSE)

![Death Messages icon](https://raw.githubusercontent.com/jukefr/death-messages/main/resources/icon.png)

Death Messages is a server-side Minecraft mod for **Fabric** and **NeoForge**. It lets you replace
the vanilla death message for any creature or damage type, and add your own messages or commands
when players join, leave, kill a mob, or kill another player. No client mod is required.

## Features

- **Custom death messages for every creature** — every entity type registered in your Minecraft
  version has an entry, and modded entity types work by their registry id too.
- **Custom messages for every damage type** — falling, lava, drowning, cactus, freezing, magic,
  explosions, and the rest.
- **Join, leave, kill-mob, and kill-player actions** — each can be a chat message or a command.
- **Vanilla fallback** — leave an entry empty and the normal death message is used.
- **Colors and formatting** — use `&` or `§` codes, just like other server mods.
- **Fabric and NeoForge** — the same config file and commands on both loaders.
- **Server-side only** — no client mod, no resource pack.

## Installation

1. Install **Fabric Loader** or **NeoForge** on your Minecraft server.
2. Download the jar for your Minecraft version from [Modrinth](https://modrinth.com/mod/death-messages)
   or [GitHub Releases](https://github.com/jukefr/death-messages/releases).
3. Put the jar in your server's `mods/` folder.
4. On Fabric, also install [Fabric API](https://modrinth.com/mod/fabric-api).
5. Start the server. The config is created at `config/death-messages.json`.
6. Edit the config and run `/deathmessages reload`, or restart the server.

> This is a **server-side** mod. Players do not need to install anything.

## Supported versions

| Loader | Minecraft versions |
|---|---|
| Fabric | 1.21.1, 1.21.11, 26.1–26.1.2, 26.2–26.3 |
| NeoForge | 1.21.1, 1.21.11, 26.1.2, 26.2–26.3 |

Use the jar that matches your Minecraft version. The 26.2–26.3 jars are built from 26.3 and
declare a range; the 26.1 jars are built from 26.1.2. NeoForge publishes 26.1.2, not 26.1 or
26.1.1.

These versions cover roughly 80% of public Minecraft servers. Older versions are not included in
the first release.

## Configuration

The config file is `config/death-messages.json`. On first start it is generated from the
registries of your Minecraft version, so every entity type and every vanilla damage type is
already present:

```json
{
  "configVersion": 1,
  "enabled": true,
  "death": {
    "broadcast_to_everyone": true,
    "default_no_entity": "",
    "default_yes_entity": "",
    "no_entity": {
      "minecraft:cactus": "${player} was pricked to death",
      "minecraft:drown": "${player} drowned",
      "minecraft:fall": "",
      "minecraft:lava": "${player} tried to swim in lava"
    },
    "yes_entity": {
      "minecraft:bee": "${player} was stung to death",
      "minecraft:creeper": "${player} was blown up by a creeper",
      "minecraft:player": "",
      "minecraft:zombie": ""
    }
  },
  "join": {
    "actions": ["Welcome ${player}!"],
    "broadcast_to_everyone": true
  },
  "leave": {
    "actions": ["Goodbye ${player}!"],
    "broadcast_to_everyone": true
  },
  "kill_entity": {
    "actions": ["${player} killed ${killedEntity}"],
    "broadcast_to_everyone": true
  },
  "kill_player": {
    "actions": ["${player} killed ${killedPlayer}"],
    "broadcast_to_everyone": true
  }
}
```

The real generated file is longer because it lists every entity type, including animals, monsters,
bosses, projectiles, and vehicles. You only need to edit the entries you care about.

| Key | Description |
|---|---|
| `enabled` | Turns the whole mod on or off. |
| `death.broadcast_to_everyone` | `true` shows custom death messages to everyone; `false` shows them only to the dead player. Vanilla death messages keep their normal team visibility. |
| `death.default_no_entity` | Fallback message when nothing damaged the player directly and no damage-type entry matched. Empty means vanilla. |
| `death.default_yes_entity` | Fallback message when a creature or projectile caused the death and no entity entry matched. Empty means vanilla. |
| `death.no_entity` | Messages keyed by damage type id, used when there is no attacker. |
| `death.yes_entity` | Messages keyed by entity type id, used when a creature or projectile caused the death. |
| `*.actions` | One or more chat messages or commands. |
| `*.broadcast_to_everyone` | `true` sends to the whole server; `false` sends only to the player who triggered the action. |

An empty message (`""`) means **use the vanilla death message**. Delete an entry or leave it empty
to keep the normal message for that creature or damage type.

### Creature and damage type keys

`yes_entity` keys are entity type ids:

```json
"minecraft:zombie": "${player} was eaten by a zombie",
"minecraft:warden": "${player} was slain by a warden",
"minecraft:ender_dragon": "${player} was slain by the Ender Dragon"
```

`no_entity` keys are damage type ids:

```json
"minecraft:fall": "${player} fell from a high place",
"minecraft:freeze": "${player} froze to death",
"minecraft:sweet_berry_bush": "${player} was poked to death by a sweet berry bush"
```

Because messages are looked up by registry id, **any creature works**, including modded mobs that
are not in your generated file. Use the same `namespace:path` id that the mod or data pack uses.

## Placeholders

| Placeholder | Death messages | Join / leave | Kill mob | Kill player |
|---|---|---|---|---|
| `${player}` | Dead player | Player | Killer | Killer |
| `${killedEntity}` | Attacker name | — | Dead mob name | Dead player name |
| `${killedPlayer}` | — | — | — | Dead player |
| `${damageType}` | Damage type id | — | Damage type id | Damage type id |

Placeholders are plain text, so they work in commands as well as chat messages.

## Actions

`join`, `leave`, `kill_entity`, and `kill_player` each contain a list of actions. An action is
either:

- **A chat message** — anything that does not start with `/`. It is sent as a system message.
- **A command** — anything that starts with `/`. It runs from the server console, so you do not
  need to be an operator and no player permission is checked.

Examples:

```json
"join": {
  "actions": [
    "&eWelcome ${player}!",
    "/give ${player} minecraft:bread 4"
  ],
  "broadcast_to_everyone": true
}
```

```json
"kill_player": {
  "actions": [
    "&c${player} &7killed &c${killedPlayer}",
    "&7${killedPlayer} will respawn shortly."
  ],
  "broadcast_to_everyone": true
}
```

To disable an action group, set `"actions": []`.

## Formatting

Use `&` or `§` before a code. For example, `&cRed text` and `§cRed text` are the same.

```
0 black        8 dark gray
1 dark blue    9 blue
2 dark green   a green
3 dark aqua    b aqua
4 dark red     c red
5 dark purple  d light purple
6 gold         e yellow
7 gray         f white

k obfuscated   l bold   m strikethrough
n underline    o italic r reset
```

## Commands

| Command | Who | Description |
|---|---|---|
| `/deathmessages reload` | Operators | Reloads `config/death-messages.json`. |
| `/deathmessages status` | Operators | Shows whether the mod is enabled and how many keys are loaded. |

## How it works

Death Messages hooks the vanilla death-message lookup, so it works with every damage source and
every mob without special cases. When a configured message exists it replaces the vanilla message;
otherwise the vanilla message is left untouched. Join, leave, and kill actions use the standard
server events.

Because the config is generated from the registry, an entry exists for every entity type in the
running version. New mobs added by Minecraft updates are supported as soon as the server runs,
even if their key is not in an older config file.

## FAQ

**Do players need the mod installed?**
No. It is server-side only. On Fabric the server needs Fabric API.

**Why is a death message still the vanilla one?**
The matching entry is empty or missing. Fill in `yes_entity` for a mob or `no_entity` for a damage
type. Empty means "use vanilla".

**Why do I see a death message and a kill message?**
They are separate features. `death` replaces the normal death message; `kill_entity` and
`kill_player` add their own announcements. Set their `actions` to `[]` if you only want one.

**Do modded mobs work?**
Yes. Use the mob's registry id, for example `mymod:my_monster`, in `yes_entity`.

**Can I use commands instead of chat messages?**
Yes. Any action that starts with `/` runs from the server console.

**Does it work with teams?**
Vanilla death messages keep their normal team visibility. A custom death message follows
`death.broadcast_to_everyone`: everyone when `true`, only the dead player when `false`.

**Do I have to restart after editing the config?**
No, operators can run `/deathmessages reload`. A restart also works.

## Links

- [Modrinth](https://modrinth.com/mod/death-messages)
- [GitHub](https://github.com/jukefr/death-messages)
- [Issue tracker](https://github.com/jukefr/death-messages/issues)

## License

MIT. See [LICENSE](https://github.com/jukefr/death-messages/blob/main/LICENSE).
