package com.serverutils.economy;

// Persists player balances and simple economy counters.

import com.serverutils.core.ServerUtilsConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class EconomySavedData extends SavedData {
    private static final String NAME = "serverutils_economy";
    private final Map<UUID, Long> balances = new LinkedHashMap<>();
    private long taxDeleted;
    private long transactionCount;

    public static EconomySavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(EconomySavedData::new, EconomySavedData::load, DataFixTypes.LEVEL),
                NAME
        );
    }

    private static EconomySavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        EconomySavedData data = new EconomySavedData();
        ListTag list = tag.getList("balances", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("player")) {
                data.balances.put(entry.getUUID("player"), entry.getLong("balance"));
            }
        }
        data.taxDeleted = tag.getLong("taxDeleted");
        data.transactionCount = tag.getLong("transactionCount");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        balances.forEach((player, balance) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            entry.putLong("balance", balance);
            list.add(entry);
        });
        tag.put("balances", list);
        tag.putLong("taxDeleted", taxDeleted);
        tag.putLong("transactionCount", transactionCount);
        return tag;
    }

    public long getBalance(UUID playerId) {
        return balances.computeIfAbsent(playerId, ignored -> (long) ServerUtilsConfig.STARTING_BALANCE.get());
    }

    public void setBalance(UUID playerId, long amount) {
        balances.put(playerId, Math.max(0L, amount));
        setDirty();
    }

    public void addTaxDeleted(long amount) {
        if (amount > 0) {
            taxDeleted += amount;
            setDirty();
        }
    }

    public void countTransaction() {
        transactionCount++;
        setDirty();
    }

    public Map<UUID, Long> topBalances(int limit) {
        return balances.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, LinkedHashMap::new));
    }
}
