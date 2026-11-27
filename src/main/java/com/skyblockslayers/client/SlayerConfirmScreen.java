package com.skyblockslayers.client;

import com.skyblockslayers.slayer.SlayerRequirements;
import com.skyblockslayers.slayer.SlayerType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import com.skyblockslayers.network.StartQuestPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public class SlayerConfirmScreen extends SlayerPanelScreen {
    private static final int WIDTH = 160;
    private static final int HEIGHT = 90;
    private static final int BUTTON_SIZE = 24;
    private static final int BUTTON_GAP = 24;

    private static final ResourceLocation CONFIRM_ICON =
            ResourceLocation.fromNamespaceAndPath("skyblockslayers", "textures/gui/confirm.png");
    private static final ResourceLocation DENY_ICON =
            ResourceLocation.fromNamespaceAndPath("skyblockslayers", "textures/gui/deny.png");

    private final SlayerType slayer;
    private final int tier;
    private final Screen parent;
    private final List<IconButton> buttons = new ArrayList<>();

    public SlayerConfirmScreen(SlayerType slayer, int tier, Screen parent) {
        super(Component.literal(slayer.getDisplayName().getString() + " " + SlayerRequirements.toRoman(tier)),
                WIDTH, HEIGHT);
        this.slayer = slayer;
        this.tier = tier;
        this.parent = parent;
    }

    @Override
    protected void buildWidgets() {
        buttons.clear();
        int totalWidth = 2 * BUTTON_SIZE + BUTTON_GAP;
        int startX = left + (panelWidth - totalWidth) / 2;
        int y = top + panelHeight - BUTTON_SIZE - 10;

        IconButton confirm = new IconButton(startX, y, BUTTON_SIZE,
                Component.literal("Confirm"), CONFIRM_ICON, b -> onConfirm());
        IconButton deny = new IconButton(startX + BUTTON_SIZE + BUTTON_GAP, y, BUTTON_SIZE,
                Component.literal("Deny"), DENY_ICON, b -> onClose());

        buttons.add(confirm);
        buttons.add(deny);
        this.addRenderableWidget(confirm);
        this.addRenderableWidget(deny);
    }

    private void onConfirm() {
        PacketDistributor.sendToServer(new StartQuestPayload(slayer, tier));
        this.minecraft.setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // the question, wrapped and centered
        List<FormattedCharSequence> lines = this.font.split(
                Component.literal("Are you sure you want to start this slayer quest?"), panelWidth - 16);
        int y = top + 24;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, left + (panelWidth - this.font.width(line)) / 2, y, 0x404040, false);
            y += this.font.lineHeight;
        }

        for (IconButton button : buttons) {
            if (button.isHovered()) {
                graphics.renderTooltip(this.font, button.getLabel(), mouseX, mouseY);
            }
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent); // deny and Esc both go back to the slayer's menu
    }
}