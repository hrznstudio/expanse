package com.hrznstudio.spatial.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.List;

public final class SpatialTweaker implements ITweaker {
    private String[] args;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
    }

    @Override
    public String getLaunchTarget() {
        return "com.hrznstudio.spatial.worker.WorkerManager";
    }

    @Override
    public String[] getLaunchArguments() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
