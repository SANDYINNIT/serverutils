package com.serverutils.shop;

// Reserved saved data hook for future shop metadata.

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class ShopSavedData extends SavedData {
    private static final String NAME = "serverutils_shop";

    public static ShopSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ShopSavedData::new, ShopSavedData::load, DataFixTypes.LEVEL),
                NAME
        );
    }

    private static ShopSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        return new ShopSavedData();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        return tag;
    }
}
