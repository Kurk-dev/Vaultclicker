package com.vaultsniper;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class SniperScreen extends Screen {

    private static final int SLOT_SIZE = 20;
    private static final int COLS = 9;
    private static final int PANEL_COLOR = 0xCC1A1A2E;
    private static final int BORDER_COLOR = 0xFF5555AA;
    private static final int SELECTED_COLOR = 0xFF55FF55;
    private static final int HOVER_COLOR = 0x44FFFFFF;

    private int panelX, panelY, panelW, panelH;

    public SniperScreen() {
        super(Text.literal("Vault Sniper"));
    }

    @Override
    protected void init() {
        List<Item> items = currentItems();
        int rows = (int) Math.ceil(items.size() / (double) COLS);
        panelW = COLS * SLOT_SIZE + 20;
        panelH = 30 + 24 + rows * SLOT_SIZE + 12 + 20 + 10;
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;

        int btnY = panelY + 28;
        int btnW = (panelW - 18) / 2;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("⚔ Normal Vault"),
                b -> {
                    SniperState.useOminousVault = false;
                    rebuildButtons();
                })
                .dimensions(panelX + 6, btnY, btnW, 18)
                .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("☠ Ominous Vault"),
                b -> {
                    SniperState.useOminousVault = true;
                    rebuildButtons();
                })
                .dimensions(panelX + 8 + btnW, btnY, btnW, 18)
                .build());

        int toggleBtnY = panelY + panelH - 24;
        this.addDrawableChild(ButtonWidget.builder(
                toggleLabel(),
                b -> {
                    if (SniperState.targetItem != null) {
                        SniperState.enabled = !SniperState.enabled;
                        b.setMessage(toggleLabel());
                    }
                })
                .dimensions(panelX + 6, toggleBtnY, panelW - 12, 18)
                .build());
    }

    private void rebuildButtons() {
        this.clearChildren();
        this.init();
    }

    private Text toggleLabel() {
        if (SniperState.targetItem == null) return Text.literal("§7Select a target item first");
        return SniperState.enabled
                ? Text.literal("§c■ Disable Sniping")
                : Text.literal("§a▶ Enable Sniping");
    }

    private List<Item> currentItems() {
        return SniperState.useOminousVault
                ? SniperState.OMINOUS_VAULT_ITEMS
                : SniperState.NORMAL_VAULT_ITEMS;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // Main panel
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_COLOR);
        context.drawBorder(panelX, panelY, panelW, panelH, BORDER_COLOR);

        // Title
        context.drawCenteredTextWithShadow(this.textRenderer,
                "§b✦ §lVault Sniper §b✦", this.width / 2, panelY + 8, 0xFFFFFF);

        // Vault type highlight
        int btnW = (panelW - 18) / 2;
        int normalX = panelX + 6;
        int ominousX = panelX + 8 + btnW;
        int btnY = panelY + 28;
        if (!SniperState.useOminousVault) {
            context.fill(normalX, btnY, normalX + btnW, btnY + 18, 0x4400FF88);
        } else {
            context.fill(ominousX, btnY, ominousX + btnW, btnY + 18, 0x44FF4444);
        }

        // Item grid
        List<Item> items = currentItems();
        int gridX = panelX + 10;
        int gridY = panelY + 52;

        for (int i = 0; i < items.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx = gridX + col * SLOT_SIZE;
            int sy = gridY + row * SLOT_SIZE;

            boolean isSelected = items.get(i) == SniperState.targetItem;
            boolean isHovered = mouseX >= sx && mouseX < sx + SLOT_SIZE
                    && mouseY >= sy && mouseY < sy + SLOT_SIZE;

            if (isSelected) {
                context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0x88FFFF88);
                context.drawBorder(sx, sy, SLOT_SIZE, SLOT_SIZE, SELECTED_COLOR);
            } else if (isHovered) {
                context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, HOVER_COLOR);
            } else {
                context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0x33FFFFFF);
            }

            context.drawItem(new ItemStack(items.get(i)), sx + 2, sy + 2);
        }

        // Target label
        int targetY = gridY + ((int) Math.ceil(items.size() / (double) COLS)) * SLOT_SIZE + 4;
        if (SniperState.targetItem != null) {
            String targetName = SniperState.targetItem.getName().getString();
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "§7Target: §f" + targetName, this.width / 2, targetY, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    "§7Click an item to select your target", this.width / 2, targetY, 0xAAAAAA);
        }

        // Tooltip for hovered item
        for (int i = 0; i < items.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx = gridX + col * SLOT_SIZE;
            int sy = gridY + row * SLOT_SIZE;
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                context.drawTooltip(this.textRenderer,
                        new ItemStack(items.get(i)).getName(), mouseX, mouseY);
                break;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            List<Item> items = currentItems();
            int gridX = panelX + 10;
            int gridY = panelY + 52;

            for (int i = 0; i < items.size(); i++) {
                int col = i % COLS;
                int row = i / COLS;
                int sx = gridX + col * SLOT_SIZE;
                int sy = gridY + row * SLOT_SIZE;

                if (mouseX >= sx && mouseX < sx + SLOT_SIZE
                        && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                    SniperState.targetItem = items.get(i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}