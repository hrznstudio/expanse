package com.hrznstudio.spatial.util;

import com.google.common.collect.Lists;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonWorkerRequirements {
    public static WorkerRequirementSet getAllCommonWorkers() {
        return createWorkerRequirementSet("horizon_server", "horizon_client", "chunk_worker", "entity_worker");
    }

    public static WorkerRequirementSet getEntityWorkers() {
//        return createWorkerAttributeSet("horizon_server", "horizon_client");
        return createWorkerRequirementSet("horizon_server", "horizon_client", "entity_worker");
    }

    public static WorkerAttributeSet createWorkerAttributeSet(String... attributes) {
        return new WorkerAttributeSet(Lists.newArrayList(attributes));
    }

    public static List<WorkerAttributeSet> createWorkerAttributeSets(String... attributes) {
        return Stream.of(attributes).map(CommonWorkerRequirements::createWorkerAttributeSet).collect(Collectors.toList());
    }

    public static WorkerRequirementSet createWorkerRequirementSet(String... attributes) {
        return new WorkerRequirementSet(createWorkerAttributeSets(attributes));
    }

    public static WorkerAttributeSet emptyAttributeSet() {
        return createWorkerAttributeSet();
    }
}