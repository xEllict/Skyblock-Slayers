package com.skyblockslayers.client;

import com.skyblockslayers.slayer.ModAttachments;
import com.skyblockslayers.slayer.SlayerData;
import com.skyblockslayers.slayer.SlayerRequirements;
import com.skyblockslayers.slayer.SlayerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class SlayerSelectScreen extends SlayerPanelScreen {
    private static final int BUTTON_SIZE = 24;
    private static final int SIDE_PADDING = 12;
    private static final int TOP_PADDING = 50;

    private final List<IconButton> buttons = new ArrayList<>();

    public SlayerSelectScreen() {
        super(Component.literal("Slayers"));
    }

    @Override
    protected void buildWidgets() {
        buttons.clear();
        SlayerData data = this.minecraft.player.getData(ModAttachments.SLAYER_DATA);
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
            button.active = SlayerRequirements.isSlayerUnlocked(data, type); // locked buttons can't be clicked
            buttons.add(button);
            this.addRenderableWidget(button);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        SlayerType[] types = SlayerType.values();
        for (int i = 0; i < buttons.size(); i++) {
            IconButton button = buttons.get(i);
            if (!button.isHovered()) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            lines.add(button.getLabel());

            SlayerType prerequisite = SlayerRequirements.getPrerequisite(types[i]);
            if (!button.active && prerequisite != null) {
                lines.add(Component.literal("Kill a Tier "
                                + SlayerRequirements.toRoman(SlayerRequirements.REQUIRED_PREVIOUS_SLAYER_TIER)
                                + " " + prerequisite.getDisplayName().getString() + " to unlock!")
                        .withStyle(ChatFormatting.RED));
            }
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }
}