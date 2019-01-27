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

    public static int blockPosToChunkIndex(final BlockPos position) {
        return (position.getX() % 16 << 8) + (position.getY() % 16 << 4) + position.getZ() % 16;
    }
}
