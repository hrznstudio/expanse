package com.hrznstudio.spatial.chunk;

import com.hrznstudio.spatial.util.BaseWorker;
import com.hrznstudio.spatial.util.CommonWorkerRequirements;
import com.hrznstudio.spatial.util.EntityBuilder;
import improbable.Position;
import improbable.worker.*;
import minecraft.world.ChunkStorage;
import minecraft.world.ChunkStorageData;

public class ChunkWorker extends BaseWorker {
    @Override
    public String getWorkerID() {
        return "ChunkWorker";
    }

    public static void main(String[] args) {
        System.out.println("Hello world");
    }

    public static void createChunk(Dispatcher dispatcher, Connection connection) {
        final improbable.collections.Option<Integer> timeoutMillis = improbable.collections.Option.of(500);

        // Reserve an entity ID.
        RequestId<ReserveEntityIdRequest> entityIdReservationRequestId = connection.sendReserveEntityIdRequest(timeoutMillis);
        // When the reservation succeeds, create an entity with the reserved ID.

        dispatcher.onReserveEntityIdResponse(op -> {
            if (op.requestId.equals(entityIdReservationRequestId) && op.statusCode == StatusCode.SUCCESS) {
                EntityBuilder builder = new EntityBuilder("chunk");
                builder.addComponent(ChunkStorage.COMPONENT, ChunkStorageData.create(), CommonWorkerRequirements.getAllCommonWorkers());
                builder.addComponent(Position.COMPONENT, new improbable.PositionData(new improbable.Coordinates(1, 2, 3)), CommonWorkerRequirements.getAllCommonWorkers());
            }
        });
    }
}
