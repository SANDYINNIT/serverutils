package com.serverutils.claims;

// Owns the main chunk claim actions and permission checks.

import com.serverutils.costs.Costs;
import com.serverutils.economy.EconomyService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public final class ClaimService {
    public static final ClaimService INSTANCE = new ClaimService();

    private ClaimService() {
    }

    public ClaimKey currentKey(ServerPlayer player) {
        ChunkPos chunk = player.chunkPosition();
        return ClaimKey.of((ServerLevel) player.level(), chunk);
    }

    public boolean claim(ServerPlayer player) {
        ClaimSavedData data = ClaimSavedData.get(player.server);
        ClaimKey key = currentKey(player);
        if (data.get(key).isPresent()) {
            player.sendSystemMessage(Component.translatable("commands.serverutils.claim.exists"));
            return false;
        }
        int cost = Costs.INSTANCE.claimCost();
        if (!EconomyService.INSTANCE.withdraw(player.server, player.getUUID(), cost)) {
            player.sendSystemMessage(Component.translatable("commands.serverutils.money.insufficient"));
            return false;
        }
        long nextRent = player.server.overworld().getGameTime() + Costs.INSTANCE.rentIntervalTicks();
        data.put(key, new Claim(player.getUUID(), nextRent));
        player.sendSystemMessage(Component.translatable("commands.serverutils.claim.created", key.display(), cost));
        return true;
    }

    public boolean unclaim(ServerPlayer player) {
        ClaimSavedData data = ClaimSavedData.get(player.server);
        ClaimKey key = currentKey(player);
        return data.get(key).map(claim -> {
            if (!claim.owner().equals(player.getUUID()) && !player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.not_owner"));
                return false;
            }
            data.remove(key);
            player.sendSystemMessage(Component.translatable("commands.serverutils.claim.removed", key.display()));
            return true;
        }).orElseGet(() -> {
            player.sendSystemMessage(Component.translatable("commands.serverutils.claim.none"));
            return false;
        });
    }

    public boolean trust(ServerPlayer player, ServerPlayer target) {
        return editTrust(player, target.getUUID(), true, target.getGameProfile().getName());
    }

    public boolean untrust(ServerPlayer player, ServerPlayer target) {
        return editTrust(player, target.getUUID(), false, target.getGameProfile().getName());
    }

    private boolean editTrust(ServerPlayer player, UUID target, boolean add, String name) {
        ClaimSavedData data = ClaimSavedData.get(player.server);
        ClaimKey key = currentKey(player);
        return data.get(key).map(claim -> {
            if (!claim.owner().equals(player.getUUID())) {
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.not_owner"));
                return false;
            }
            if (add) {
                claim.trusted().add(target);
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.trusted", name));
            } else {
                claim.trusted().remove(target);
                player.sendSystemMessage(Component.translatable("commands.serverutils.claim.untrusted", name));
            }
            data.dirty();
            return true;
        }).orElseGet(() -> {
            player.sendSystemMessage(Component.translatable("commands.serverutils.claim.none"));
            return false;
        });
    }

    public boolean canBuild(ServerPlayer player, ServerLevel level, ChunkPos chunk) {
        if (player.hasPermissions(2)) {
            return true;
        }
        return ClaimSavedData.get(player.server)
                .get(ClaimKey.of(level, chunk))
                .map(claim -> claim.canBuild(player.getUUID()))
                .orElse(true);
    }
}
