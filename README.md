# EZStorage

Simple storage mod for Minecraft 1.7.10 (Forge).

## Description

EZStorage (alias Easy Storage) introduces an early-game storage system that scales and evolves as players progress. Want to put 100k Cobblestone in 1 slot? No problem. Blocks in the mod can add a crafting grid, additional storage, and more. Also includes integration into some mods for easier crafting or additional features!
 
## Blocks

- **Storage Core**
  - This is the core of your storage system
  - Click on this block to open the GUI (search box included), and add adjacent blocks to expand
  - Each system can only have 1 Storage Core
  - This block can only be broken if it contains no items
- **Storage Box**
  - Tier 1 storage add-on wich increases the storage capacity of the Storage Core by a small amount
- **Condensed Storage Box**
  - Tier 2 storage add-on
- **Hyper Storage Box**
  - Tier 3 storage add-on
- **Proxy Port**
  - Expose the storage inventory to hoppers, conduits or maschines
- **Crafting Box**
  - This adds a crafting grid to the GUI of your Storage Core (compatible with NEI + clicking for easy crafting from the internal inventory)

## Mod Integration

- **Not Enough Items** (GTNH version)
  - Overlay recipes
  - One-click crafting
  - NEI-like search
- **Waila**
  - Advanced tooltip overlay
  - Show storage content (items/types count) in world tooltip
- **JABBA**
  - Move the storage core from one place to another place using the dolly from Jabba
- **Crafting Tweaks**
  - Show typical crafting tweaks buttons on crafting grid
- **Et Futurum Requiem**
  - Spectator mode

## Remarks

This mod is intented to be a compact storage solution, and not an automated storage network. As of right now, I'm not going to include any features like filtered output, network cables, external monitors, or anything else remeniscent of Applied Energistics. If you have an idea how such features would fit nicely in vanilla worlds, feel free to open an issue for discussion. Contribution are also welcome.

## Contribution

If you want to help adding any feature or make improvements or getting compatibility with another mod, feel free to make a pull request. I only have the time to maintenance this mod at a low level, the lowest effort needed for getting it working nicely on my server and in the modpack.

## Changes compared to the latest official EZStorage version

This fork becomes some changes to be usable on servers, less-buggy and a lot of feature and code improvements.

- Many bugfixes and some stability and code improvements
- Lots of UI and performance improvements
- Mod compat with NEI, Waila, Jabba, Crafting Tweaks, Et Futurum Requiem, etc.
- Re-made the most textures to look nicer and fit better into vanilla worlds
- Replaced input and output block with a more enhanced proxy block
- Configurable maximum different item types per storage (no limit by default)
- Store storage as separated file in the world's save directory instead directly on the TileEntity (only send to client when needed)
