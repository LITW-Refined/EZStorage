package com.zerofall.ezstorage.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.enums.PortableStoragePanelTier;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.Method;

@Interface(modid = "Baubles", iface = "baubles.api.expanded.IBaubleExpanded")
public class ItemPortableStoragePanel extends EZItem implements IBaubleExpanded {

    public ItemPortableStoragePanel() {
        super("portable_storage_terminal");
        setMaxStackSize(1);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int meta,
        float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityStorageCore core
                && itemStack.getItem() instanceof ItemPortableStoragePanel panel) {
                panel.setStorageCore(itemStack, core);
                if (player instanceof EntityPlayerMP playerMP) {
                    GTNHLib.proxy.sendMessageAboveHotbar(
                        playerMP,
                        new ChatComponentTranslation(
                            "chat.msg.storagecore_connected",
                            core.xCoord,
                            core.yCoord,
                            core.zCoord),
                        100,
                        true,
                        true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote && itemStackIn.getItem() instanceof ItemPortableStoragePanel panel
            && player instanceof EntityPlayerMP playerMP) {
            TileEntityStorageCore core = panel.getStorageCore(itemStackIn);
            if (core != null) {
                if (isInRange(itemStackIn, core, playerMP)) {
                    player.openGui(
                        EZStorage.instance,
                        core.hasCraftBox ? 2 : 1,
                        core.getWorldObj(),
                        core.xCoord,
                        core.yCoord,
                        core.zCoord);
                } else {
                    GTNHLib.proxy.sendMessageAboveHotbar(
                        playerMP,
                        new ChatComponentTranslation("chat.msg.storagecore_out_of_range"),
                        60,
                        true,
                        true);
                }
            } else {
                GTNHLib.proxy.sendMessageAboveHotbar(
                    playerMP,
                    new ChatComponentTranslation("chat.msg.storagecore_not_found"),
                    60,
                    true,
                    true);
            }
        }
        return itemStackIn;
    }

    public PortableStoragePanelTier getTier(ItemStack stack) {
        return PortableStoragePanelTier.getTierFromMeta(this.getDamage(stack));
    }

    public void setTier(ItemStack itemStack, PortableStoragePanelTier tier) {
        this.setDamage(itemStack, tier.meta);
    }

    public TileEntityStorageCore getStorageCore(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();
        TileEntityStorageCore core = null;

        if (tag != null && tag.hasKey("core")) {
            MinecraftServer server = MinecraftServer.getServer();

            if (server != null) {
                NBTTagCompound tagCore = tag.getCompoundTag("core");
                int dimId = tagCore.getInteger("dim");
                int x = tagCore.getInteger("x");
                int y = tagCore.getInteger("y");
                int z = tagCore.getInteger("z");
                WorldServer dim = null;

                for (WorldServer world : server.worldServers) {
                    if (world.provider.dimensionId == dimId) {
                        dim = world;
                        break;
                    }
                }

                if (dim != null && dim.blockExists(x, y, z)) {
                    TileEntity te = dim.getTileEntity(x, y, z);
                    if (te instanceof TileEntityStorageCore tecore) {
                        core = tecore;
                    }
                }
            }
        }

        return core;
    }

    public void setStorageCore(ItemStack itemStack, TileEntityStorageCore core) {
        if (core == null) {
            NBTTagCompound tag = itemStack.getTagCompound();
            if (tag != null) {
                tag.removeTag("core");
            }
        } else if (core.getWorldObj() != null) {
            NBTTagCompound tag = itemStack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                itemStack.setTagCompound(tag);
            }
            NBTTagCompound tagCore = new NBTTagCompound();
            tagCore.setInteger("dim", core.getWorldObj().provider.dimensionId);
            tagCore.setInteger("x", core.xCoord);
            tagCore.setInteger("y", core.yCoord);
            tagCore.setInteger("z", core.zCoord);
            tag.setTag("core", tagCore);
        }
    }

    public static boolean isInRange(ItemStack itemStackPanel, TileEntityStorageCore core, EntityPlayerMP player) {
        if (!(itemStackPanel.getItem() instanceof ItemPortableStoragePanel panel)) {
            return false;
        }

        PortableStoragePanelTier tier = panel.getTier(itemStackPanel);

        // Check infinity
        if (tier.isInfinity) {
            return true;
        }

        // Check dimension
        if (core.getWorldObj().provider.dimensionId != player.worldObj.provider.dimensionId) {
            return false;
        }

        // Check position
        final double rangeLimit = tier.range * tier.range;
        final double offX = core.xCoord - player.posX;
        final double offY = core.yCoord - player.posY;
        final double offZ = core.zCoord - player.posZ;
        final double r = offX * offX + offY * offY + offZ * offZ;
        if (r > rangeLimit) {
            return false;
        }

        return true;
    }

    @Override
    @Method(modid = "Baubles")
    public boolean canEquip(ItemStack itemStack, EntityLivingBase player) {
        return true;
    }

    @Override
    @Method(modid = "Baubles")
    public boolean canUnequip(ItemStack itemStack, EntityLivingBase player) {
        return true;
    }

    @Override
    @Method(modid = "Baubles")
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.AMULET;
    }

    @Override
    @Method(modid = "Baubles")
    public void onEquipped(ItemStack itemStack, EntityLivingBase player) {
        // Nothing todo here.
    }

    @Override
    @Method(modid = "Baubles")
    public void onUnequipped(ItemStack itemStack, EntityLivingBase player) {
        // Nothing todo here.
    }

    @Override
    @Method(modid = "Baubles")
    public void onWornTick(ItemStack itemStack, EntityLivingBase player) {
        // Nothing todo here.
    }

    @Override
    @Method(modid = "Baubles")
    public String[] getBaubleTypes(ItemStack itemStack) {
        return new String[] { BaubleExpandedSlots.universalType };
    }

    @Method(modid = "Baubles")
    public static ItemStack getFromBaubles(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return null;
        }

        IInventory baubles = BaublesApi.getBaubles((EntityPlayer) entity);
        int invSize = baubles.getSizeInventory();
        for (int i = 0; i < invSize; i++) {
            ItemStack ring = baubles.getStackInSlot(i);
            if (ring != null && ring.getItem() instanceof ItemPortableStoragePanel) {
                return ring;
            }
        }

        return null;
    }
}
