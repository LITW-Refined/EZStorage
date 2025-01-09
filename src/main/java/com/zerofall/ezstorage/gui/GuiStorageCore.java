package com.zerofall.ezstorage.gui;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.network.MyMessage;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import com.zerofall.ezstorage.util.EZItemRenderer;
import com.zerofall.ezstorage.util.ItemGroup;

public class GuiStorageCore extends GuiContainer {

    TileEntityStorageCore tileEntity;
    EZItemRenderer ezRenderer;
    int scrollRow = 0;
    private boolean isScrolling = false;
    private boolean wasClicking = false;
    private static final ResourceLocation creativeInventoryTabs = new ResourceLocation(
        "textures/gui/container/creative_inventory/tabs.png");
    private static final ResourceLocation searchBar = new ResourceLocation(
        "textures/gui/container/creative_inventory/tab_item_search.png");
    private float currentScroll;
    private GuiTextField searchField;
    private List<ItemGroup> filteredList;
    private ItemStack mouseOverItem;
    protected List<GuiButton> extraButtons;

    @Override
    public void initGui() {
        super.initGui();
        this.searchField = new GuiTextField(
            this.fontRendererObj,
            this.guiLeft + 10,
            this.guiTop + 6,
            80,
            this.fontRendererObj.FONT_HEIGHT);
        this.searchField.setMaxStringLength(20);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setCanLoseFocus(true);
        this.searchField.setFocused(true);
        this.searchField.setText("");
        filteredList = new ArrayList<ItemGroup>(this.tileEntity.inventory.inventory);
        extraButtons = new ArrayList<GuiButton>();
    }

    public GuiStorageCore(EntityPlayer player, World world, int x, int y, int z) {
        this(new ContainerStorageCore(player, world, x, y, z), world, x, y, z);
    }

    public GuiStorageCore(ContainerStorageCore containerStorageCore, World world, int x, int y, int z) {
        super(containerStorageCore);
        this.tileEntity = (TileEntityStorageCore) world.getTileEntity(x, y, z);
        this.xSize = 195;
        this.ySize = 222;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.mc.renderEngine.bindTexture(getBackground());
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
        this.searchField.setVisible(true);
        this.mc.renderEngine.bindTexture(searchBar);
        drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 4, 80, 4, 90, 12);
        this.searchField.drawTextBox();
        for (GuiButton button : extraButtons) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        handleScrolling(mouseX, mouseY);
        // updateFilteredItems();
        DecimalFormat formatter = new DecimalFormat("#,###");
        String totalCount = formatter.format(this.tileEntity.inventory.getTotalCount());
        String max = formatter.format(this.tileEntity.inventory.maxItems);
        String amount = totalCount + "/" + max;
        // Right-align text
        int stringWidth = fontRendererObj.getStringWidth(amount);

        // Scale down text if its too large
        if (stringWidth > 88) {
            float ScaleFactor = 0.7f;
            float RScaleFactor = 1.0f / ScaleFactor;
            GL11.glPushMatrix();
            GL11.glScaled(ScaleFactor, ScaleFactor, ScaleFactor);
            int X = (int) (((float) 187 - stringWidth * ScaleFactor) * RScaleFactor);
            fontRendererObj.drawString(amount, X, 10, 4210752);
            GL11.glPopMatrix();
        } else {
            fontRendererObj.drawString(amount, 187 - stringWidth, 6, 4210752);
        }

        int x = 8;
        int y = 18;
        this.zLevel = 100.0F;
        itemRender.zLevel = 100.0F;
        if (this.ezRenderer == null) {
            this.ezRenderer = new EZItemRenderer();
        }
        this.ezRenderer.zLevel = 100.0F;

        boolean finished = false;
        for (int i = 0; i < this.rowsVisible(); i++) {
            x = 8;
            for (int j = 0; j < 9; j++) {
                int index = (i * 9) + j;
                index = scrollRow * 9 + index;
                if (index >= this.filteredList.size()) {
                    finished = true;
                    break;
                }

                ItemGroup group = this.filteredList.get(index);
                ItemStack stack = group.itemStack;
                FontRenderer font = null;
                if (stack != null) font = stack.getItem()
                    .getFontRenderer(stack);
                if (font == null) font = fontRendererObj;
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, x, y);
                ezRenderer.renderItemOverlayIntoGUI(font, stack, x, y, "" + group.count);
                x += 18;
            }
            if (finished) {
                break;
            }
            y += 18;
        }

        int i1 = 175;
        int k = 18;
        int l = k + 108;
        this.mc.getTextureManager()
            .bindTexture(creativeInventoryTabs);
        this.drawTexturedModalRect(i1, k + (int) ((float) (l - k - 17) * this.currentScroll), 232, 0, 12, 15);
        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        cacheMouseOverItem(mouseX, mouseY);
    }

    protected void cacheMouseOverItem(int mouseX, int mouseY) {
        Integer slot = getSlotAt(mouseX, mouseY);

        if (slot != null) {
            if (slot < this.filteredList.size()) {
                ItemGroup group = this.filteredList.get(slot);

                if (group != null) {
                    mouseOverItem = group.itemStack;
                    return;
                }
            }
        }

        mouseOverItem = null;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!this.checkHotbarKeys(keyCode)) {
            if (this.searchField.isFocused() && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
                updateFilteredItems();
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
        if (keyCode == 63) {
            this.tileEntity.sortInventory();
        }
    }

    private void updateFilteredItems() {
        filteredList = new ArrayList<ItemGroup>(this.tileEntity.inventory.inventory);
        Iterator<ItemGroup> iterator = this.filteredList.iterator();
        String s1 = this.searchField.getText()
            .toLowerCase();

        while (iterator.hasNext()) {
            ItemGroup group = (ItemGroup) iterator.next();
            ItemStack itemstack = group.itemStack;
            boolean flag = false;
            Iterator<?> iterator1 = itemstack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips)
                .iterator();

            while (true) {
                if (iterator1.hasNext()) {
                    String s = (String) iterator1.next();

                    if (!EnumChatFormatting.getTextWithoutFormattingCodes(s)
                        .toLowerCase()
                        .contains(s1)) {
                        continue;
                    }

                    flag = true;
                }

                if (!flag) {
                    iterator.remove();
                }

                break;
            }
        }
    }

    private void handleScrolling(int mouseX, int mouseY) {
        boolean flag = Mouse.isButtonDown(0);

        int k = this.guiLeft;
        int l = this.guiTop;
        int i1 = k + 175;
        int j1 = l + 18;
        int k1 = i1 + 14;
        int l1 = j1 + 108;

        if (!this.wasClicking && flag && mouseX >= i1 && mouseY >= j1 && mouseX < k1 && mouseY < l1) {
            this.isScrolling = true;
        }

        if (!flag) {
            this.isScrolling = false;
        }

        this.wasClicking = flag;

        if (this.isScrolling) {
            this.currentScroll = ((float) (mouseY - j1) - 7.5F) / ((float) (l1 - j1) - 15.0F);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            scrollTo(this.currentScroll);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Integer slot = getSlotAt(mouseX, mouseY);
        if (slot != null) {
            int mode = 0;
            if (GuiScreen.isShiftKeyDown()) {
                mode = 1;
            }
            int index = this.tileEntity.inventory.slotCount();
            if (slot < this.filteredList.size()) {
                ItemGroup group = this.filteredList.get(slot);
                if (group != null) {
                    index = this.tileEntity.inventory.inventory.indexOf(group);
                    if (index < 0) {
                        return;
                    }
                }
            }
            EZStorage.instance.networkWrapper.sendToServer(new MyMessage(index, mouseButton, mode));
            ContainerStorageCore container = (ContainerStorageCore) this.inventorySlots;
            container.customSlotClick(index, mouseButton, mode, this.mc.thePlayer);
        } else {
            int elementX = this.searchField.xPosition;
            int elementY = this.searchField.yPosition;
            if (mouseX >= elementX && mouseX <= elementX + this.searchField.width
                && mouseY >= elementY
                && mouseY <= elementY + this.searchField.height) {
                if (mouseButton == 1 || GuiScreen.isShiftKeyDown()) {
                    this.searchField.setText("");
                }
                this.searchField.setFocused(true);
            } else {
                this.searchField.setFocused(false);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private Integer getSlotAt(int x, int y) {
        int startX = this.guiLeft + 8 - 1;
        int startY = this.guiTop + 18 - 1;

        int clickedX = x - startX;
        int clickedY = y - startY;

        if (clickedX > 0 && clickedY > 0) {
            int column = clickedX / 18;
            if (column < 9) {
                int row = clickedY / 18;
                if (row < this.rowsVisible()) {
                    int slot = (row * 9) + column + (scrollRow * 9);
                    return slot;
                }
            }
        }
        return null;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            int j = this.tileEntity.inventory.slotCount() / 9 - this.rowsVisible() + 1;

            if (i > 0) {
                i = 1;
            }

            if (i < 0) {
                i = -1;
            }

            this.currentScroll = (float) ((double) this.currentScroll - (double) i / (double) j);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            scrollTo(this.currentScroll);
        }

    }

    @Override
    protected void handleMouseClick(Slot p_146984_1_, final int p_146984_2_, final int p_146984_3_,
        final int p_146984_4_) {
        super.handleMouseClick(p_146984_1_, p_146984_2_, p_146984_3_, p_146984_4_);
        updateFilteredItems();
    }

    private void scrollTo(float scroll) {
        int i = (this.tileEntity.inventory.slotCount() + 8) / 9 - this.rowsVisible();
        int j = (int) ((double) (scroll * (float) i) + 0.5D);
        if (j < 0) {
            j = 0;
        }
        this.scrollRow = j;
    }

    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageScrollGui.png");
    }

    public int rowsVisible() {
        return 6;
    }

    public ItemStack getMouseOverItem() {
        return mouseOverItem;
    }
}
