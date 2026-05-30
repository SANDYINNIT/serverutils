package com.serverutils.economy;

// Provides the server-side money operations used by all modules.

import com.serverutils.api.economy.ServerUtilsEconomy;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;

public final class EconomyService implements ServerUtilsEconomy {
    public static final EconomyService INSTANCE = new EconomyService();

    private EconomyService() {
    }

    @Override
    public long getBalance(MinecraftServer server, UUID playerId) {
        return EconomySavedData.get(server).getBalance(playerId);
    }

    @Override
    public boolean canAfford(MinecraftServer server, UUID playerId, long amount) {
        return amount >= 0 && getBalance(server, playerId) >= amount;
    }

    @Override
    public void deposit(MinecraftServer server, UUID playerId, long amount) {
        if (amount <= 0) {
            return;
        }
        EconomySavedData data = EconomySavedData.get(server);
        data.setBalance(playerId, data.getBalance(playerId) + amount);
    }

    @Override
    public boolean withdraw(MinecraftServer server, UUID playerId, long amount) {
        if (amount < 0) {
            return false;
        }
        EconomySavedData data = EconomySavedData.get(server);
        long balance = data.getBalance(playerId);
        if (balance < amount) {
            return false;
        }
        data.setBalance(playerId, balance - amount);
        return true;
    }

    @Override
    public TransferResult transfer(MinecraftServer server, UUID from, UUID to, long amount, int taxPercent) {
        if (amount <= 0) {
            return new TransferResult(false, amount, 0, "commands.serverutils.amount.invalid");
        }
        EconomySavedData data = EconomySavedData.get(server);
        long senderBalance = data.getBalance(from);
        if (senderBalance < amount) {
            return new TransferResult(false, amount, 0, "commands.serverutils.money.insufficient");
        }

        long tax = Math.max(0L, Math.min(amount, amount * Math.max(0, taxPercent) / 100L));
        long payout = amount - tax;
        data.setBalance(from, senderBalance - amount);
        data.setBalance(to, data.getBalance(to) + payout);
        data.addTaxDeleted(tax);
        data.countTransaction();
        return new TransferResult(true, amount, tax, "commands.serverutils.money.transfer.success");
    }

    public void removeAsTax(MinecraftServer server, long amount) {
        EconomySavedData.get(server).addTaxDeleted(amount);
    }

    public Map<UUID, Long> topBalances(MinecraftServer server, int limit) {
        return EconomySavedData.get(server).topBalances(limit);
    }

    public void setBalance(MinecraftServer server, UUID playerId, long amount) {
        EconomySavedData.get(server).setBalance(playerId, amount);
    }
}
