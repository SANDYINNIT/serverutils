package com.serverutils.claims;

// Blocks protected-world interactions inside claimed chunks.

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;

public final class ClaimEvents {
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            if (!ClaimService.INSTANCE.canBuild(player, level, level.getChunk(event.getPos()).getPos())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.protected"));
            }
        }
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getLevel() instanceof ServerLevel level) {
            if (!ClaimService.INSTANCE.canBuild(player, level, level.getChunk(event.getPos()).getPos())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.protected"));
            }
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            if (!ClaimService.INSTANCE.canBuild(player, level, level.getChunk(event.getPos()).getPos())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.protected"));
            }
        }
    }

    private ClaimEvents() {
    }
}
