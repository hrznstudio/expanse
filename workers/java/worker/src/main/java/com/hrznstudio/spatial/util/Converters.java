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
        return (position.getX() & 0xFFFFFF00) + ((position.getY() >> 2) & 0xFFFFF0F0) + (position.getY() >> 4);
    }
}
