package com.serverutils.teleport;

// Captures player events that affect teleport history.

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public final class TeleportEvents {
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeleportSavedData.get(player.server).setBack(player.getUUID(), StoredLocation.of(player));
        }
    }

    private TeleportEvents() {
    }
}
