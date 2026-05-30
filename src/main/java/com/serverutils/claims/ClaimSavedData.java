package com.serverutils.claims;

// Stores all chunk claims and their rent state.

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimSavedData extends SavedData {
    private static final String NAME = "serverutils_claims";
    private final Map<ClaimKey, Claim> claims = new HashMap<>();

    public static ClaimSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ClaimSavedData::new, ClaimSavedData::load, DataFixTypes.LEVEL),
                NAME
        );
    }

    private static ClaimSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ClaimSavedData data = new ClaimSavedData();
        ListTag list = tag.getList("claims", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("owner")) {
                continue;
            }
            ClaimKey key = new ClaimKey(entry.getString("dimension"), entry.getInt("chunkX"), entry.getInt("chunkZ"));
            Set<UUID> trusted = entry.getList("trusted", 10).stream()
                    .filter(CompoundTag.class::isInstance)
                    .map(CompoundTag.class::cast)
                    .filter(trustedTag -> trustedTag.hasUUID("player"))
                    .map(trustedTag -> trustedTag.getUUID("player"))
                    .collect(Collectors.toSet());
            data.claims.put(key, new Claim(entry.getUUID("owner"), trusted, entry.getLong("nextRentDue"), entry.getBoolean("unpaid")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        claims.forEach((key, claim) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", key.dimension());
            entry.putInt("chunkX", key.chunkX());
            entry.putInt("chunkZ", key.chunkZ());
            entry.putUUID("owner", claim.owner());
            entry.putLong("nextRentDue", claim.nextRentDueGameTime());
            entry.putBoolean("unpaid", claim.unpaid());
            ListTag trusted = new ListTag();
            claim.trusted().forEach(player -> {
                CompoundTag trustedTag = new CompoundTag();
                trustedTag.putUUID("player", player);
                trusted.add(trustedTag);
            });
            entry.put("trusted", trusted);
            list.add(entry);
        });
        tag.put("claims", list);
        return tag;
    }

    public Optional<Claim> get(ClaimKey key) {
        return Optional.ofNullable(claims.get(key));
    }

    public Map<ClaimKey, Claim> claims() {
        return claims;
    }

    public void put(ClaimKey key, Claim claim) {
        claims.put(key, claim);
        setDirty();
    }

    public boolean remove(ClaimKey key) {
        boolean removed = claims.remove(key) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public void dirty() {
        setDirty();
    }
}
