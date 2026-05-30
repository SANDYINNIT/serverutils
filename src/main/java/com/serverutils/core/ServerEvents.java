package com.serverutils.core;

// Bridges Forge events into the Server Utils modules.

import com.serverutils.claims.ClaimEvents;
import com.serverutils.claims.ClaimRentService;
import com.serverutils.commands.ServerUtilsCommands;
import com.serverutils.teleport.TeleportEvents;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ServerEvents {
    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        ServerUtilsCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ClaimRentService.processLogin(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        ClaimRentService.processServerTick(event);
    }

    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        ClaimEvents.onBlockBreak(event);
    }

    @SubscribeEvent
    public static void onBlockPlace(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent event) {
        ClaimEvents.onBlockPlace(event);
    }

    @SubscribeEvent
    public static void onRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        ClaimEvents.onRightClickBlock(event);
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        TeleportEvents.onPlayerDeath(event);
    }

    private ServerEvents() {
    }
}
