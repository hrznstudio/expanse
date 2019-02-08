package com.hrznstudio.spatial.worker.entity;

import com.hrznstudio.spatial.util.ConnectionManager;
import com.hrznstudio.spatial.worker.BaseWorker;
import improbable.collections.Option;
import improbable.worker.Authority;
import improbable.worker.Callback;
import improbable.worker.EntityId;
import improbable.worker.Ops;
import minecraft.entity.PlayerConnection;
import minecraft.entity.WorldEntity;
import sun.management.counter.perf.PerfLongArrayCounter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityWorker extends BaseWorker.BaseViewWorker {

    public static Map<EntityId, Integer> playerTimeout = new HashMap<>();
    private ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);

    @Override
    protected void onConnected() {
        getDispatcher().onComponentUpdate(PlayerConnection.COMPONENT, argument -> playerTimeout.put(argument.entityId, 10));
        getDispatcher().onAuthorityChange(WorldEntity.COMPONENT, argument -> {
            if (argument.authority == Authority.AUTHORITATIVE) {
                playerTimeout.put(argument.entityId, 5);
            } else if (argument.authority == Authority.NOT_AUTHORITATIVE) {
                playerTimeout.remove(argument.entityId);
            }
        });

        service.scheduleAtFixedRate(() -> {
            playerTimeout.keySet().forEach(id -> {
                int i = playerTimeout.get(id);
                if (i == 0) {
                    ConnectionManager.getConnection().sendDeleteEntityRequest(id, Option.empty());
                    playerTimeout.remove(id);
                } else {
                    playerTimeout.put(id, i - 1);
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }
}