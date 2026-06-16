package com.zerofall.ezstorage.nei;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.gui.GuiCraftingCore;
import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.network.client.MsgAutoCraft;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventory;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton.ItemOverlayState;
import codechicken.nei.recipe.IRecipeHandler;

public class NeiCraftingOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex,
        final boolean maxTransfer) {
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        overlayRecipe(gui, ingredients);
    }

    public void overlayRecipe(final GuiContainer gui, final List<PositionedStack> ingredients) {
        if (!(gui instanceof GuiCraftingCore)) {
            return;
        }
        final NBTTagCompound recipeNBT = buildRecipeNBT(gui, ingredients);
        EZStorage.instance.network.sendToServer(new MsgReqCrafting(recipeNBT));
    }

    @Override
    public int transferRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        overlayRecipe(firstGui, recipe, recipeIndex, multiplier != 1);
        return 1;
    }

    @Override
    public boolean canCraft(GuiContainer firstGui, IRecipeHandler handler, int recipeIndex) {
        List<ItemOverlayState> states = presenceOverlay(firstGui, handler, recipeIndex);
        return states.stream()
            .allMatch(ItemOverlayState::isPresent);
    }

    @Override
    public boolean craft(GuiContainer firstGui, IRecipeHandler handler, int recipeIndex, int multiplier) {
        if (!(firstGui instanceof GuiCraftingCore)) {
            return false;
        }

        GuiCraftingCore gui = (GuiCraftingCore) firstGui;
        EZInventory inventory = gui.getInventory();
        if (inventory == null) {
            return false;
        }

        List<PositionedStack> ingredients = handler.getIngredientStacks(recipeIndex);
        NBTTagCompound recipeNBT = buildRecipeNBT(firstGui, ingredients);

        ItemStack[] playerInv = gui.mc.thePlayer.inventory.mainInventory;
        int maxCrafts = calculateMaxCrafts(inventory, playerInv, ingredients, multiplier);
        if (maxCrafts <= 0) {
            return false;
        }

        deductIngredientsFromClient(inventory, playerInv, ingredients, maxCrafts);

        PositionedStack resultStack = handler.getResultStack(recipeIndex);
        if (resultStack != null && resultStack.items != null && resultStack.items.length > 0) {
            int perCraft = resultStack.items[0].stackSize;
            if (perCraft <= 0) perCraft = 1;
            ItemStack result = resultStack.items[0].copy();
            result.stackSize = perCraft * maxCrafts;
            inventory.input(result);
        }

        EZStorage.instance.network.sendToServer(new MsgAutoCraft(recipeNBT, maxCrafts));
        return true;
    }

    @Override
    public List<ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        List<ItemStack> invStacks = new ArrayList<ItemStack>();

        if (firstGui instanceof GuiStorageCore coreGui) {
            EZInventory inventory = coreGui.getInventory();
            if (inventory != null) {
                for (ItemStack stack : new ArrayList<>(inventory.inventory)) {
                    invStacks.add(stack.copy());
                }
            }
        }

        invStacks
            .addAll(getFromInventory(firstGui.mc.thePlayer.inventoryContainer.inventorySlots, firstGui.mc.thePlayer));

        final List<ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);

        for (PositionedStack stack : ingredients) {
            Optional<ItemStack> used = invStacks.stream()
                .filter(is -> is.stackSize > 0 && stack.contains(is))
                .findAny();

            itemPresenceSlots.add(new ItemOverlayState(stack, used.isPresent()));

            if (used.isPresent()) {
                ItemStack is = used.get();
                is.stackSize -= 1;
            }
        }

        return itemPresenceSlots;
    }

    private NBTTagCompound buildRecipeNBT(GuiContainer gui, List<PositionedStack> ingredients) {
        final NBTTagCompound recipe = new NBTTagCompound();

        for (final PositionedStack positionedStack : ingredients) {
            if (positionedStack == null || positionedStack.items == null || positionedStack.items.length == 0) {
                continue;
            }

            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;

            for (final Slot slot : gui.inventorySlots.inventorySlots) {
                if (!(slot.inventory instanceof InventoryCrafting) || slot.getSlotIndex() != col + row * 3) {
                    continue;
                }

                final NBTTagList tags = new NBTTagList();
                final List<ItemStack> list = new LinkedList<ItemStack>();

                for (int x = 0; x < positionedStack.items.length; x++) {
                    list.add(positionedStack.items[x]);
                }

                for (final ItemStack is : list) {
                    final NBTTagCompound tag = new NBTTagCompound();
                    is.writeToNBT(tag);
                    tags.appendTag(tag);
                }

                recipe.setTag("#" + slot.getSlotIndex(), tags);
                break;
            }
        }

        return recipe;
    }

    private int calculateMaxCrafts(EZInventory inventory, ItemStack[] playerInv, List<PositionedStack> ingredients,
        int maxMultiplier) {
        List<ItemStack> snapshot = new ArrayList<>(inventory.inventory);
        for (ItemStack stack : playerInv) {
            if (stack != null && stack.stackSize > 0) {
                snapshot.add(stack.copy());
            }
        }
        int size = snapshot.size();
        int[] tempCounts = new int[size];
        int[] toolDurability = new int[size];
        for (int i = 0; i < size; i++) {
            ItemStack s = snapshot.get(i);
            tempCounts[i] = s.stackSize;
            if (isGTTool(s)) {
                toolDurability[i] = (int) Math.min(Integer.MAX_VALUE, getGTDurability(s));
            } else if (isReusableTool(s)) {
                toolDurability[i] = s.getMaxDamage() - s.getItemDamage();
            } else {
                toolDurability[i] = -1;
            }
        }

        for (int crafts = 0; crafts < maxMultiplier; crafts++) {
            for (PositionedStack ps : ingredients) {
                if (ps == null || ps.items == null || ps.items.length == 0) continue;

                boolean found = false;
                for (ItemStack candidate : ps.items) {
                    if (candidate == null) continue;
                    int needed = candidate.stackSize > 0 ? candidate.stackSize : 1;

                    for (int i = 0; i < size; i++) {
                        ItemStack stored = snapshot.get(i);
                        if (!ContainerStorageCoreCrafting.isRecipeItemValid(candidate, stored)) continue;

                        if (toolDurability[i] >= 0) {
                            if (toolDurability[i] >= needed) {
                                toolDurability[i] -= needed;
                                found = true;
                                break;
                            }
                        } else if (tempCounts[i] >= needed) {
                            tempCounts[i] -= needed;
                            found = true;
                            break;
                        }
                    }
                    if (found) break;
                }
                if (!found) return crafts;
            }
        }
        return maxMultiplier;
    }

    private void deductIngredientsFromClient(EZInventory inventory, ItemStack[] playerInv,
        List<PositionedStack> ingredients, int count) {
        for (int c = 0; c < count; c++) {
            for (PositionedStack ps : ingredients) {
                if (ps == null || ps.items == null || ps.items.length == 0) continue;

                for (ItemStack candidate : ps.items) {
                    if (candidate == null) continue;
                    int needed = candidate.stackSize > 0 ? candidate.stackSize : 1;

                    if (deductFromStorage(inventory, candidate, needed)) break;
                    if (deductFromPlayerInventory(playerInv, candidate, needed)) break;
                    break;
                }
            }
        }
    }

    private static boolean deductFromStorage(EZInventory inventory, ItemStack candidate, int needed) {
        for (int i = 0; i < inventory.inventory.size(); i++) {
            ItemStack stored = inventory.inventory.get(i);
            if (!ContainerStorageCoreCrafting.isRecipeItemValid(candidate, stored)) continue;

            if (isGTTool(stored)) {
                incrementGTDamage(stored, needed);
                if (getGTDurability(stored) <= 0) {
                    inventory.inventory.remove(i);
                }
            } else if (isReusableTool(stored)) {
                stored.setItemDamage(stored.getItemDamage() + needed);
                if (stored.getItemDamage() > stored.getMaxDamage()) {
                    inventory.inventory.remove(i);
                }
            } else if (isContainerSwitchItem(stored)) {
                ItemStack container = stored.getItem()
                    .getContainerItem(stored);
                stored.stackSize -= needed;
                if (stored.stackSize <= 0) {
                    inventory.inventory.remove(i);
                }
                if (container != null) {
                    for (int j = 0; j < needed; j++) {
                        inventory.input(container.copy());
                    }
                }
            } else {
                stored.stackSize -= needed;
                if (stored.stackSize <= 0) {
                    inventory.inventory.remove(i);
                }
            }
            inventory.setHasChanges();
            return true;
        }
        return false;
    }

    private static boolean deductFromPlayerInventory(ItemStack[] playerInv, ItemStack candidate, int needed) {
        for (int i = 0; i < playerInv.length; i++) {
            ItemStack stack = playerInv[i];
            if (stack == null || !ContainerStorageCoreCrafting.isRecipeItemValid(candidate, stack)) continue;

            if (isGTTool(stack)) {
                incrementGTDamage(stack, needed);
                if (getGTDurability(stack) <= 0) {
                    playerInv[i] = null;
                }
            } else if (isReusableTool(stack)) {
                stack.setItemDamage(stack.getItemDamage() + needed);
                if (stack.getItemDamage() > stack.getMaxDamage()) {
                    playerInv[i] = null;
                }
            } else if (isContainerSwitchItem(stack)) {
                ItemStack container = stack.getItem()
                    .getContainerItem(stack);
                stack.stackSize -= needed;
                if (stack.stackSize <= 0) {
                    playerInv[i] = null;
                }
                if (container != null) {
                    for (int j = 0; j < needed; j++) {
                        mergeIntoPlayerInventory(playerInv, container.copy());
                    }
                }
            } else {
                stack.stackSize -= needed;
                if (stack.stackSize <= 0) {
                    playerInv[i] = null;
                }
            }
            return true;
        }
        return false;
    }

    private static void mergeIntoPlayerInventory(ItemStack[] playerInv, ItemStack toAdd) {
        for (int i = 0; i < playerInv.length; i++) {
            ItemStack slot = playerInv[i];
            if (slot != null && slot.isItemEqual(toAdd)
                && ItemStack.areItemStackTagsEqual(slot, toAdd)
                && slot.stackSize < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.stackSize;
                int add = Math.min(toAdd.stackSize, space);
                slot.stackSize += add;
                toAdd.stackSize -= add;
                if (toAdd.stackSize <= 0) return;
            }
        }
        for (int i = 0; i < playerInv.length; i++) {
            if (playerInv[i] == null) {
                playerInv[i] = toAdd;
                return;
            }
        }
    }

    private static boolean isReusableTool(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        if (isGTTool(stack)) return true;
        if (!stack.getItem()
            .hasContainerItem(stack)) return false;
        ItemStack container = stack.getItem()
            .getContainerItem(stack);
        return container != null && container.getItem() == stack.getItem() && container.isItemStackDamageable();
    }

    private static boolean isContainerSwitchItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        if (isGTTool(stack)) return false;
        if (!stack.getItem()
            .hasContainerItem(stack)) return false;
        ItemStack container = stack.getItem()
            .getContainerItem(stack);
        return container != null && container.getItem() != stack.getItem();
    }

    public static boolean isGTTool(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return false;
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("GT.ToolStats");
    }

    public static long getGTDurability(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("GT.ToolStats")) return 0;
        NBTTagCompound stats = tag.getCompoundTag("GT.ToolStats");
        return stats.getLong("MaxDamage") - stats.getLong("Damage");
    }

    public static void incrementGTDamage(ItemStack stack, int amount) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("GT.ToolStats")) return;
        NBTTagCompound stats = tag.getCompoundTag("GT.ToolStats");
        stats.setLong("Damage", stats.getLong("Damage") + amount);
    }

    private List<ItemStack> getFromInventory(List<Slot> inventorySlots, EntityClientPlayerMP thePlayer) {
        return inventorySlots.stream()
            .filter(
                s -> s != null && s.getStack() != null
                    && s.getStack().stackSize > 0
                    && s.isItemValid(s.getStack())
                    && s.canTakeStack(thePlayer))
            .map(
                s -> s.getStack()
                    .copy())
            .collect(Collectors.toCollection(ArrayList::new));
    }

}
