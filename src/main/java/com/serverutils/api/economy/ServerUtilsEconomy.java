package com.serverutils.api.economy;

// Public economy API for Server Utils integrations.

import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public interface ServerUtilsEconomy {
    long getBalance(MinecraftServer server, UUID playerId);

    boolean canAfford(MinecraftServer server, UUID playerId, long amount);

    void deposit(MinecraftServer server, UUID playerId, long amount);

    boolean withdraw(MinecraftServer server, UUID playerId, long amount);

    TransferResult transfer(MinecraftServer server, UUID from, UUID to, long amount, int taxPercent);

    record TransferResult(boolean success, long amount, long tax, String messageKey) {
    }
}
