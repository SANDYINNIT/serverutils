package com.serverutils.client;

// Registers client-only shop screens during client setup.

import com.serverutils.core.ModRegistries;
import com.serverutils.core.ServerUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ServerUtils.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientShopSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModRegistries.CASH_REGISTER_MENU.get(), CashRegisterScreen::new));
    }

    private ClientShopSetup() {
    }
}
