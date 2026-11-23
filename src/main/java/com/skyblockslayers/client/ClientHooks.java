package com.skyblockslayers.client;

import net.minecraft.client.Minecraft;

public class ClientHooks {
    public static void openSlayerMenu() {
        Minecraft.getInstance().setScreen(new SlayerSelectScreen());
    }
}