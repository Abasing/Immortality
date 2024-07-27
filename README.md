# Immortality Plugin

**Immortality** is a Minecraft plugin that allows players to become immortal, preventing them from dying from fatal damage. 

## Features

- Toggle Immortality mode for players
- Cooldown time for reactivating Immortality
- Health threshold requirement for activation
- Activation delay
- Environment restrictions (worlds and biomes)
- Configurable messages

## Commands

- `/immortality toggle [player]` - Toggles Immortality mode for the player.
- `/immortality list` - Lists all players with Immortality mode enabled.
- `/immortality reload` - Reloads the plugin configuration.

## Permissions

- `immortality.use` - Allows the player to use the Immortality command.
- `immortality.reload` - Allows the player to reload the plugin configuration.
- `Immortality.bypass.cooldown` - Bypass the cooldown to activate the Immortality mode.
- `Immortality.bypass.health.requirement` - Bypasses the health threshold for activating Immortality mode.
- `Immortality.bypass.activation.delay` - bypass the activation delay for activating Immortality mode.

## Configuration

### `config.yml`

```yaml
enable-cooldown-time: true
cooldown-time: 600 # Cooldown in seconds
enable-health-threshold: true
health-threshold: 5.0 # Minimum health required to activate Immortality
enable-activation-delay: true
activation-delay: 5 # Delay in seconds
enable-environment-restrictions: true
environment-restrictions:
  allowed-worlds: ["world", "world_nether"]
  allowed-biomes: ["FOREST", "PLAINS"]
```

### `messages.yml`

```yaml
messages:
  ImmortalityEnabled: "&aImmortality is now enabled for you."
  ImmortalityDisabled: "&cImmortality is now disabled for you."
  playerNotFound: "&cPlayer not found."
  reloadSuccess: "&aImmortality configuration reloaded."
  noPermission: "&cYou do not have permission to use this command."
  cooldownActive: "&cYou must wait before reactivating Immortality."
  healthTooLow: "&cYour health is too low to activate Immortality."
  activationDelayed: "&eImmortality will activate in %seconds% seconds."
  environmentRestricted: "&cYou cannot activate Immortality in this environment."
  ImmortalityListHeader: "&6Players with Immortality enabled:"
  ImmortalityListEntry: "&7- &e%player%"
```

## Installation

1. Download the latest release of the plugin.
2. Place the `.jar` file in your server's `plugins` folder.
3. Start your server to generate the configuration files.
4. Edit the `config.yml` and `messages.yml` files to your liking.
5. Reload or restart your server to apply the changes.

## How to Use

1. **Toggle Immortality:** Use `/immortality toggle [player]` to enable or disable Immortality mode for yourself or another player.
2. **Check Immortality Status:** Use `/immortality list` to see all players with Immortality mode enabled.
3. **Reload Configuration:** Use `/immortality reload` to reload the plugin's configuration files after making changes.

## Authors

- Abasing
- Zamin Ali

## License

This project is licensed under the MIT License.
