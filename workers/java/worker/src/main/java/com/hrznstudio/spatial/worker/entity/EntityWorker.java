package com.hrznstudio.spatial.worker.entity;

import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.worker.BaseWorker;
import improbable.Coordinates;
import improbable.Position;
import improbable.Vector3f;
import improbable.collections.Option;
import improbable.worker.Authority;
import improbable.worker.EntityId;
import minecraft.entity.PlayerConnection;
import minecraft.entity.PlayerInput;
import minecraft.entity.WorldEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityWorker extends BaseWorker.BaseViewWorker {

    public static Map<EntityId, Integer> playerTimeout = new HashMap<>();
    private ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void onConnected() {
        getDispatcher().onAddComponent(PlayerConnection.COMPONENT, argument -> playerTimeout.put(argument.entityId, 10));
        getDispatcher().onComponentUpdate(PlayerConnection.COMPONENT, argument -> playerTimeout.put(argument.entityId, 10));
        getDispatcher().onAuthorityChange(WorldEntity.COMPONENT, argument -> {
            if (argument.authority == Authority.AUTHORITATIVE) {
                playerTimeout.put(argument.entityId, 5);
            } else if (argument.authority == Authority.NOT_AUTHORITATIVE) {
                playerTimeout.remove(argument.entityId);
            }
        });

        getDispatcher().onComponentUpdate(PlayerInput.COMPONENT, argument -> {
            if (getDispatcher().getAuthority(Position.COMPONENT, argument.entityId) != Authority.NOT_AUTHORITATIVE) {
                Vector3f pos = argument.update.getMovePosition().orElse(null);
                if (pos != null) {
                    ConnectionManager.getConnection().sendComponentUpdate(
                            Position.COMPONENT,
                            argument.entityId,
                            new Position.Update()
                                    .setCoords(
                                            new Coordinates(
                                                    pos.getX(), //TODO: Put some checks in here to make sure this position isn't speed hack
                                                    pos.getY(),
                                                    pos.getZ()
                                            )
                                    )
                    );
                }
            }
        });

        service.scheduleAtFixedRate(() -> {
            if(!playerTimeout.isEmpty()) {
                new HashSet<>(playerTimeout.keySet()).forEach(id -> {
                    int i = playerTimeout.get(id);
                    if (i == 0) {
                        ConnectionManager.getConnection().sendDeleteEntityRequest(id, Option.empty());
                        System.out.println("Removing entity " + id.getInternalId());
                        playerTimeout.remove(id);
                    } else {
                        playerTimeout.put(id, i - 1);
                    }
                });
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}