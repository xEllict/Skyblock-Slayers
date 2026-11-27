package com.skyblockslayers.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.skyblockslayers.slayer.ModAttachments;
import com.skyblockslayers.slayer.SlayerData;
import com.skyblockslayers.slayer.SlayerLevels;
import com.skyblockslayers.slayer.SlayerProgress;
import com.skyblockslayers.slayer.SlayerType;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = "skyblockslayers")
public class SlayerDebugCommand {
    private static final SimpleCommandExceptionType UNKNOWN_SLAYER =
            new SimpleCommandExceptionType(Component.literal("Unknown slayer"));

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("slayerdebug")
                .requires(source -> source.hasPermission(2)) // operators only
                .then(Commands.literal("reset")
                        .then(Commands.literal("kills")
                                .then(slayer().then(tier().then(target().executes(SlayerDebugCommand::resetKills)))))
                        .then(Commands.literal("xp")
                                .then(slayer().then(target().executes(SlayerDebugCommand::resetXp))))
                        .then(Commands.literal("rngmeter")
                                .then(slayer().then(target().executes(SlayerDebugCommand::resetRngMeter)))))
                .then(Commands.literal("set")
                        .then(Commands.literal("kills")
                                .then(slayer().then(tier().then(value().then(target().executes(SlayerDebugCommand::setKills))))))
                        .then(Commands.literal("xp")
                                .then(slayer().then(value().then(target().executes(SlayerDebugCommand::setXp)))))
                        .then(Commands.literal("rngmeter")
                                .then(slayer().then(value().then(target().executes(SlayerDebugCommand::setRngMeter))))))
                .then(Commands.literal("call")
                        .then(Commands.literal("kills")
                                .then(slayer().then(tier().then(target().executes(SlayerDebugCommand::callKills)))))
                        .then(Commands.literal("xp")
                                .then(slayer().then(target().executes(SlayerDebugCommand::callXp))))
                        .then(Commands.literal("rngmeter")
                                .then(slayer().then(target().executes(SlayerDebugCommand::callRngMeter))))));
    }

    // ---------- argument definitions ----------

    private static RequiredArgumentBuilder<CommandSourceStack, String> slayer() {
        return Commands.argument("slayer", StringArgumentType.word())
                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(SlayerType.values()).map(SlayerType::getSerializedName), builder));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> tier() {
        return Commands.argument("tier", IntegerArgumentType.integer(1, 5));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Integer> value() {
        return Commands.argument("value", IntegerArgumentType.integer(0));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> target() {
        return Commands.argument("target", EntityArgument.player());
    }

    // ---------- reading the arguments ----------

    private static SlayerType slayerOf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "slayer");
        for (SlayerType type : SlayerType.values()) {
            if (type.getSerializedName().equals(name)) {
                return type;
            }
        }
        throw UNKNOWN_SLAYER.create();
    }

    private static ServerPlayer targetOf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return EntityArgument.getPlayer(ctx, "target");
    }

    private static int tierOf(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "tier");
    }

    private static int valueOf(CommandContext<CommandSourceStack> ctx) {
        return IntegerArgumentType.getInteger(ctx, "value");
    }

    // ---------- changing and reading the data ----------

    private static void update(ServerPlayer player, SlayerType slayer, UnaryOperator<SlayerProgress> change) {
        SlayerData data = player.getData(ModAttachments.SLAYER_DATA);
        player.setData(ModAttachments.SLAYER_DATA, data.with(slayer, change.apply(data.get(slayer))));
    }

    private static SlayerProgress progressOf(ServerPlayer player, SlayerType slayer) {
        return player.getData(ModAttachments.SLAYER_DATA).get(slayer);
    }

    private static void say(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal(message), false);
    }

    // ---------- kills ----------

    private static int resetKills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int tier = tierOf(ctx);
        update(target, slayer, p -> p.withCompletions(tier, 0));
        say(ctx, "Reset " + target.getName().getString() + "'s T" + tier + " "
                + slayer.getDisplayName().getString() + " kills to 0.");
        return 1;
    }

    private static int setKills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int tier = tierOf(ctx);
        int value = valueOf(ctx);
        update(target, slayer, p -> p.withCompletions(tier, value));
        say(ctx, "Set " + target.getName().getString() + "'s T" + tier + " "
                + slayer.getDisplayName().getString() + " kills to " + value + ".");
        return 1;
    }

    private static int callKills(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int tier = tierOf(ctx);
        int kills = progressOf(target, slayer).getCompletions(tier);
        say(ctx, target.getName().getString() + " has " + kills + " T" + tier + " "
                + slayer.getDisplayName().getString() + " kills.");
        return kills;
    }

    // ---------- xp ----------

    private static int resetXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        update(target, slayer, p -> p.withXp(0));
        say(ctx, "Reset " + target.getName().getString() + "'s " + slayer.getDisplayName().getString() + " XP to 0.");
        return 1;
    }

    private static int setXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int value = valueOf(ctx);
        update(target, slayer, p -> p.withXp(value));
        say(ctx, "Set " + target.getName().getString() + "'s " + slayer.getDisplayName().getString()
                + " XP to " + value + " (level " + SlayerLevels.getLevel(slayer, value) + ").");
        return 1;
    }

    private static int callXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int xp = progressOf(target, slayer).xp();
        say(ctx, target.getName().getString() + " has " + xp + " " + slayer.getDisplayName().getString()
                + " XP (level " + SlayerLevels.getLevel(slayer, xp) + ").");
        return xp;
    }

    // ---------- rng meter ----------

    private static int resetRngMeter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        update(target, slayer, p -> p.withRngMeterXp(0));
        say(ctx, "Reset " + target.getName().getString() + "'s " + slayer.getDisplayName().getString()
                + " rng meter to 0.");
        return 1;
    }

    private static int setRngMeter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int value = valueOf(ctx);
        update(target, slayer, p -> p.withRngMeterXp(value));
        say(ctx, "Set " + target.getName().getString() + "'s " + slayer.getDisplayName().getString()
                + " rng meter to " + value + ".");
        return 1;
    }

    private static int callRngMeter(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        SlayerType slayer = slayerOf(ctx);
        ServerPlayer target = targetOf(ctx);
        int meter = progressOf(target, slayer).rngMeterXp();
        say(ctx, target.getName().getString() + " has " + meter + " " + slayer.getDisplayName().getString()
                + " rng meter XP.");
        return meter;
    }
}