package com.hrznstudio.spatial;

import com.google.common.collect.Lists;
import improbable.WorkerAttributeSet;
import improbable.WorkerRequirementSet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonWorkerRequirements {
    public static WorkerRequirementSet getAllCommonWorkers() {
        return createWorkerRequirementSet("horizon_server", "horizon_client", "chunk_worker");
    }

    public static WorkerRequirementSet getEntityWorkers() {
//        return createWorkerAttributeSet("horizon_server", "horizon_client");
        return createWorkerRequirementSet("horizon_server", "horizon_client", "chunk_worker");
    }

    private static WorkerAttributeSet createWorkerAttributeSet(String... attributes) {
        return new WorkerAttributeSet(Lists.newArrayList(attributes));
    }

    private static List<WorkerAttributeSet> createWorkerAttributeSets(String... attributes) {
        return Stream.of(attributes).map(CommonWorkerRequirements::createWorkerAttributeSet).collect(Collectors.toList());
    }

    private static WorkerRequirementSet createWorkerRequirementSet(String... attributes) {
        return new WorkerRequirementSet(createWorkerAttributeSets(attributes));
    }

    public static WorkerAttributeSet emptyAttributeSet() {
        return createWorkerAttributeSet();
    }
}