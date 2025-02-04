package org.uiop.easyplacefix.screen;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.uiop.easyplacefix.data.LoosenModeData;

import java.util.*;
import java.util.function.Predicate;

public class CustomInventoryScreen extends HandledScreen<CustomInventoryScreen.CustomScreenHandler> {
    private float loosenScrool = 0f;
    private final Collection<ItemStack> loosenItemList = ItemStackSet.create();
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/creative_inventory/scroller_disabled");
    private static final Identifier[] TAB_TOP_UNSELECTED_TEXTURES = new Identifier[]{
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_1"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_2"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_3"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_4"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_5"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_6"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_unselected_7")
    };
    private static final Identifier[] TAB_TOP_SELECTED_TEXTURES = new Identifier[]{
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_1"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_2"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_3"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_4"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_5"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_6"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_7")
    };
    private static final Identifier BUTTON = Identifier.ofVanilla("advancements/tab_left_top");
    private static final Identifier ON_BUTTON = Identifier.ofVanilla("advancements/tab_left_top_selected");

    private static final Identifier[] TAB_BOTTOM_UNSELECTED_TEXTURES = new Identifier[]{
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_1"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_2"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_3"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_4"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_5"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_6"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_unselected_7"),
            Identifier.ofVanilla("textures/gui/book.png")
    };
    private static final Identifier[] TAB_BOTTOM_SELECTED_TEXTURES = new Identifier[]{
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_1"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_2"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_3"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_4"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_5"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_6"),
            Identifier.ofVanilla("container/creative_inventory/tab_bottom_selected_7"),
            Identifier.ofVanilla("container/creative_inventory/tab_top_selected_6")
    };
    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    static final SimpleInventory INVENTORY = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static final int WHITE = 16777215;
    private static ItemGroup selectedTab = ItemGroups.getDefaultTab();
    private static ItemGroup loosenGroup = null;
    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Set<TagKey<Item>> searchResultTags = new HashSet();
    private final boolean operatorTabEnabled;

    public CustomInventoryScreen(ClientPlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled) {
        super(new CustomScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);
        player.currentScreenHandler = this.handler;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
        this.operatorTabEnabled = operatorTabEnabled;
        this.populateDisplay(player.networkHandler.getSearchManager(), enabledFeatures, this.shouldShowOperatorTab(player), player.getWorld().getRegistryManager());
    }

    private boolean shouldShowOperatorTab(PlayerEntity player) {
        return player.isCreativeLevelTwoOp() && this.operatorTabEnabled;
    }

    private void updateDisplayParameters(FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registries) {
        ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
        if (this.populateDisplay(clientPlayNetworkHandler != null ? clientPlayNetworkHandler.getSearchManager() : null, enabledFeatures, showOperatorTab, registries)) {
            for (ItemGroup itemGroup : ItemGroups.getGroups()) {
                Collection<ItemStack> collection = itemGroup.getDisplayStacks();
                if (itemGroup == selectedTab) {
                    if (itemGroup.getType() == ItemGroup.Type.CATEGORY && collection.isEmpty()) {
                        this.setSelectedTab(ItemGroups.getDefaultTab());
                    } else {
                        this.refreshSelectedTab(collection);
                    }
                }

            }
        }
    }

    private boolean populateDisplay(@Nullable SearchManager searchManager, FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registries) {
        if (!ItemGroups.updateDisplayContext(enabledFeatures, showOperatorTab, registries)) {
            return false;
        } else {
            if (searchManager != null) {
                List<ItemStack> list = List.copyOf(ItemGroups.getSearchGroup().getDisplayStacks());
                searchManager.addItemTooltipReloader(registries, list);
                searchManager.addItemTagReloader(list);
            }
            return true;
        }
    }

    private void refreshSelectedTab(Collection<ItemStack> displayStacks) {
        int i = this.handler.getRow(this.scrollPosition);
        this.handler.itemList.clear();
        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.search();
        } else {
            this.handler.itemList.addAll(displayStacks);
        }
        this.scrollPosition = this.handler.getScrollPosition(i);
        this.handler.scrollItems(this.scrollPosition);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.client != null && this.client.player != null) {
            this.updateDisplayParameters(
                    this.client.player.networkHandler.getEnabledFeatures(), this.shouldShowOperatorTab(this.client.player), this.client.player.getWorld().getRegistryManager()
            );
        }
    }

    @Override
    public void close() {
        LoosenModeData.saveToFile(loosenItemList);
        super.close();
    }

    @Override
    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        // 自定义点击操作
        // 你可以在这里实现自定义的点击逻辑
        if (slot != null) {
            if (actionType == SlotActionType.QUICK_MOVE) {
                loosenItemList.remove(slot.getStack());
                if (selectedTab == loosenGroup) {
                    this.setSelectedTab(selectedTab);
                }
            } else {
                if (!slot.getStack().isEmpty()) {
                    loosenItemList.add(slot.getStack());
                    if (selectedTab == loosenGroup) {
                        this.setSelectedTab(selectedTab);
                    }
                }


            }

        }
    }

    @Override
    protected void init() {
        super.init();
        loosenItemList.addAll(LoosenModeData.loadFromFile());
        loosenGroup = new ItemGroup.Builder(ItemGroup.Row.BOTTOM, 7)
                .displayName(Text.literal("自定义宽容模式 LoosenMode Tab"))
                .icon(() ->
                {
                    if (loosenItemList.isEmpty()) {
                        return new ItemStack(Items.DIAMOND);
                    } else {
                        return loosenItemList.iterator().next();
                    }

                }).build();
        this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 80, 9, Text.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setDrawsBackground(false);
        this.searchBox.setVisible(false);
        this.searchBox.setEditableColor(16777215);
        this.addSelectableChild(this.searchBox);
        selectedTab = ItemGroups.getDefaultTab();
        ItemGroup itemGroup = selectedTab;
        this.setSelectedTab(itemGroup);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int i = this.handler.getRow(this.scrollPosition);
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }
        this.scrollPosition = this.handler.getScrollPosition(i);
        this.handler.scrollItems(this.scrollPosition);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            return false;
        } else {
            String string = this.searchBox.getText();
            if (this.searchBox.charTyped(chr, modifiers)) {
                if (!Objects.equals(string, this.searchBox.getText())) {
                    this.search();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            if (this.client.options.chatKey.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroups.getSearchGroup());
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
            boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
            if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                String string = this.searchBox.getText();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(string, this.searchBox.getText())) {
                        this.search();
                    }
                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE ? true : super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        this.handler.itemList.clear();
        this.searchResultTags.clear();
        String string = this.searchBox.getText();
        if (string.isEmpty()) {
            this.handler.itemList.addAll(selectedTab.getDisplayStacks());
        } else {
            ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.getNetworkHandler();
            if (clientPlayNetworkHandler != null) {
                SearchManager searchManager = clientPlayNetworkHandler.getSearchManager();
                SearchProvider<ItemStack> searchProvider;
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    searchProvider = searchManager.getItemTagReloadFuture();
                    this.searchForTags(string);
                } else {
                    searchProvider = searchManager.getItemTooltipReloadFuture();
                }
                this.handler.itemList.addAll(searchProvider.findAll(string.toLowerCase(Locale.ROOT)));
            }
        }
        this.scrollPosition = 0.0F;
        this.handler.scrollItems(0.0F);
    }

    private void searchForTags(String id) {
        int i = id.indexOf(58);
        Predicate<Identifier> predicate;
        if (i == -1) {
            predicate = idx -> idx.getPath().contains(id);
        } else {
            String string = id.substring(0, i).trim();
            String string2 = id.substring(i + 1).trim();
            predicate = idx -> idx.getNamespace().contains(string) && idx.getPath().contains(string2);
        }
        Registries.ITEM.streamTags().map(RegistryEntryList.Named::getTag).filter(tag -> predicate.test(tag.id())).forEach(this.searchResultTags::add);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (selectedTab.shouldRenderName()) {
            context.drawText(this.textRenderer, selectedTab.getDisplayName(), 8, 6, 4210752, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.x;
            double e = mouseY - (double) this.y;

            for (ItemGroup itemGroup : ItemGroups.getGroupsToDisplay()) {
                if (this.isClickInTab(itemGroup, d, e)) {
                    return true;
                }
            }

            if (selectedTab.getType() != ItemGroup.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }
//            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.x;
            double e = mouseY - (double) this.y;
            this.scrolling = false;

            for (ItemGroup itemGroup : ItemGroups.getGroupsToDisplay()) {
                if (this.isClickInTab(itemGroup, d, e)) {
                    this.setSelectedTab(itemGroup);
                    return true;
                }
                if (this.isClickInTab2(loosenGroup, d, e)) {
                    this.setSelectedTab(loosenGroup);
                    return true;
                }
            }

        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hasScrollbar() {
        return selectedTab.hasScrollbar() && this.handler.shouldShowScrollbar();
    }

    private void setSelectedTab(ItemGroup group) {
        ItemGroup itemGroup = selectedTab;
        selectedTab = group;
        this.cursorDragSlots.clear();
        this.handler.itemList.clear();
        this.endTouchDrag();
        if (selectedTab.getType() == ItemGroup.Type.HOTBAR) {
            HotbarStorage hotbarStorage = this.client.getCreativeHotbarStorage();

            for (int i = 0; i < 9; i++) {
                HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(i);
                if (hotbarStorageEntry.isEmpty()) {
                    for (int j = 0; j < 9; j++) {
                        if (j == i) {
                            ItemStack itemStack = new ItemStack(Items.PAPER);
                            itemStack.set(DataComponentTypes.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
                            Text text = this.client.options.hotbarKeys[i].getBoundKeyLocalizedText();
                            Text text2 = this.client.options.saveToolbarActivatorKey.getBoundKeyLocalizedText();
                            itemStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("inventory.hotbarInfo", text2, text));
                            this.handler.itemList.add(itemStack);
                        } else {
                            this.handler.itemList.add(ItemStack.EMPTY);
                        }
                    }
                } else {
                    this.handler.itemList.addAll(hotbarStorageEntry.deserialize(this.client.world.getRegistryManager()));
                }
            }
        } else if (selectedTab.getType() == ItemGroup.Type.CATEGORY) {
            if (selectedTab == loosenGroup) {
                this.handler.itemList.addAll(loosenItemList);
            } else {
                this.handler.itemList.addAll(selectedTab.getDisplayStacks());
            }

        }

        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            ScreenHandler screenHandler = this.client.player.playerScreenHandler;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(this.handler.slots);
            }

            this.handler.slots.clear();

            for (int ix = 0; ix < screenHandler.slots.size(); ix++) {
                int n;
                int jx;
                if (ix >= 5 && ix < 9) {
                    int k = ix - 5;
                    int l = k / 2;
                    int m = k % 2;
                    n = 54 + l * 54;
                    jx = 6 + m * 27;
                } else if (ix >= 0 && ix < 5) {
                    n = -2000;
                    jx = -2000;
                } else if (ix == 45) {
                    n = 35;
                    jx = 20;
                } else {
                    int k = ix - 9;
                    int l = k % 9;
                    int m = k / 9;
                    n = 9 + l * 18;
                    if (ix >= 36) {
                        jx = 112;
                    } else {
                        jx = 54 + m * 18;
                    }
                }

                Slot slot = new CustomSlot(screenHandler.slots.get(ix), ix, n, jx);
                this.handler.slots.add(slot);
            }

            this.deleteItemSlot = new Slot(INVENTORY, 0, 173, 112);
            this.handler.slots.add(this.deleteItemSlot);
        } else if (itemGroup.getType() == ItemGroup.Type.INVENTORY) {
            this.handler.slots.clear();
            this.handler.slots.addAll(this.slots);
            this.slots = null;
        }

        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setFocused(true);
            if (itemGroup != group) {
                this.searchBox.setText("");
            }
            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setFocused(false);
            this.searchBox.setText("");
        }
        if (loosenGroup != selectedTab) {
            if (itemGroup == loosenGroup) {
                loosenScrool = this.scrollPosition;
            }
            this.scrollPosition = 0.0F;
        } else {
            if (itemGroup == loosenGroup) {
                loosenScrool = this.scrollPosition;
            } else {
                this.scrollPosition = loosenScrool;
            }

        }
        this.handler.scrollItems(this.scrollPosition);


    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        } else if (!this.hasScrollbar()) {
            return false;
        } else {
            this.scrollPosition = this.handler.getScrollPosition(this.scrollPosition, verticalAmount);
            this.handler.scrollItems(this.scrollPosition);
            return true;
        }
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double) left
                || mouseY < (double) top
                || mouseX >= (double) (left + this.backgroundWidth)
                || mouseY >= (double) (top + this.backgroundHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(selectedTab, mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.y + 18;
            int j = i + 112;
            this.scrollPosition = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
            this.handler.scrollItems(this.scrollPosition);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        for (ItemGroup itemGroup : ItemGroups.getGroupsToDisplay()) {
            if (this.renderTabTooltipIfHovered(context, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        if (this.deleteItemSlot != null
                && selectedTab.getType() == ItemGroup.Type.INVENTORY
                && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double) mouseX, (double) mouseY)) {
            context.drawTooltip(this.textRenderer, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        for (ItemGroup itemGroup : ItemGroups.getGroupsToDisplay()) {
            if (itemGroup != selectedTab) {
                this.renderTabIcon(context, itemGroup);
            }
        }

        if (selectedTab != loosenGroup) {
            this.renderTabIcon2(context, loosenGroup);
        }


        context.drawTexture(RenderLayer::getGuiTextured, selectedTab.getTexture(), this.x, this.y, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        this.searchBox.render(context, mouseX, mouseY, delta);
        int i = this.x + 175;
        int j = this.y + 18;
        int k = j + 112;
        if (selectedTab.hasScrollbar()) {
            Identifier identifier = this.hasScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
            context.drawGuiTexture(RenderLayer::getGuiTextured, identifier, i, j + (int) ((float) (k - j - 17) * this.scrollPosition), 12, 15);
        }
        if (selectedTab != loosenGroup) {
            this.renderTabIcon(context, selectedTab);
        } else {
            this.renderTabIcon2(context, loosenGroup);
        }


        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            InventoryScreen.drawEntity(context, this.x + 73, this.y + 6, this.x + 105, this.y + 49, 20, 0.0625F, (float) mouseX, (float) mouseY, this.client.player);
        }
    }

    private int getTabX(ItemGroup group) {
        int i = group.getColumn();
        int j = 27;
        int k = 27 * i;
        if (group.isSpecial()) {
            k = this.backgroundWidth - 27 * (7 - i) + 1;
        }
        return k;
    }

    private int getTabX2(ItemGroup group) {
        return -25;
    }

    private int getTabY2(ItemGroup group) {
        return 10;
    }

    private int getTabY(ItemGroup group) {
        int i = 0;
        if (group.getRow() == ItemGroup.Row.TOP) {
            i -= 32;
        } else {
            i += this.backgroundHeight;
        }
        return i;
    }

    protected boolean isClickInTab2(ItemGroup group, double mouseX, double mouseY) {
        int i = this.getTabX2(group);
        int j = this.getTabY2(group);
        return mouseX >= (double) i && mouseX <= (double) (i + 26) && mouseY >= (double) j && mouseY <= (double) (j + 32);
    }

    protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        return mouseX >= (double) i && mouseX <= (double) (i + 26) && mouseY >= (double) j && mouseY <= (double) (j + 32);
    }

    protected boolean renderTabTooltipIfHovered(DrawContext context, ItemGroup group, int mouseX, int mouseY) {
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        if (this.isPointWithinBounds(i + 3, j + 3, 21, 27, (double) mouseX, (double) mouseY)) {
            context.drawTooltip(this.textRenderer, group.getDisplayName(), mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabIcon2(DrawContext context, ItemGroup group) {
        boolean isSelectTab = group == selectedTab;
        int tabX = this.x - 23;


        int tabY = this.y + 10;
        Identifier identifiers;

        identifiers = isSelectTab ? ON_BUTTON : BUTTON;


        context.drawGuiTexture(RenderLayer::getGuiTextured, identifiers, tabX, tabY, 26, 32);
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 100.0F);
        tabX += 5;
        tabY += 8 + (-1);
        ItemStack itemStack = group.getIcon();
        context.drawItem(itemStack, tabX, tabY);
        context.drawStackOverlay(this.textRenderer, itemStack, tabX, tabY);
        context.getMatrices().pop();
    }

    protected void renderTabIcon(DrawContext context, ItemGroup group) {
        boolean bl = group == selectedTab;
        boolean bl2 = group.getRow() == ItemGroup.Row.TOP;
        int i = group.getColumn();
        int j = this.x + this.getTabX(group);
        int k = this.y - (bl2 ? 28 : -(this.backgroundHeight - 4));
        Identifier[] identifiers;
        if (bl2) {
            identifiers = bl ? TAB_TOP_SELECTED_TEXTURES : TAB_TOP_UNSELECTED_TEXTURES;
        } else {
            identifiers = bl ? TAB_BOTTOM_SELECTED_TEXTURES : TAB_BOTTOM_UNSELECTED_TEXTURES;
        }

        context.drawGuiTexture(RenderLayer::getGuiTextured, identifiers[MathHelper.clamp(i, 0, identifiers.length)], j, k, 26, 32);
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, 100.0F);
        j += 5;
        k += 8 + (bl2 ? 1 : -1);
        ItemStack itemStack = group.getIcon();
        context.drawItem(itemStack, j, k);
        context.drawStackOverlay(this.textRenderer, itemStack, j, k);
        context.getMatrices().pop();
    }

    public boolean isInventoryTabSelected() {
        return selectedTab.getType() == ItemGroup.Type.INVENTORY;
    }


    @Environment(EnvType.CLIENT)
    public static class CustomScreenHandler extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;

        public CustomScreenHandler(PlayerEntity player) {
            super(null, 0);
            this.parent = player.playerScreenHandler;
            PlayerInventory playerInventory = player.getInventory();

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 9; j++) {
                    this.addSlot(new LockableSlot(CustomInventoryScreen.INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            this.addPlayerHotbarSlots(playerInventory, 9, 112);
            this.scrollItems(0.0F);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        protected int getOverflowRows() {
            return MathHelper.ceilDiv(this.itemList.size(), 9) - 5;
        }

        protected int getRow(float scroll) {
            return Math.max((int) ((double) (scroll * (float) this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return MathHelper.clamp((float) row / (float) this.getOverflowRows(), 0.0F, 1.0F);
        }

        protected float getScrollPosition(float current, double amount) {
            return MathHelper.clamp(current - (float) (amount / (double) this.getOverflowRows()), 0.0F, 1.0F);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);

            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 9; k++) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.itemList.size()) {
                        CustomInventoryScreen.INVENTORY.setStack(k + j * 9, this.itemList.get(l));
                    } else {
                        CustomInventoryScreen.INVENTORY.setStack(k + j * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {
            if (slot >= this.slots.size() - 9 && slot < this.slots.size()) {
                Slot slot2 = this.slots.get(slot);
                if (slot2 != null && slot2.hasStack()) {
                    slot2.setStack(ItemStack.EMPTY);
                }
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != CustomInventoryScreen.INVENTORY;
        }

        @Override
        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != CustomInventoryScreen.INVENTORY;
        }

        @Override
        public ItemStack getCursorStack() {
            return this.parent.getCursorStack();
        }

        @Override
        public void setCursorStack(ItemStack stack) {
            this.parent.setCursorStack(stack);
        }
    }

    @Environment(EnvType.CLIENT)
    static class CustomSlot extends Slot {
        final Slot slot;

        public CustomSlot(Slot slot, int invSlot, int x, int y) {
            super(slot.inventory, invSlot, x, y);
            this.slot = slot;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.slot.onTakeItem(player, stack);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.slot.canInsert(stack);
        }

        @Override
        public ItemStack getStack() {
            return this.slot.getStack();
        }

        @Override
        public boolean hasStack() {
            return this.slot.hasStack();
        }

        @Override
        public void setStack(ItemStack stack, ItemStack previousStack) {
            this.slot.setStack(stack, previousStack);
        }

        @Override
        public void setStackNoCallbacks(ItemStack stack) {
            this.slot.setStackNoCallbacks(stack);
        }

        @Override
        public void markDirty() {
            this.slot.markDirty();
        }

        @Override
        public int getMaxItemCount() {
            return this.slot.getMaxItemCount();
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return this.slot.getMaxItemCount(stack);
        }

        @Nullable
        @Override
        public Identifier getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        @Override
        public ItemStack takeStack(int amount) {
            return this.slot.takeStack(amount);
        }

        @Override
        public boolean isEnabled() {
            return this.slot.isEnabled();
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.slot.canTakeItems(playerEntity);
        }
    }

    @Environment(EnvType.CLIENT)
    static class LockableSlot extends Slot {
        public LockableSlot(Inventory inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            ItemStack itemStack = this.getStack();
            return super.canTakeItems(playerEntity) && !itemStack.isEmpty()
                    ? itemStack.isItemEnabled(playerEntity.getWorld().getEnabledFeatures()) && !itemStack.contains(DataComponentTypes.CREATIVE_SLOT_LOCK)
                    : itemStack.isEmpty();
        }
    }
}
