package com.serverutils.teleport;

// Handles paid teleports and temporary TPA requests.

import com.serverutils.costs.Costs;
import com.serverutils.economy.EconomyService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TeleportService {
    public static final TeleportService INSTANCE = new TeleportService();
    private final Map<UUID, Request> requestsByTarget = new HashMap<>();

    private TeleportService() {
    }

    public boolean charge(ServerPlayer player, long cost) {
        if (cost <= 0) {
            return true;
        }
        if (!EconomyService.INSTANCE.withdraw(player.server, player.getUUID(), cost)) {
            player.sendSystemMessage(Component.translatable("commands.serverutils.money.insufficient"));
            return false;
        }
        return true;
    }

    public void teleport(ServerPlayer player, StoredLocation location) {
        Optional<net.minecraft.server.level.ServerLevel> level = location.resolve(player.server);
        if (level.isEmpty()) {
            player.sendSystemMessage(Component.translatable("commands.serverutils.teleport.dimension_missing"));
            return;
        }
        TeleportSavedData.get(player.server).setBack(player.getUUID(), StoredLocation.of(player));
        player.teleportTo(level.get(), location.x(), location.y(), location.z(), location.yaw(), location.pitch());
    }

    public void request(ServerPlayer requester, ServerPlayer target) {
        if (!charge(requester, Costs.INSTANCE.tpaCost())) {
            return;
        }
        requestsByTarget.put(target.getUUID(), new Request(requester.getUUID(), requester.server.overworld().getGameTime()));
        requester.sendSystemMessage(Component.translatable("commands.serverutils.tpa.sent", target.getGameProfile().getName()));
        target.sendSystemMessage(Component.translatable("commands.serverutils.tpa.received", requester.getGameProfile().getName()));
    }

    public void accept(ServerPlayer target) {
        Request request = requestsByTarget.remove(target.getUUID());
        if (request == null || expired(target.server, request)) {
            target.sendSystemMessage(Component.translatable("commands.serverutils.tpa.none"));
            return;
        }
        ServerPlayer requester = target.server.getPlayerList().getPlayer(request.requester);
        if (requester == null) {
            target.sendSystemMessage(Component.translatable("commands.serverutils.player.offline"));
            return;
        }
        teleport(requester, StoredLocation.of(target));
        target.sendSystemMessage(Component.translatable("commands.serverutils.tpa.accepted"));
    }

    public void deny(ServerPlayer target) {
        if (requestsByTarget.remove(target.getUUID()) == null) {
            target.sendSystemMessage(Component.translatable("commands.serverutils.tpa.none"));
        } else {
            target.sendSystemMessage(Component.translatable("commands.serverutils.tpa.denied"));
        }
    }

    private boolean expired(MinecraftServer server, Request request) {
        return server.overworld().getGameTime() - request.createdGameTime > Costs.INSTANCE.tpaExpireTicks();
    }

    private record Request(UUID requester, long createdGameTime) {
    }
}
