package com.zerofall.ezstorage.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.network.client.MsgPullBookmarkItems;

import codechicken.nei.api.IBookmarkContainerHandler;

/** Wires NEI's bookmark autocraft (Shift+C) into EZStorage's storage core / crafting terminal GUIs. */
public class EZStorageBookmarkContainerHandler implements IBookmarkContainerHandler {

    @Override
    public List<ItemStack> getStorageStacks(GuiContainer guiContainer) {
        if (!(guiContainer instanceof GuiStorageCore gui)) {
            return Collections.emptyList();
        }
        return gui.copyStorageStacks();
    }

    @Override
    public void pullBookmarkItemsFromContainer(GuiContainer guiContainer, ArrayList<ItemStack> bookmarkItems) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : bookmarkItems) {
            if (stack == null || stack.stackSize <= 0) {
                continue;
            }
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            list.appendTag(tag);
        }
        NBTTagCompound wrapper = new NBTTagCompound();
        wrapper.setTag("items", list);
        EZStorage.instance.network.sendToServer(new MsgPullBookmarkItems(wrapper));
    }
}
