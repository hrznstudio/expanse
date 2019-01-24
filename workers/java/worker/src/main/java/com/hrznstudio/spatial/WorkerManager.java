package com.hrznstudio.spatial;

import picocli.CommandLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
@CommandLine.Command(name = "worker", sortOptions = false,

        header = {
                "@|blue Spatial Worker Manager |@",
                ""
        },
        descriptionHeading = "@|bold %nDescription|@:%n",
        description = {
                "Displays commands and usage help.",
        }
)
public class WorkerManager implements Runnable {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays commands and usage help.")
    private boolean help;

    @CommandLine.Parameters(description = "Worker to run")
    private String worker;

    public static Map<String, WorkerService> workerMap = new HashMap<>();

    public static void main(String... args) {
        ServiceLoader<WorkerService> workerService = ServiceLoader.load(WorkerService.class);
        workerService.forEach(service -> workerMap.put(service.getWorkerID(), service));

        CommandLine.run(new WorkerManager(), System.out, args);
    }

    @Override
    public void run() {
        if(workerMap.containsKey(worker)) {
            workerMap.get(worker).start();
        }
    }
}