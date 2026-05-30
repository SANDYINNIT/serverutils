package com.serverutils.teleport;

// Saves homes, spawn, and back locations for the server.

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TeleportSavedData extends SavedData {
    private static final String NAME = "serverutils_teleport";
    private final Map<UUID, Map<String, StoredLocation>> homes = new HashMap<>();
    private final Map<UUID, StoredLocation> backs = new HashMap<>();
    private StoredLocation spawn;

    public static TeleportSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(TeleportSavedData::new, TeleportSavedData::load, DataFixTypes.LEVEL),
                NAME
        );
    }

    private static TeleportSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TeleportSavedData data = new TeleportSavedData();
        ListTag players = tag.getList("homes", 10);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag playerTag = players.getCompound(i);
            if (!playerTag.hasUUID("player")) {
                continue;
            }
            UUID playerId = playerTag.getUUID("player");
            ListTag homeList = playerTag.getList("entries", 10);
            Map<String, StoredLocation> playerHomes = new LinkedHashMap<>();
            for (int h = 0; h < homeList.size(); h++) {
                CompoundTag homeTag = homeList.getCompound(h);
                playerHomes.put(homeTag.getString("name"), StoredLocation.load(homeTag.getCompound("location")));
            }
            data.homes.put(playerId, playerHomes);
        }
        ListTag backs = tag.getList("backs", 10);
        for (int i = 0; i < backs.size(); i++) {
            CompoundTag backTag = backs.getCompound(i);
            if (backTag.hasUUID("player")) {
                data.backs.put(backTag.getUUID("player"), StoredLocation.load(backTag.getCompound("location")));
            }
        }
        if (tag.contains("spawn", 10)) {
            data.spawn = StoredLocation.load(tag.getCompound("spawn"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag playerHomes = new ListTag();
        homes.forEach((player, entries) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player", player);
            ListTag homeList = new ListTag();
            entries.forEach((name, location) -> {
                CompoundTag homeTag = new CompoundTag();
                homeTag.putString("name", name);
                homeTag.put("location", location.save());
                homeList.add(homeTag);
            });
            playerTag.put("entries", homeList);
            playerHomes.add(playerTag);
        });
        tag.put("homes", playerHomes);

        ListTag backList = new ListTag();
        backs.forEach((player, location) -> {
            CompoundTag backTag = new CompoundTag();
            backTag.putUUID("player", player);
            backTag.put("location", location.save());
            backList.add(backTag);
        });
        tag.put("backs", backList);
        if (spawn != null) {
            tag.put("spawn", spawn.save());
        }
        return tag;
    }

    public Map<String, StoredLocation> homes(UUID playerId) {
        return homes.computeIfAbsent(playerId, ignored -> new LinkedHashMap<>());
    }

    public void setHome(UUID playerId, String name, StoredLocation location) {
        homes(playerId).put(name.toLowerCase(), location);
        setDirty();
    }

    public boolean deleteHome(UUID playerId, String name) {
        boolean removed = homes(playerId).remove(name.toLowerCase()) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public Optional<StoredLocation> getHome(UUID playerId, String name) {
        return Optional.ofNullable(homes(playerId).get(name.toLowerCase()));
    }

    public void setBack(UUID playerId, StoredLocation location) {
        backs.put(playerId, location);
        setDirty();
    }

    public Optional<StoredLocation> getBack(UUID playerId) {
        return Optional.ofNullable(backs.get(playerId));
    }

    public void setSpawn(StoredLocation location) {
        spawn = location;
        setDirty();
    }

    public Optional<StoredLocation> getSpawn() {
        return Optional.ofNullable(spawn);
    }
}
