package com.hrznstudio.spatial.chunk;

import com.hrznstudio.spatial.WorkerService;
import com.hrznstudio.spatial.util.CommonWorkerRequirements;
import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.util.EntityBuilder;
import improbable.Coordinates;
import improbable.Position;
import improbable.collections.Option;
import improbable.worker.*;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class ChunkWorker implements WorkerService {

    private final UUID uuid = UUID.randomUUID();
    private final String name = getClass().getSimpleName() + "$" + uuid;
    private final Logger logger = LogManager.getLogger(name);

    @Override
    public String getWorkerID() {
        return "ChunkWorker";
    }

    public static void createChunk(Dispatcher dispatcher, Connection connection) {
        final Option<Integer> timeoutMillis = Option.of(500);

        // Reserve an entity ID.
        RequestId<ReserveEntityIdsRequest> entityIdReservationRequestId = connection.sendReserveEntityIdsRequest(1, timeoutMillis);
        // When the reservation succeeds, create an entity with the reserved ID.

        dispatcher.onReserveEntityIdsResponse(op -> {
            if (op.requestId.equals(entityIdReservationRequestId) && op.statusCode == StatusCode.SUCCESS) {
                EntityBuilder builder = new EntityBuilder("chunk");
                builder.addComponent(ChunkStorage.COMPONENT, ChunkStorageData.create(), CommonWorkerRequirements.getAllCommonWorkers());
                builder.addComponent(Position.COMPONENT, new improbable.PositionData(new Coordinates(1, 2, 3)), CommonWorkerRequirements.getAllCommonWorkers());
                connection.sendCreateEntityRequest(builder.build(), op.firstEntityId, timeoutMillis);
            }
        });
    }

    @Override
    public void start() {
        logger.info("Starting to connect");
        ConnectionManager.connect(name);
        logger.info("Connected");
    }
}
