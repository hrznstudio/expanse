package com.hrznstudio.spatial.util;

import improbable.Coordinates;
import improbable.PositionData;
import net.minecraft.util.math.BlockPos;

public abstract class Converters {
    private Converters() {
    }

    public static BlockPos improbableToBlockPos(final PositionData position) {
        return improbableToBlockPos(position.getCoords());
    }

    public static BlockPos improbableToBlockPos(final Coordinates position) {
        return new BlockPos(position.getX(), position.getY(), position.getZ());
    }

    public static BlockPos improbableToChunkPos(final PositionData position) {
        return improbableToChunkPos(position.getCoords());
    }

    public static BlockPos improbableToChunkPos(final Coordinates position) {
        return new BlockPos((int) position.getX() >> 4, (int) position.getY() >> 4, (int) position.getZ() >> 4);
    }

    public static int blockPosToChunkIndex(final BlockPos position) {
        return blockPosToChunkIndex(position.getX(), position.getY(), position.getZ());
    }

    public static int blockPosToChunkIndex(final int x, final int y, final int z) {
        return (x % 16 << 8) + (y % 16 << 4) + z % 16;
    }
}
