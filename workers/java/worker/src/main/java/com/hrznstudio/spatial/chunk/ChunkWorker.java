package com.hrznstudio.spatial.chunk;

import com.hrznstudio.spatial.WorkerService;
import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.EntityBuilder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkWorker implements WorkerService {
    private static final String CHUNK = "chunk";

    private final UUID uuid = UUID.randomUUID();
    private final String name = getClass().getSimpleName() + "$" + uuid;
    private final Logger logger = LogManager.getLogger(name);
    private static final Block block = new Block("minecraft:stone");
    private final Map<Integer, State> tmpChunk;
    private static final WorkerRequirementSet CHUNK_REQUIREMENT_SET = new WorkerRequirementSet(Collections.singletonList(new WorkerAttributeSet(Collections.singletonList("chunk_worker"))));

    {
        tmpChunk = new LinkedHashMap<>();
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++) tmpChunk.put(getIDForPos(i, 1, j), new State(block, 0));
    }

    @Override
    public String getWorkerID() {
        return "ChunkWorker";
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
                connection.sendCreateEntityRequest(builder.build(CHUNK_REQUIREMENT_SET), op.firstEntityId, timeoutMillis);
            }
        });
    }

    private int getIDForPos(int x, int y, int z) {
        return x << 4 + y << 2 + z;
    }

    @Override
    public void start() {
        logger.info("Starting to connect");
        ConnectionManager.connect(name, true);
        ConnectionManager.setConnectionCallback(this::onConnected);
    }

    private void onConnected() {
        logger.info("Connection status: " + ConnectionManager.getConnectionStatus());
        View v = ConnectionManager.getView();
        System.out.println();
//        createChunk(ConnectionManager.getDispatcher(), ConnectionManager.getConnection(), new Coordinates(0, 0, 0));
    }
}
