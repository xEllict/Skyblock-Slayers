package com.skyblockslayers.slayer;

import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public enum DropRarity {
    // ordered from least to most rare (the order matters)
    OCCASIONAL(ModSounds.OCCASIONAL_DROP, 1.0F, 1.0F, ChatFormatting.AQUA, "OCCASIONAL DROP!"),
    RARE(ModSounds.RARE_DROP, 1.0F, 1.0F, ChatFormatting.BLUE, "RARE DROP!"),
    VERY_RARE(ModSounds.VERY_RARE_DROP, 1.0F, 1.0F, ChatFormatting.DARK_PURPLE, "VERY RARE DROP!");

    // chance ranges, in percent
    private static final double VERY_RARE_MAX = 5.0;   // 5% or less
    private static final double RARE_MAX = 20.0;       // above 5%, below 20%
    private static final double OCCASIONAL_MAX = 50.0; // 20% up to 50%

    private final Supplier<SoundEvent> sound;
    private final float volume;
    private final float pitch;
    private final ChatFormatting color;
    private final String label;

    DropRarity(Supplier<SoundEvent> sound, float volume, float pitch, ChatFormatting color, String label) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.color = color;
        this.label = label;
    }

    public SoundEvent getSound() { return sound.get(); }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }

    /** The chat message for a dropped item, e.g. "RARE DROP! Undead Catalyst". */
    public Component createMessage(ItemStack stack) {
        Component itemName = stack.getHoverName().copy().withStyle(style -> {
            Style result = style.withBold(false)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack)));
            return result.getColor() == null ? result.withColor(stack.getRarity().color()) : result;
        });
        return Component.literal(label + " ").withStyle(color, ChatFormatting.BOLD).append(itemName);
    }

    /** The tier for a drop chance in percent, or null above 50% (no sound, no message). */
    @Nullable
    public static DropRarity fromChance(double chancePercent) {
        if (chancePercent <= VERY_RARE_MAX) return VERY_RARE;
        if (chancePercent < RARE_MAX) return RARE;
        if (chancePercent <= OCCASIONAL_MAX) return OCCASIONAL;
        return null;
    }
}