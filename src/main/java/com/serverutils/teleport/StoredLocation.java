package com.serverutils.teleport;

// Represents a saved cross-dimension teleport destination.

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;

import java.util.Optional;

public record StoredLocation(String dimension, double x, double y, double z, float yaw, float pitch) {
    public static StoredLocation of(ServerPlayer player) {
        return new StoredLocation(player.level().dimension().location().toString(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
    }

    public static StoredLocation of(ServerLevel level, BlockPos pos) {
        return new StoredLocation(level.dimension().location().toString(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
    }

    public static StoredLocation load(CompoundTag tag) {
        return new StoredLocation(tag.getString("dimension"), tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"), tag.getFloat("yaw"), tag.getFloat("pitch"));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", dimension);
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        return tag;
    }

    public Optional<ServerLevel> resolve(MinecraftServer server) {
        ResourceLocation location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            return Optional.empty();
        }
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, location);
        return Optional.ofNullable(server.getLevel(key));
    }
}
