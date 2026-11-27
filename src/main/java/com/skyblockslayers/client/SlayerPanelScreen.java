package com.skyblockslayers.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class SlayerPanelScreen extends Screen {
    protected static final int PANEL_WIDTH = 176;   // default: same size as the player inventory
    protected static final int PANEL_HEIGHT = 166;

    protected final int panelWidth;
    protected final int panelHeight;
    protected int left;
    protected int top;

    protected SlayerPanelScreen(Component title) {
        this(title, PANEL_WIDTH, PANEL_HEIGHT);
    }

    protected SlayerPanelScreen(Component title, int panelWidth, int panelHeight) {
        super(title);
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    @Override
    protected void init() {
        this.left = (this.width - panelWidth) / 2;
        this.top = (this.height - panelHeight) / 2;
        buildWidgets();
    }

    protected abstract void buildWidgets();

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, 0xFF000000);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xFFC6C6C6);
        graphics.fill(left, top, left + panelWidth, top + 1, 0xFFFFFFFF);
        graphics.fill(left, top, left + 1, top + panelHeight, 0xFFFFFFFF);
        graphics.fill(left, top + panelHeight - 1, left + panelWidth, top + panelHeight, 0xFF555555);
        graphics.fill(left + panelWidth - 1, top, left + panelWidth, top + panelHeight, 0xFF555555);
        graphics.drawString(this.font, this.title, left + 8, top + 8, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}