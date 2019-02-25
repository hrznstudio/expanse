package com.hrznstudio.spatial.worker.chunk;

import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.EntityBuilder;
import com.hrznstudio.spatial.worker.BaseWorker;
import improbable.Coordinates;
import improbable.Position;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;
import improbable.collections.Option;
import improbable.worker.*;
import minecraft.world.Block;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import minecraft.world.State;
import net.minecraft.init.Bootstrap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ChunkWorker extends BaseWorker.BaseViewWorker {
    public static final String CHUNK = "chunk";
    private static final Block block = new Block("minecraft:stone");
    private static final WorkerRequirementSet CHUNK_REQUIREMENT_SET = new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("chunk_worker"))));
    private final Map<Integer, State> tmpChunk;

    public ChunkWorker() {
        Bootstrap.register();
    }

    {
        tmpChunk = new LinkedHashMap<>();
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++) tmpChunk.put(getIDForPos(i, 1, j), new State(block, 0));
    }

    private void createChunk(Dispatcher dispatcher, Connection connection, Coordinates position) {
        final Option<Integer> timeoutMillis = Option.of(500);

        // Reserve an entity ID.
        RequestId<ReserveEntityIdsRequest> entityIdReservationRequestId = connection.sendReserveEntityIdsRequest(1, timeoutMillis);
        // When the reservation succeeds, create an entity with the reserved ID.

        dispatcher.onReserveEntityIdsResponse(op -> {
            if (op.requestId.equals(entityIdReservationRequestId) && op.statusCode == StatusCode.SUCCESS) {
                EntityBuilder builder = new EntityBuilder(CHUNK);
                builder.addComponent(ChunkStorage.COMPONENT, new ChunkStorageData(tmpChunk), CHUNK_REQUIREMENT_SET);
                builder.addComponent(Position.COMPONENT, new improbable.PositionData(position), CHUNK_REQUIREMENT_SET);
                connection.sendCreateEntityRequest(builder.build(), op.firstEntityId, timeoutMillis);
            }
        });
    }

    private int getIDForPos(int x, int y, int z) {
        return (x << 8) + (y << 4) + z;
    }

    @Override
    protected void onConnected() {
        createChunk(ConnectionManager.getDispatcher(), ConnectionManager.getConnection(), new Coordinates(0, 0, 0));
    }
}