package com.serverutils.claims;

// Identifies a claimed chunk by dimension and chunk position.

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public record ClaimKey(String dimension, int chunkX, int chunkZ) {
    public static ClaimKey of(ServerLevel level, ChunkPos pos) {
        return new ClaimKey(level.dimension().location().toString(), pos.x, pos.z);
    }

    public String display() {
        return dimension + " [" + chunkX + ", " + chunkZ + "]";
    }
}
