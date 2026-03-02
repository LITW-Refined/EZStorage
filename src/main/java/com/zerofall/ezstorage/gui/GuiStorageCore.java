package com.zerofall.ezstorage.gui;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.container.ContainerStorageCore;
import com.zerofall.ezstorage.enums.SearchMode;
import com.zerofall.ezstorage.enums.SortMode;
import com.zerofall.ezstorage.enums.SortOrder;
import com.zerofall.ezstorage.integration.ModIds;
import com.zerofall.ezstorage.network.client.MsgInvSlotClicked;
import com.zerofall.ezstorage.util.EZInventory;
import com.zerofall.ezstorage.util.EZItemRenderer;
import com.zerofall.ezstorage.util.ItemStackCountComparator;
import com.zerofall.ezstorage.util.ItemStackModComparator;
import com.zerofall.ezstorage.util.ItemStackNameComparator;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.SearchField;
import codechicken.nei.api.ItemFilter;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.registry.GameData;

// maybe let the AI comment this code
// if not good luck
public class GuiStorageCore extends GuiContainer {

    protected static final ResourceLocation creativeInventoryTabs = new ResourceLocation(
        "textures/gui/container/creative_inventory/tabs.png");
    protected static final ResourceLocation searchBar = new ResourceLocation(
        "textures/gui/container/creative_inventory/tab_item_search.png");

    protected static String searchText = "";
    protected static SearchMode currentSearchMode = SearchMode.AUTO;
    protected static SortMode currentSortMode = SortMode.AMOUNT;
    protected static SortOrder currentSortOrder = SortOrder.DESCENDING;
    protected static boolean saveSearch = false;

    private static final int BTN_W = 16;
    private static final int BTN_H = 16;
    private static final int BTN_STRIDE = 20;

    private static final int BTN_SORT_MODE = 0;
    private static final int BTN_SORT_ORDER = 1;
    private static final int BTN_SEARCH_MODE = 2;
    private static final int BTN_SAVE_SEARCH = 3;

    private static final class SideButton {

        final int id;
        int x;
        int y;

        SideButton(int id) {
            this.id = id;
        }

        boolean contains(int mx, int my) {
            return mx >= x && mx < x + BTN_W && my >= y && my < y + BTN_H;
        }
    }

    private final SideButton[] sideButtons = { new SideButton(BTN_SORT_MODE), new SideButton(BTN_SORT_ORDER),
        new SideButton(BTN_SEARCH_MODE), new SideButton(BTN_SAVE_SEARCH) };

    protected EZItemRenderer ezRenderer;
    protected int scrollRow = 0;
    protected float currentScroll;
    protected boolean isScrolling = false;
    protected boolean wasClicking = false;
    protected GuiTextField searchField;
    protected ItemStack mouseOverItem;
    protected List<ItemStack> filteredList = new ArrayList<ItemStack>();
    protected LocalDateTime inventoryUpdateTimestamp;
    protected boolean needFullUpdate;

    private boolean guiWasOpen = false;

    private String lastNeiText = "";

    public GuiStorageCore(EntityPlayer player, World world, int x, int y, int z) {
        this(new ContainerStorageCore(player), world, x, y, z);
    }

    public GuiStorageCore(ContainerStorageCore containerStorageCore, World world, int x, int y, int z) {
        super(containerStorageCore);
        this.xSize = 195;
        this.ySize = 222;
        loadSettingsFromConfig();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.searchField = new GuiTextField(
            this.fontRendererObj,
            this.guiLeft + 10,
            this.guiTop + 6,
            100,
            this.fontRendererObj.FONT_HEIGHT);
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setCanLoseFocus(true);

        // Restore search text only if saveSearch is enabled
        if (saveSearch && !searchText.isEmpty()) {
            this.searchField.setText(searchText);
        } else {
            this.searchField.setText("");
            if (!saveSearch) searchText = "";
        }

        if (!guiWasOpen) {
            boolean shouldFocus = (currentSearchMode == SearchMode.AUTO || currentSearchMode == SearchMode.NEI_SYNC);
            this.searchField.setFocused(shouldFocus);
            guiWasOpen = true;
        }

        int bx = this.guiLeft - BTN_W - 2;
        int by = this.guiTop + 8;
        for (int i = 0; i < sideButtons.length; i++) {
            sideButtons[i].x = bx;
            sideButtons[i].y = by + i * BTN_STRIDE;
        }

        updateFilteredItems(true);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (saveSearch) {
            searchText = this.searchField.getText()
                .trim();
        }
        saveSettingsToConfig();
    }

    public EZInventory getInventory() {
        return ((ContainerStorageCore) inventorySlots).inventory;
    }

    public boolean isOverTextField(int mousex, int mousey) {
        int fx = this.searchField.xPosition;
        int fy = this.searchField.yPosition;
        int fw = this.searchField.width;
        int fh = this.searchField.height + 4;
        return mousex >= fx && mousex < fx + fw && mousey >= fy && mousey < fy + fh;
    }

    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        if (displayName != null && !displayName.isEmpty()) {
            this.searchField.setText(displayName);
            searchText = displayName;
            this.searchField.setFocused(true);
            currentScroll = 0;
            scrollRow = 0;
            updateFilteredItems(true);
            if (ModIds.NEI.isLoaded()
                && (currentSearchMode == SearchMode.NEI_SYNC || currentSearchMode == SearchMode.NEI_STANDARD)) {
                setNeiSearchText(displayName);
                lastNeiText = displayName;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Button labels & tooltips
    // -------------------------------------------------------------------------

    private String getLabelFor(int btnId) {
        switch (btnId) {
            case BTN_SORT_MODE:
                switch (currentSortMode) {
                    case NAME:
                        return "Nm";
                    case MOD:
                        return "Md";
                    default:
                        return "#";
                }
            case BTN_SORT_ORDER:
                return currentSortOrder == SortOrder.ASCENDING ? "/\\" : "\\/";
            case BTN_SEARCH_MODE:
                switch (currentSearchMode) {
                    case AUTO:
                        return "Au";
                    case NEI_SYNC:
                        return "NS";
                    case NEI_STANDARD:
                        return "NSt";
                    default:
                        return "St";
                }
            case BTN_SAVE_SEARCH:
                return "Sv";
            default:
                return "?";
        }
    }

    private String getTooltipFor(int btnId) {
        switch (btnId) {
            case BTN_SORT_MODE:
                return StatCollector.translateToLocal(currentSortMode.langKey);
            case BTN_SORT_ORDER:
                return StatCollector.translateToLocal(currentSortOrder.langKey);
            case BTN_SEARCH_MODE:
                return StatCollector.translateToLocal(currentSearchMode.langKey);
            case BTN_SAVE_SEARCH:
                return StatCollector.translateToLocal(
                    saveSearch ? "hud.msg.ezstorage.savesearch.on" : "hud.msg.ezstorage.savesearch.off");
            default:
                return "";
        }
    }

    private boolean isActive(int btnId) {
        if (btnId == BTN_SAVE_SEARCH) return saveSearch;
        return true;
    }

    private void handleButtonClick(SideButton btn) {
        switch (btn.id) {
            case BTN_SORT_MODE:
                currentSortMode = currentSortMode.next();
                saveSettingsToConfig();
                updateFilteredItems(true);
                break;
            case BTN_SORT_ORDER:
                currentSortOrder = currentSortOrder.next();
                saveSettingsToConfig();
                updateFilteredItems(true);
                break;
            case BTN_SEARCH_MODE:
                currentSearchMode = currentSearchMode.next();
                saveSettingsToConfig();
                updateFilteredItems(true);
                break;
            case BTN_SAVE_SEARCH:
                saveSearch = !saveSearch;
                if (!saveSearch) {
                    searchText = "";
                } else {
                    // Capture current text immediately when enabling save search
                    searchText = this.searchField.getText()
                        .trim();
                }
                saveSettingsToConfig();
                break;
            default:
                break;
        }
        // Play click sound
        this.mc.getSoundHandler()
            .playSound(
                net.minecraft.client.audio.PositionedSoundRecord
                    .func_147674_a(new ResourceLocation("gui.button.press"), 1.0f));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        cacheMouseOverItem(mouseX, mouseY);
        drawSideButtonTooltips(mouseX, mouseY);
    }

    private void drawSideButtons(int mouseX, int mouseY) {
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);

        for (SideButton btn : sideButtons) {
            boolean hovered = btn.contains(mouseX, mouseY);
            boolean active = isActive(btn.id);

            int bg = active ? (hovered ? 0xFF6699CC : 0xFF4477AA) : (hovered ? 0xFFAAAAAA : 0xFF888888);
            drawRect(btn.x, btn.y, btn.x + BTN_W, btn.y + BTN_H, bg);

            int border = hovered ? 0xFFFFFFFF : 0xFF222222;
            drawHorizontalLine(btn.x, btn.x + BTN_W - 1, btn.y, border);
            drawHorizontalLine(btn.x, btn.x + BTN_W - 1, btn.y + BTN_H - 1, border);
            drawVerticalLine(btn.x, btn.y, btn.y + BTN_H - 1, border);
            drawVerticalLine(btn.x + BTN_W - 1, btn.y, btn.y + BTN_H - 1, border);

            String label = getLabelFor(btn.id);
            int tw = this.fontRendererObj.getStringWidth(label);
            int lx = btn.x + (BTN_W - tw) / 2;
            int ly = btn.y + (BTN_H - this.fontRendererObj.FONT_HEIGHT) / 2;
            this.fontRendererObj.drawStringWithShadow(label, lx, ly, active ? 0xFFFFFF : 0xAAAAAA);
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void drawSideButtonTooltips(int mouseX, int mouseY) {
        // Tooltips drawn last, on top of everything
        for (SideButton btn : sideButtons) {
            if (btn.contains(mouseX, mouseY)) {
                List<String> tip = new ArrayList<String>();
                tip.add(getTooltipFor(btn.id));
                func_146283_a(tip, mouseX, mouseY);
                break;
            }
        }
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

        drawSideButtons(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        handleScrolling(mouseX, mouseY);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String totalCount = formatter.format(getInventory().getTotalCount());
        String max = formatter.format(getInventory().maxItems);
        String amount = totalCount + "/" + max;
        int stringWidth = fontRendererObj.getStringWidth(amount);

        if (stringWidth > 88) {
            float scaleFactor = 0.7f;
            float rScaleFactor = 1.0f / scaleFactor;
            GL11.glPushMatrix();
            GL11.glScaled(scaleFactor, scaleFactor, scaleFactor);
            int bx = (int) (((float) 187 - stringWidth * scaleFactor) * rScaleFactor);
            fontRendererObj.drawString(amount, bx, 10, 4210752);
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
        this.ezRenderer.zLevel = 200.0F;

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
                ItemStack stack = this.filteredList.get(index);
                if (stack != null) {
                    FontRenderer font = stack.getItem()
                        .getFontRenderer(stack);
                    if (font == null) font = fontRendererObj;
                    RenderHelper.enableGUIStandardItemLighting();
                    itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), stack, x, y);
                    ezRenderer.renderItemOverlayIntoGUI(font, stack, x, y, "" + stack.stackSize);
                }
                x += 18;
            }
            if (finished) break;
            y += 18;
        }

        this.zLevel = 0.0F;
        itemRender.zLevel = 0.0F;
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        int i1 = 175;
        int k = 18;
        int l = k + 108;
        this.mc.getTextureManager()
            .bindTexture(creativeInventoryTabs);
        this.drawTexturedModalRect(i1, k + (int) ((float) (l - k - 17) * this.currentScroll), 232, 0, 12, 15);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // Side buttons (outside GuiContainer bounds — handle before super)
        if (mouseButton == 0 || mouseButton == 1) {
            for (SideButton btn : sideButtons) {
                if (btn.contains(mouseX, mouseY)) {
                    handleButtonClick(btn);
                    return;
                }
            }
        }

        boolean wantFocus;
        if (isOverSearchField(mouseX, mouseY)) {
            ItemStack heldItem = this.mc.thePlayer.inventory.getItemStack();
            if (heldItem != null && (mouseButton == 0 || mouseButton == 1)) { // Clicked field with item
                String displayName = EnumChatFormatting.getTextWithoutFormattingCodes(heldItem.getDisplayName());
                setTextFieldValue(displayName, mouseX, mouseY, heldItem);
            } else if (mouseButton == 1) { // Right click to clear
                searchText = "";
                this.searchField.setText("");
                updateFilteredItems(true);
                if (ModIds.NEI.isLoaded()
                    && (currentSearchMode == SearchMode.NEI_SYNC || currentSearchMode == SearchMode.NEI_STANDARD)) {
                    setNeiSearchText("");
                    lastNeiText = "";
                }
            } else if (mouseButton == 0) {
                this.searchField.mouseClicked(mouseX, mouseY, mouseButton);
            }
            wantFocus = true;
        } else {
            wantFocus = false;
        }

        Integer slot = getSlotAt(mouseX, mouseY);
        if (slot != null) {
            int mode = GuiScreen.isShiftKeyDown() ? 1 : 0;
            int index = getInventory().slotCount();
            if (slot < this.filteredList.size()) {
                ItemStack group = this.filteredList.get(slot);
                if (group == null || group.stackSize == 0) {
                    return;
                }
                index = getInventory().getIndexOf(group);
                if (index < 0) {
                    return;
                }
            }
            EZStorage.instance.network.sendToServer(new MsgInvSlotClicked(index, mouseButton, mode));
            ContainerStorageCore container = (ContainerStorageCore) this.inventorySlots;
            container.customSlotClick(index, mouseButton, mode, this.mc.thePlayer);
            wantFocus = false;
        }

        this.searchField.setFocused(wantFocus);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean isOverSearchField(int mouseX, int mouseY) {
        int fx = this.searchField.xPosition;
        int fy = this.searchField.yPosition;
        return mouseX >= fx && mouseX < fx + this.searchField.width
            && mouseY >= fy
            && mouseY < fy + this.searchField.height + 4;
    }

    private Integer getSlotAt(int x, int y) {
        int startX = this.guiLeft + 8 - 1;
        int startY = this.guiTop + 18 - 1;
        int cx = x - startX;
        int cy = y - startY;
        if (cx > 0 && cy > 0) {
            int col = cx / 18;
            if (col < 9) {
                int row = cy / 18;
                if (row < this.rowsVisible()) {
                    return (row * 9) + col + (scrollRow * 9);
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
            int j = filteredList.size() / 9 - this.rowsVisible() + 1;
            if (i > 0) i = 1;
            if (i < 0) i = -1;
            this.currentScroll = (float) ((double) this.currentScroll - (double) i / (double) j);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            scrollTo(this.currentScroll);
        }
    }

    private void handleScrolling(int mouseX, int mouseY) {
        boolean flag = Mouse.isButtonDown(0);
        int i1 = this.guiLeft + 175;
        int j1 = this.guiTop + 18;
        int k1 = i1 + 14;
        int l1 = j1 + 108;

        if (!this.wasClicking && flag && mouseX >= i1 && mouseY >= j1 && mouseX < k1 && mouseY < l1) {
            this.isScrolling = true;
        }
        if (!flag) this.isScrolling = false;
        this.wasClicking = flag;

        if (this.isScrolling) {
            this.currentScroll = ((float) (mouseY - j1) - 7.5F) / ((float) (l1 - j1) - 15.0F);
            this.currentScroll = MathHelper.clamp_float(this.currentScroll, 0.0F, 1.0F);
            scrollTo(this.currentScroll);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!this.checkHotbarKeys(keyCode)) {
            if (this.searchField.isFocused() && this.searchField.textboxKeyTyped(typedChar, keyCode)) {
                currentScroll = 0;
                scrollRow = 0;
                updateFilteredItems(true);
                if (ModIds.NEI.isLoaded()
                    && (currentSearchMode == SearchMode.NEI_SYNC || currentSearchMode == SearchMode.NEI_STANDARD)) {
                    setNeiSearchText(this.searchField.getText());
                    lastNeiText = this.searchField.getText();
                }
            } else {
                super.keyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public void updateScreen() {
        if (inventorySlots instanceof ContainerStorageCore container
            && (inventoryUpdateTimestamp != container.inventoryUpdateTimestamp
                || (needFullUpdate && !GuiScreen.isShiftKeyDown()))) {
            inventoryUpdateTimestamp = container.inventoryUpdateTimestamp;
            updateFilteredItems(false);
        }

        // Sync NEI text block
        if (ModIds.NEI.isLoaded()
            && (currentSearchMode == SearchMode.NEI_SYNC || currentSearchMode == SearchMode.NEI_STANDARD)) {
            String neiText = getNeiSearchText();
            if (neiText != null && !neiText.equals(lastNeiText)) {
                lastNeiText = neiText;
                if (!neiText.equals(this.searchField.getText())) {
                    this.searchField.setText(neiText);
                    searchText = neiText;
                    currentScroll = 0;
                    scrollRow = 0;
                    updateFilteredItems(true);
                }
            }
        }

        super.updateScreen();
    }

    private void updateFilteredItems(boolean forceFullUpdate) {
        searchText = this.searchField.getText()
            .trim();

        if (forceFullUpdate || !GuiScreen.isShiftKeyDown()) {
            filteredList.clear();
            filterItems(searchText, getInventory().inventory);
            sortFilteredList();
            needFullUpdate = false;
        } else {
            List<ItemStack> listNewStacks = new ArrayList<ItemStack>();
            for (ItemStack stackSrc : getInventory().inventory) {
                boolean found = false;
                for (ItemStack stackDest : filteredList) {
                    if (EZInventory.stacksEqual(stackDest, stackSrc)) {
                        stackDest.stackSize = stackSrc.stackSize;
                        found = true;
                    }
                }
                if (!found) listNewStacks.add(stackSrc);
            }
            for (ItemStack stackDest : filteredList) {
                boolean found = false;
                for (ItemStack stackSrc : getInventory().inventory) {
                    if (EZInventory.stacksEqual(stackDest, stackSrc)) {
                        found = true;
                        break;
                    }
                }
                if (!found) stackDest.stackSize = 0;
            }
            if (!listNewStacks.isEmpty()) filterItems(searchText, listNewStacks);
            needFullUpdate = true;
        }
    }

    private void sortFilteredList() {
        Comparator<ItemStack> comparator;
        switch (currentSortMode) {
            case NAME:
                comparator = new ItemStackNameComparator();
                break;
            case MOD:
                comparator = new ItemStackModComparator();
                break;
            default:
                comparator = new ItemStackCountComparator();
                break;
        }
        if (currentSortOrder == SortOrder.ASCENDING) {
            comparator = Collections.reverseOrder(comparator);
        }
        Collections.sort(filteredList, comparator);
    }

    private void filterItems(String text, List<ItemStack> input) {
        if (text.isEmpty()) {
            filteredList.addAll(input);
            return;
        }
        if ((currentSearchMode == SearchMode.AUTO || currentSearchMode == SearchMode.NEI_SYNC)
            && ModIds.NEI.isLoaded()) {
            filterItemsViaNei(text, input);
        } else {
            filterItemsViaVanilla(text, input);
        }
    }

    private void filterItemsViaVanilla(String raw, List<ItemStack> input) {
        String text = raw.toLowerCase();
        boolean modFilter = text.startsWith("@");
        String query = modFilter ? text.substring(1) : text;
        if (query.isEmpty()) {
            filteredList.addAll(input);
            return;
        }
        for (ItemStack group : input) {
            if (modFilter) {
                String modId = "";
                String regName = GameData.getItemRegistry()
                    .getNameForObject(group.getItem());
                if (regName != null && regName.contains(":")) {
                    modId = regName.split(":")[0];
                }
                if (modId.toLowerCase()
                    .contains(query)) {
                    filteredList.add(group);
                } else {
                    List<String> lines = group.getTooltip(this.mc.thePlayer, false);
                    if (lines.size() > 0) {
                        String lastLine = EnumChatFormatting.getTextWithoutFormattingCodes(lines.get(lines.size() - 1))
                            .toLowerCase();
                        if (lastLine.contains(query)) {
                            filteredList.add(group);
                        }
                    }
                }
            } else {
                List<String> lines = group.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
                for (String line : lines) {
                    if (EnumChatFormatting.getTextWithoutFormattingCodes(line)
                        .toLowerCase()
                        .contains(text)) {
                        filteredList.add(group);
                        break;
                    }
                }
            }
        }
    }

    @Method(modid = "NotEnoughItems")
    private void filterItemsViaNei(String text, List<ItemStack> input) {
        if (text.isEmpty()) {
            filteredList.addAll(input);
            return;
        }
        ItemFilter filter = SearchField.getFilter(text);
        for (ItemStack group : input) {
            if (filter.matches(group)) {
                filteredList.add(group);
            }
        }
    }

    private void scrollTo(float scroll) {
        int i = (filteredList.size() + 8) / 9 - this.rowsVisible();
        int j = (int) ((double) (scroll * (float) i) + 0.5D);
        if (j < 0) j = 0;
        this.scrollRow = j;
    }

    protected void cacheMouseOverItem(int mouseX, int mouseY) {
        Integer slot = getSlotAt(mouseX, mouseY);
        if (slot != null && slot < this.filteredList.size()) {
            ItemStack group = this.filteredList.get(slot);
            if (group != null) {
                mouseOverItem = group;
                return;
            }
        }
        mouseOverItem = null;
    }

    private static void loadSettingsFromConfig() {
        try {
            currentSortMode = SortMode.valueOf(EZConfiguration.guiSortMode);
        } catch (IllegalArgumentException e) {
            currentSortMode = SortMode.AMOUNT;
        }
        try {
            currentSortOrder = SortOrder.valueOf(EZConfiguration.guiSortOrder);
        } catch (IllegalArgumentException e) {
            currentSortOrder = SortOrder.DESCENDING;
        }
        try {
            currentSearchMode = SearchMode.valueOf(EZConfiguration.guiSearchMode);
        } catch (IllegalArgumentException e) {
            currentSearchMode = SearchMode.AUTO;
        }
        saveSearch = EZConfiguration.guiSaveSearch;
        if (saveSearch) {
            searchText = EZConfiguration.guiSearchText;
        }
    }

    private static void saveSettingsToConfig() {
        EZConfiguration.guiSortMode = currentSortMode.name();
        EZConfiguration.guiSortOrder = currentSortOrder.name();
        EZConfiguration.guiSearchMode = currentSearchMode.name();
        EZConfiguration.guiSaveSearch = saveSearch;
        EZConfiguration.guiSearchText = saveSearch ? searchText : "";
        EZConfiguration.save();
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

    @Method(modid = "NotEnoughItems")
    protected void setNeiSearchText(String text) {
        if (ModIds.NEI.isLoaded()) {
            NEIClientConfig.setSearchExpression(text);
            if (codechicken.nei.LayoutManager.searchField != null) {
                codechicken.nei.LayoutManager.searchField.setText(text);
            }
        }
    }

    @Method(modid = "NotEnoughItems")
    protected String getNeiSearchText() {
        if (ModIds.NEI.isLoaded()) {
            return NEIClientConfig.getSearchExpression();
        }
        return null;
    }
}
