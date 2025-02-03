# EZStorage

Simple storage mod for Minecraft 1.7.10 (Forge).

## Description

EZStorage (alias Easy Storage) introduces an early-game storage system that scales and evolves as players progress. Want to put 100,000 Cobblestone in 1 slot? No problem. Blocks in the mod can add a crafting grid, additional storage, and more. Also includes integration into some mods easier crafting or additional features!
 
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

This mod is intented to be a compact storage solution, and not an automated storage network. As of right now, I do not want to include any features like filtered output, network cables, external monitors, wireless access, or anything else remeniscent of Applied Energistics. Any issues created as requests for these features will be immediately closed.

## Contribution

If you want to help adding any feature or make improvements or getting compatibility with another mod, feel free to make a pull request. I only have the time to maintenance this mod at a low level, the lowest effort needed for getting it working nicely on my server and in the modpack.

## Changes compared to the latest official EZStorage version

This fork becomes some changes to be usable on servers, less-buggy and a lot of feature and code improvements.

- Many bugfixes and some stability and code improvements
- Lots of UI and performance improvements
- Mod compat with NEI, Waila, Jabba, Crafting Tweaks, Et Futurum Requiem, etc.
- Re-made the most textures to look nicer and fit better into vanilla worlds
- Replaced input and output block with a more enhanced proxy block
- Configurable maximum different item types per storage (10000 by default)

## About the item types limit

### The issue explained

As you know, Applied Energistics 2 has a limit of 63 item types per storage cell. As you can place 10 cells within one storage block, the maximum item types per block is 630. This limit is there for a reason. It itends to limit the NBT data stored at the NBT tag of the tile entity (like the storage box or cell rac).

The system that AE2 and Easy Storage use is the same. Each item type will create a new NBT entry with the item definition and the amount of items stored. While storing an item that hasn't been stored already creates a new NBT entry and with this increases the size of the whole NBT tag, storing an item that already has been storaged just changes the amount of items without changing the NBT tag size.

You probably know, the world is splitted into many small chunks (16 x 16 x 256). The server sends each chunk seperately to the client. So, each chunk will be sent in its own network packet. Now imagen If the client then have a big storage (chests, AE2 server racks, storage box) that have many many many items, the size of the chunk will grow up to 2 MiB, which is the maximum possible size of a Minecraft network packet in vanilla.

Some items have more NBT data then others. Having a too large NBT tag might lead to a server crash or makes it impossible to login with a client on the server ever again. That's why there is a limit to make those issues less likely. So, don't increase the number in the config on your own risk! It will not break directly (especially if you use mods to increase the network packet size limit), but if, I'll not be responsible for it.

### Solutions

Mods like [Hodgepodge](https://github.com/GTNewHorizons/Hodgepodge) increases the maximum size of those network packets. While default is 2 MiB, Hodgepodge increases it to 256 MiB by default. This helps mods and even vanilla to never reach the maximum ever again.

Independent of this, it is planned to change the storage system to a different kind, like Refined Storage for modern Minecraft versions. It'll uses a file-based system then. The inventory of a storage box will then only be sent to the client when the GUI is open. As this action will get its own network packet tt can use the full network packet size.