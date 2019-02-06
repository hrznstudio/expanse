package com.hrznstudio.spatial.client;

import improbable.worker.ComponentMetaclass;
import improbable.worker.Dispatcher;
import improbable.worker.EntityId;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ComponentRequirement {
    private final Map<ComponentMetaclass, Object> map = new HashMap<>();
    private Consumer<EntityId> consumer;

    public ComponentRequirement(Dispatcher dispatcher, Consumer<EntityId> consumer, ComponentMetaclass<?, ?>... metadata) {
        this.consumer = consumer;
        for (ComponentMetaclass<?, ?> meta : metadata) {
            map.put(meta, null);
            dispatcher.onAddComponent(meta, argument -> {
                map.put(meta, argument.data);
                update(argument.entityId);
            });
        }
    }

    private boolean obtained() {
        return !map.entrySet().stream().anyMatch(componentMetaclassObjectEntry -> componentMetaclassObjectEntry.getValue() == null);
    }

    private void update(EntityId id) {
        if (obtained())
            consumer.accept(id);
    }
}