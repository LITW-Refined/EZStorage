# EZStorage

Simple storage mod for Minecraft 1.7.10 (Forge).

## Description

EZStorage (alias Easy Storage) introduces an early-game storage system that scales and evolves as players progress. Want to put 100,000 Cobblestone in 1 slot? No problem. Blocks in the mod can add a crafting grid, additional storage, and more. Also includes NEI integration for 1-click crafting from the system's internal inventory!
 
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
  - Advanced tooltip overlay
  - NEI-like search
- **Waila**
  - Show storage content (items/types count) in world tooltip
- **JABBA**
  - Move the storage core from one place to another place using the dolly from Jabba
- **Crafting Tweaks**
  - Show typical crafting tweaks buttons on crafting grid
- **Et Futurum Requiem**
  - Spectator mode

## Remarks

This mod is intented to be a compact storage solution, and not an automated storage network. As of right now, I do not want to include any features like filtered output, network cables, external monitors, wireless access, or anything else remeniscent of Applied Energistics. Any issues created as requests for these features will be immediately closed.

## Contribution

If you want to help adding any feature or make improvements or getting compatibility with another mod, feel free to make a pull request. I only have the time to maintenance this mod at a low level, the lowest effort needed for getting it working nicely on my server and in the modpack.

## Changes to the latest official EZStorage version

This fork becomes some changes to be usable on servers, less-buggy

- Many bugfixes and some stability and code improvements
- Mod compat with Waila, Jabba, Crafting Tweaks, Et Futurum Requiem
- Re-made the most textures to look nicer and fit better into vanilla worlds
- Maximum of 1000 different item types per storage (for stability reasons)

## About the 1000 item types limit

As you know, Applied Energistics 2 has a limit of 63 item types per storage cell. As you can place 10 cells within one storage block, the maximum item types per block is 630. This limit is there for a reason. It itends to limit the NBT data stored at the NBT tag of the tile entity (like the storage box or cell rac).

The system that AE2 and Easy Storage use is the same. Each item type will create a new NBT entry with the item definition and the amount of items stored. While storing an item that hasn't been stored already creates a new NBT entry and with this increases the size of the whole NBT tag, storing an item that already has been storaged just changes the amount of items without changing the NBT tag size.

Some items has more NBT data then others. Having a too large NBT tag might lead to a server crash or makes it impossible to login with a client on the server ever again. That's why there is a limit to make those issues less likely. So, don't increase the number in the config on your own risk! It will not break directly, but if, I'll not be responsible for it.

It is planned to change the storage system to a different kind, like Refined Storage for modern Minecraft versions. It uses a file-bases system which would have its own limit.