package com.skyblockslayers.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class SlayerPanelScreen extends Screen {
    protected static final int PANEL_WIDTH = 176;   // same size as the player inventory
    protected static final int PANEL_HEIGHT = 166;

    protected int left;
    protected int top;

    protected SlayerPanelScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_WIDTH) / 2;
        this.top = (this.height - PANEL_HEIGHT) / 2;
        buildWidgets();
    }

    protected abstract void buildWidgets();

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, 0xFF000000);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFFC6C6C6);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 1, 0xFFFFFFFF);
        graphics.fill(left, top, left + 1, top + PANEL_HEIGHT, 0xFFFFFFFF);
        graphics.fill(left, top + PANEL_HEIGHT - 1, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF555555);
        graphics.fill(left + PANEL_WIDTH - 1, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF555555);
        graphics.drawString(this.font, this.title, left + 8, top + 8, 0x404040, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // the game keeps running behind the menu
    }
}