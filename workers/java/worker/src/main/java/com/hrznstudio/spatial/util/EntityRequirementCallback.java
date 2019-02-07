package com.hrznstudio.spatial.util;

import improbable.Metadata;
import improbable.MetadataData;
import improbable.worker.ComponentMetaclass;
import improbable.worker.Dispatcher;
import improbable.worker.EntityId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityRequirementCallback {
    //TODO: maybe make this delete old entities after a while to prevent high ram usage.
    private final Map<EntityId, Map<ComponentMetaclass<?, ?>, Object>> map;
    private final Callback callback;
    private final List<Requirement<?, ?>> requirements;

    private EntityRequirementCallback(Dispatcher dispatcher, Callback callback, List<Requirement<?, ?>> requirements) {
        this.callback = callback;
        map = new HashMap<>();
        this.requirements = requirements;
        for (Requirement requirement : requirements) {
            dispatcher.onAddComponent(requirement.getMetaclass(), argument -> addEntityMetaclassToMap(argument.entityId, requirement.getMetaclass(), argument.data, requirement));
        }
    }

    public static Builder builder(Callback callback) {
        return new Builder(callback);
    }

    private <META extends ComponentMetaclass<DATA, ?>, DATA> void addEntityMetaclassToMap(EntityId id, META metaclass, DATA data, Requirement<META, DATA> requirement) {
        if (requirement.isValid(data)) {
            map.computeIfAbsent(id, id1 -> {
                Map<ComponentMetaclass<?, ?>, Object> map = new HashMap<>();
                for (Requirement<?, ?> requirement1 : requirements) {
                    map.put(requirement1.getMetaclass(), null);
                }
                return map;
            }).put(metaclass, data);
            update(id);
        }
    }

    private boolean allPresent(EntityId id) {
        return this.map.get(id).entrySet().stream().noneMatch(entry -> entry.getValue() == null);
    }

    private void update(EntityId id) {
        if (allPresent(id))
            callback.on(id);
    }

    public interface Requirement<META extends ComponentMetaclass<DATA, ?>, DATA> {
        META getMetaclass();

        boolean isValid(DATA data);
    }

    @FunctionalInterface
    public interface Callback {
        void on(EntityId entityId);
    }

    public static class GenericRequirement<META extends ComponentMetaclass<DATA, ?>, DATA> implements Requirement<META, DATA> {
        private final META metaclass;

        public GenericRequirement(META metaclass) {
            this.metaclass = metaclass;
        }

        @Override
        public META getMetaclass() {
            return metaclass;
        }

        @Override
        public boolean isValid(DATA o) {
            return true;
        }
    }

    public static class EntityTypeRequirement implements Requirement<Metadata, MetadataData> {
        private final String type;

        public EntityTypeRequirement(String type) {
            this.type = type;
        }

        @Override
        public Metadata getMetaclass() {
            return Metadata.COMPONENT;
        }

        @Override
        public boolean isValid(MetadataData metadataData) {
            return type.equals(metadataData.getEntityType());
        }
    }

    public static class Builder {
        private final Callback callback;
        private final List<Requirement<?, ?>> requirements = new ArrayList<>();

        private Builder(Callback callback) {
            this.callback = callback;
        }

        public Builder requireType(String type) {
            return addRequirement(new EntityTypeRequirement(type));
        }

        public Builder requireComponent(ComponentMetaclass<?, ?> componentMetaclass) {
            return addRequirement(new GenericRequirement<>(componentMetaclass));
        }

        public Builder addRequirement(Requirement<?, ?> requirement) {
            requirements.add(requirement);
            return this;
        }

        public void attach(Dispatcher dispatcher) {
            new EntityRequirementCallback(dispatcher, callback, requirements);
        }
    }
}