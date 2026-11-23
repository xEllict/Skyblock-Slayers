package com.skyblockslayers.client;

import com.skyblockslayers.slayer.SlayerType;
import com.skyblockslayers.entities.RevenantTier;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class SlayerDetailScreen extends SlayerPanelScreen {
    private static final int TIER_COUNT = 5;
    private static final int BUTTON_WIDTH = 30;
    private static final int BUTTON_HEIGHT = 20;
    private static final int LEFT_PADDING = 10;
    private static final int TOP_PADDING = 24;
    private static final int SPACING = 4;

    private final SlayerType slayer;
    private final Screen parent;

    private final List<Button> tierButtons = new ArrayList<>();
    private static final int WINDOW_PADDING = 4;
    private static final int SCROLLBAR_WIDTH = 4;

    private int selectedTier = 0;
    private double scrollOffset = 0;
    private List<FormattedCharSequence> wrappedLines = new ArrayList<>();

    public SlayerDetailScreen(SlayerType slayer, Screen parent) {
        super(slayer.getDisplayName());
        this.slayer = slayer;
        this.parent = parent;
    }

    @Override
    protected void buildWidgets() {
        tierButtons.clear();
        for (int i = 0; i < TIER_COUNT; i++) {
            int tier = i + 1;
            Button button = Button.builder(Component.literal("T" + tier), b -> onTierClicked(tier))
                    .bounds(left + LEFT_PADDING,
                            top + TOP_PADDING + i * (BUTTON_HEIGHT + SPACING),
                            BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build();
            tierButtons.add(button);
            this.addRenderableWidget(button);
        }
    }

    // The hover text for each tier button. Put your own info here (use \n for new lines).
    private Component getTierInfo(int tier) {
        MutableComponent text = Component.empty();
        if(slayer == SlayerType.REVENANT_HORROR){
            RevenantTier stats = RevenantTier.byTier(tier);
            double damage = stats.getDamage();
            double health = stats.getHealth();
            if (tier == 1) {
                text.append(colored(slayer.getDisplayName().getString() + " " + tier + "\n", ChatFormatting.GREEN));
                text.append(colored("Beginner\n\n", ChatFormatting.GRAY));
            } else if (tier == 2) {
                text.append(colored(slayer.getDisplayName().getString() + " " + tier + "\n", ChatFormatting.YELLOW));
                text.append(colored("Strong\n\n", ChatFormatting.GRAY));
            } else if (tier == 3) {
                text.append(colored(slayer.getDisplayName().getString() + " " + tier + "\n", ChatFormatting.RED));
                text.append(colored("Challenging\n\n", ChatFormatting.GRAY));
            } else if (tier == 4) {
                text.append(colored(slayer.getDisplayName().getString() + " " + tier + "\n", ChatFormatting.DARK_RED));
                text.append(colored("Deadly\n\n", ChatFormatting.GRAY));
            } else if (tier == 5) {
                text.append(colored(slayer.getDisplayName().getString() + " " + tier + "\n", ChatFormatting.DARK_PURPLE));
                text.append(colored("Excruciating \n\n", ChatFormatting.GRAY));
            }
            text.append(colored("Health: ", ChatFormatting.GRAY));
            text.append(colored(formatNumber(health), ChatFormatting.WHITE));
            text.append(colored("♥\n", ChatFormatting.RED));
            text.append(colored("Melee Damage: ", ChatFormatting.GRAY));
            text.append(colored(formatNumber(damage), ChatFormatting.WHITE));
            text.append(colored("🗡\n\n", ChatFormatting.RED));

            text.append(colored("Life Drain", ChatFormatting.RED));
            text.append(colored(": ", ChatFormatting.WHITE));
            text.append(colored("Drains ", ChatFormatting.GRAY));
            text.append(colored(formatNumber(damage), ChatFormatting.WHITE));
            text.append(colored("♥", ChatFormatting.RED));
            text.append(colored(" every few seconds.\n", ChatFormatting.GRAY));

            if (tier == 2 || tier == 3 || tier == 4) {
                text.append(colored("Pestilence", ChatFormatting.YELLOW));
                text.append(colored(": Does ", ChatFormatting.GRAY));
                text.append(colored(formatNumber(damage/2), ChatFormatting.WHITE));
                text.append(colored("🗡 ", ChatFormatting.RED));
                text.append(colored("damage to all players in a 2.5 block radius every second, 25% of which ignores armour.\n", ChatFormatting.GRAY));
            }
            if (tier == 3 || tier == 4) {
                text.append(colored("Enrage", ChatFormatting.DARK_RED));
                text.append(colored(": After 20 seconds, the boss becomes ", ChatFormatting.GRAY));
                text.append(colored("Enraged", ChatFormatting.DARK_RED));
                text.append(colored(", dealing double melee damage for 6 seconds.\n", ChatFormatting.GRAY));
            }

            text.append("\n\n"); // 2 blank lines
            text.append(colored("Reward: ", ChatFormatting.GRAY));
            text.append(colored("5 Slayer XP\n", ChatFormatting.LIGHT_PURPLE));
            text.append(colored("+ Boss Drops", ChatFormatting.GRAY));
        }
        return text;
    }

    private static MutableComponent colored(String text, ChatFormatting... formatting) {
        return Component.literal(text).withStyle(formatting);
    }

    private static String formatNumber(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    // Called when a tier button is clicked. Nothing happens yet.
    private void onTierClicked(int tier) {
    }

    // ---- text size (1.0 = vanilla size, lower = smaller) ----
    private static final float TEXT_SCALE = 0.75F;

    private int lineHeight() { return Math.max(1, Math.round(this.font.lineHeight * TEXT_SCALE)); }

    // ---- window position (the lower half of the space to the right of the buttons) ----
    private int boxLeft() { return left + LEFT_PADDING + BUTTON_WIDTH + 8; }
    private int boxRight() { return left + PANEL_WIDTH - 8; }
    private int boxBottom() { return top + PANEL_HEIGHT - 8; }
    private int boxTop() {
        int regionTop = top + TOP_PADDING;
        return regionTop + (boxBottom() - regionTop) / 2;
    }
    private int viewHeight() { return boxBottom() - boxTop() - 2 * WINDOW_PADDING; }
    private int textWidth() { return boxRight() - boxLeft() - 2 * WINDOW_PADDING - SCROLLBAR_WIDTH - 2; }
    private int maxScroll() { return Math.max(0, wrappedLines.size() * lineHeight() - viewHeight()); }

    private void selectTier(int tier) {
        selectedTier = tier;
        scrollOffset = 0;
        wrappedLines = this.font.split(getTierInfo(tier), (int) (textWidth() / TEXT_SCALE));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        for (int i = 0; i < tierButtons.size(); i++) {
            if (tierButtons.get(i).isHovered() && selectedTier != i + 1) {
                selectTier(i + 1);
            }
        }
        if (selectedTier == 0) {
            return; // nothing hovered yet
        }

        graphics.fill(boxLeft(), boxTop(), boxRight(), boxBottom(), 0xF0100010);

        int viewTop = boxTop() + WINDOW_PADDING;
        int max = maxScroll();
        scrollOffset = Mth.clamp(scrollOffset, 0, max);

        // text, clipped to the window
        graphics.enableScissor(boxLeft(), viewTop, boxRight() - SCROLLBAR_WIDTH - 2, viewTop + viewHeight());
        int y = viewTop - (int) scrollOffset;
        for (FormattedCharSequence line : wrappedLines) {
            graphics.pose().pushPose();
            graphics.pose().translate(boxLeft() + WINDOW_PADDING, y, 0);
            graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
            graphics.drawString(this.font, line, 0, 0, 0xFFFFFF, false);
            graphics.pose().popPose();
            y += lineHeight();
        }
        graphics.disableScissor();

        // scrollbar (only when the text doesn't fit)
        if (max > 0) {
            int barX = boxRight() - SCROLLBAR_WIDTH - 2;
            int contentHeight = wrappedLines.size() * lineHeight();
            int thumbHeight = Math.max(10, viewHeight() * viewHeight() / contentHeight);
            int thumbY = viewTop + (int) ((viewHeight() - thumbHeight) * (scrollOffset / max));
            graphics.fill(barX, viewTop, barX + SCROLLBAR_WIDTH, viewTop + viewHeight(), 0xFF303030);
            graphics.fill(barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= boxLeft() && mouseX <= boxRight() && mouseY >= boxTop() && mouseY <= boxBottom()) {
            scrollOffset = Mth.clamp(scrollOffset - scrollY * lineHeight(), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent); // Esc goes back to the main menu instead of closing everything
    }
}