package com.hrznstudio.spatial;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public final class SpatialLaunchWrapper {

    private SpatialLaunchWrapper() {
    }

    public static void main(String[] args) {
        setEnvironment();
        Launch.main(ArrayUtils.addAll(args,
                "--tweakClass",
                SpatialTweaker.class.getName(),
                "--tweakClass",
                "net.minecraftforge.fml.common.launcher.FMLTweaker",
//        "net.minecraftforge.fml.common.launcher.FMLServerTweaker",
                "--tweakClass",
                "net.minecraftforge.gradle.tweakers.CoremodTweaker"
        ));
    }

    private static void setEnvironment() {
        try {
            Class gradleStart = Class.forName("net.minecraftforge.gradle.GradleStartCommon");

            System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-srg", getStaticFile(gradleStart, "SRG_NOTCH_SRG").getCanonicalPath());
            System.setProperty("net.minecraftforge.gradle.GradleStart.srg.notch-mcp", getStaticFile(gradleStart, "SRG_NOTCH_MCP").getCanonicalPath());
            System.setProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp", getStaticFile(gradleStart, "SRG_SRG_MCP").getCanonicalPath());
            System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-srg", getStaticFile(gradleStart, "SRG_MCP_SRG").getCanonicalPath());
            System.setProperty("net.minecraftforge.gradle.GradleStart.srg.mcp-notch", getStaticFile(gradleStart, "SRG_MCP_NOTCH").getCanonicalPath());
            System.setProperty("net.minecraftforge.gradle.GradleStart.csvDir", getStaticFile(gradleStart, "CSV_DIR").getCanonicalPath());
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | IOException ignored) {
            // Not in dev environment
        }
    }

    private static File getStaticFile(final Class clazz, final String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = clazz.getDeclaredField(field);
        f.setAccessible(true);
        return (File) f.get(null);
    }

}
