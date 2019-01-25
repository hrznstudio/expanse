package com.hrznstudio.spatial;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static Logger logger = LogManager.getLogger(WorkerManager.class.getSimpleName());

    public static void main(String... args) {
        int i = ArrayUtils.indexOf(args, "--version");
        if (i != ArrayUtils.INDEX_NOT_FOUND) {
            args = ArrayUtils.subarray(args, 0, i);
        } else
            logger.warn("Running the WorkerManager directly is not recommended. Please use SpatialLaunchWrapper instead.");
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