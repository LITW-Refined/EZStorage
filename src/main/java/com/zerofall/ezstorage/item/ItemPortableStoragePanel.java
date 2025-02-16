package com.zerofall.ezstorage.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.gtnewhorizon.gtnhlib.GTNHLib;
import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.enums.PortableStoragePanelTier;
import com.zerofall.ezstorage.network.server.MsgStorage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZInventoryManager;
import com.zerofall.ezstorage.util.EZInventoryReference;

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
    public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int meta,
        float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof TileEntityStorageCore core && itemStack.getItem() instanceof ItemPortableStoragePanel panel) {
            if (!world.isRemote) {
                panel.setInventoryReference(itemStack, core);

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
                    return true;
                }
            }

        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote && itemStackIn.getItem() instanceof ItemPortableStoragePanel panel
            && player instanceof EntityPlayerMP playerMP) {
            boolean found = false;
            boolean inRange = false;

            if (validateReference(itemStackIn)) {
                EZInventoryReference reference = panel.getInventoryReference(itemStackIn);
                EZInventory inventory = EZInventoryManager.getInventory(reference.inventoryId);

                if (reference != null && inventory != null) {
                    found = true;

                    if (isInRange(itemStackIn, reference, playerMP)) {
                        inRange = true;
                        player.openGui(
                            EZStorage.instance,
                            this.getHasCraftingArea(itemStackIn) ? 2 : 1,
                            reference.getWorld(),
                            reference.blockX,
                            reference.blockY,
                            reference.blockZ);
                        EZStorage.instance.network.sendTo(new MsgStorage(inventory), playerMP);
                    }
                }
            }

            if (!found) {
                GTNHLib.proxy.sendMessageAboveHotbar(
                    playerMP,
                    new ChatComponentTranslation("chat.msg.storagecore_not_found"),
                    60,
                    true,
                    true);
            } else if (!inRange) {
                GTNHLib.proxy.sendMessageAboveHotbar(
                    playerMP,
                    new ChatComponentTranslation("chat.msg.storagecore_out_of_range"),
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

    public boolean getHasCraftingArea(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();

        if (tag != null) {
            return tag.getBoolean("hasCraftingArea");
        }

        return false;
    }

    public void setHasCraftingArea(ItemStack itemStack, boolean hasCraftingArea) {
        NBTTagCompound tag = itemStack.getTagCompound();

        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTagCompound(tag);
        }

        tag.setBoolean("hasCraftingArea", hasCraftingArea);
    }

    public EZInventoryReference getInventoryReference(ItemStack itemStack) {
        NBTTagCompound tag = itemStack.getTagCompound();

        if (tag != null && tag.hasKey("reference")) {
            NBTTagCompound tagCore = tag.getCompoundTag("reference");
            String id = tagCore.getString("inventoryId");
            int dimId = tagCore.getInteger("blockDimId");
            int x = tagCore.getInteger("blockX");
            int y = tagCore.getInteger("blockY");
            int z = tagCore.getInteger("blockZ");
            return new EZInventoryReference(id, dimId, x, y, z);
        }

        return null;
    }

    public void setInventoryReference(ItemStack itemStack, TileEntityStorageCore core) {
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
            tagCore.setString("inventoryId", core.getInventory().id);
            tagCore.setInteger("blockDimId", core.getWorldObj().provider.dimensionId);
            tagCore.setInteger("blockX", core.xCoord);
            tagCore.setInteger("blockY", core.yCoord);
            tagCore.setInteger("blockZ", core.zCoord);
            tag.setTag("reference", tagCore);
        }
    }

    public boolean validateReference(ItemStack itemStackPanel) {
        EZInventoryReference reference = this.getInventoryReference(itemStackPanel);
        boolean isValid = false;

        if (reference != null) {
            WorldServer dim = reference.getWorld();

            if (dim != null && dim.blockExists(reference.blockX, reference.blockY, reference.blockZ)) {
                TileEntity te = dim.getTileEntity(reference.blockX, reference.blockY, reference.blockZ);
                if (te instanceof TileEntityStorageCore tecore && tecore.inventoryId.equals(reference.inventoryId)) {
                    isValid = true;
                } else {
                    this.setInventoryReference(itemStackPanel, null);
                }
            }
        }

        return isValid;
    }

    public static boolean isInRange(ItemStack itemStackPanel, EZInventoryReference reference, EntityPlayerMP player) {
        if (!(itemStackPanel.getItem() instanceof ItemPortableStoragePanel panel)) {
            return false;
        }

        PortableStoragePanelTier tier = panel.getTier(itemStackPanel);

        // Check infinity
        if (tier.isInfinity) {
            return true;
        }

        // Check dimension
        if (reference.blockDimId != player.worldObj.provider.dimensionId) {
            return false;
        }

        // Check position
        final double rangeLimit = tier.range * tier.range;
        final double offX = reference.blockX - player.posX;
        final double offY = reference.blockY - player.posY;
        final double offZ = reference.blockZ - player.posZ;
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
