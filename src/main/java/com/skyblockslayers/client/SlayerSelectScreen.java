package com.skyblockslayers.client;

import com.skyblockslayers.slayer.SlayerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SlayerSelectScreen extends SlayerPanelScreen {
    private static final int BUTTON_SIZE = 24;
    private static final int SIDE_PADDING = 12;
    private static final int TOP_PADDING = 32;

    private final List<IconButton> buttons = new ArrayList<>();

    public SlayerSelectScreen() {
        super(Component.literal("Slayers"));
    }

    @Override
    protected void buildWidgets() {
        buttons.clear();
        SlayerType[] types = SlayerType.values();
        int usableWidth = PANEL_WIDTH - 2 * SIDE_PADDING;
        int gap = (usableWidth - types.length * BUTTON_SIZE) / (types.length - 1);

        for (int i = 0; i < types.length; i++) {
            SlayerType type = types[i];
            IconButton button = new IconButton(
                    left + SIDE_PADDING + i * (BUTTON_SIZE + gap),
                    top + TOP_PADDING,
                    BUTTON_SIZE,
                    type.getDisplayName(),
                    type.getIcon(),
                    b -> this.minecraft.setScreen(new SlayerDetailScreen(type, this)));
            buttons.add(button);
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        for (IconButton button : buttons) {
            if (button.isHovered()) {
                graphics.renderTooltip(this.font, button.getLabel(), mouseX, mouseY);
            }
        }
    }
}