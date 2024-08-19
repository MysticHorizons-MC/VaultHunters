# VaultHunters

VaultHunters is a Minecraft plugin designed for version 1.21 and above. It allows server owners and developers to customize how Vaults operate by injecting custom loot using NBT data. The plugin offers advanced features to modify loot tables, integrate custom tiers of loot, and dynamically inject new loot into Vaults based on defined configurations.

## Features

- **Custom Loot Tiers:** Define custom tiers of loot that can be injected into Vaults.
- **NBT Data Integration:** Use NBT data to add complex items to Vaults.
- **Dynamic Loot Injection:** Inject loot into Vaults based on predefined configurations and randomization.
- **Hologram Support:** Display the current loot tier above the Vaults using holograms.
- **Cooldown System:** Set cooldowns for Vaults to control when new loot can be injected.
- **NBTAPI Embedded:** VaultHunters already embeds the `NBTAPI` library, eliminating the need for additional dependencies.

## Prerequisites

- **Minecraft Server:** Version 1.21 or higher.
- **Java:** JDK 17 or higher.
- **Maven:** For building the plugin.

## Optional Dependencies

- **DecentHologram:** For displaying holograms above Vaults.

## Installation

1. **Download the Plugin:**
    - Download the latest release of VaultHunters from the [Releases](https://github.com/MysticHorizons-MC/VaultHunters/releases) page.

2. **Install the Plugin:**
    - Place the `VaultHunters.jar` file into your server's `plugins` directory.

3. **Start the Server:**
    - Start your Minecraft server. The plugin will generate configuration files in the `plugins/VaultHunters` directory.

4. **Configure the Plugin:**
    - Customize the `tiers.yml` and other configuration files as needed to define your loot tiers and behaviors.

## Configuration

### tiers.yml

This file allows you to define different loot tiers that can be injected into Vaults. Each tier can have a name, associated items, and a chance of being selected.

```yaml
tiers:
  uncommon:
    name: "&aUncommon"
    file: "uncommon.yml"
    chance: 25.0
    number_of_items: "2-5"
  rare:
    name: "&bRare"
    file: "rare.yml"
    chance: 15.0
    number_of_items: "1-3"
  epic:
    name: "&dEpic"
    file: "epic.yml"
    chance: 10.0
    number_of_items: "1-2"
  legendary:
    name: "&6Legendary"
    file: "legendary.yml"
    chance: 5.0
    number_of_items: "1"
```

### items/*.yml

Each tier file (`uncommon.yml`, `rare.yml`, etc.) contains the items associated with that tier. These items will be injected into Vaults based on the tier selection.

```yaml
items:
  - item:
      type: "DIAMOND_SWORD"
      meta:
        display_name: "&bUncommon Sword"
        enchantments:
          DAMAGE_ALL: 2
        lore:
          - "&7An uncommon sword."
    chance: 20.0
  - item:
      type: "GOLDEN_APPLE"
    chance: 80.0
```

## Usage

Once installed and configured, VaultHunters will automatically handle loot injection into Vaults based on the defined tiers and cooldowns. Players can interact with Vaults as usual, and the plugin will manage the loot tables and injection processes behind the scenes.

### Commands

- `/vaulthunters reload` - Reloads the configuration files.
- `/vaulthunters info` - Displays information about the plugin.
- `/vaulthunters gui` - Opens the GUI for managing Vaults.
- `/vaulthunters bypass` - Bypass the cooldown for all Vaults.

### Permissions

- `vaulthunters.admin` - Access to all VaultHunters commands and permissions.
- `vaulthunters.bypass` - Allows the user to toggle bypass mode.
- `vaulthunters.gui` - Allows the user to open the GUI.
- `vaulthunters.reload` - Allows the user to reload the configuration files.
- `vaulthunters.info` - Allows the user to view plugin information.
- `vaulthunters.use` - Allows the user to interact with Vaults and the use the main command.

## Building from Source

### Requirements

- **Java JDK 17** or higher
- **Maven**

### Steps to Build

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/MysticHorizons-MC/VaultHunters.git
   cd VaultHunters
   ```

2. **Build the Plugin:**

   Use Maven to build the plugin. This will compile the code, run any tests, and package the plugin into a `.jar` file.

   ```bash
   mvn clean package
   ```

3. **Locate the JAR:**

   After the build is complete, the plugin JAR file will be located in the `target` directory.

   ```bash
   ls target/
   VaultHunters-1.0-SNAPSHOT.jar
   ```

4. **Install the Plugin:**

   Copy the generated JAR file into your server's `plugins` directory.

## Contributing

Contributions are welcome! If you'd like to contribute to VaultHunters, please follow these steps:

1. **Fork the Repository**
2. **Create a Feature Branch**
3. **Commit Your Changes**
4. **Push to Your Fork**
5. **Submit a Pull Request**

Make sure your code follows the project's coding standards and is well-documented.

## License

VaultHunters is licensed under the Apache License 2.0 License. See the [LICENSE](LICENSE) file for more details.

## Support

If you encounter any issues or have any questions, feel free to open an issue on the [GitHub Issues](https://github.com/MysticHorizons-MC/VaultHunters/issues) page.