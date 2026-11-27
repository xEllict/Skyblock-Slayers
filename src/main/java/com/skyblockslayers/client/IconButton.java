package com.skyblockslayers.client;

import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends AbstractButton {
    private final Component label;
    private final ResourceLocation icon;
    private final Consumer<IconButton> action;

    public IconButton(int x, int y, int size, Component label, ResourceLocation icon, Consumer<IconButton> action) {
        super(x, y, size, size, Component.empty());
        this.label = label;
        this.icon = icon;
        this.action = action;
    }

    public Component getLabel() {
        return label;
    }

    @Override
    public void onPress() {
        action.accept(this);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (!this.active) {
            graphics.setColor(0.4F, 0.4F, 0.4F, 1.0F); // darkened when locked
        }
        graphics.blit(icon, getX() + (getWidth() - 16) / 2, getY() + (getHeight() - 16) / 2, 0, 0, 16, 16, 16, 16);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, label);
    }
}