package com.skyblockslayers.items;

import com.skyblockslayers.client.ClientHooks;
import com.skyblockslayers.slayer.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MaddoxBatphoneItem extends Item {
    public MaddoxBatphoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack); // offhand does nothing
        }
        if (level.isClientSide) {
            ClientHooks.openSlayerMenu();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}