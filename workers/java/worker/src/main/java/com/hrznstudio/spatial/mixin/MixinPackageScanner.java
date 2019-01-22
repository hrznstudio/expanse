package com.hrznstudio.spatial.mixin;

import org.reflections.Reflections;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.HashSet;
import java.util.Set;

/**
 * ASM Hack to the PackageScanner to reduce connection time.
 */
@Mixin(targets = "improbable.worker.PackageScanner")
public class MixinPackageScanner {

    private static final String[] prefixes = new String[]{
            "improbable",
            "minecraft"
    };

    /**
     * @reason Reduce SpatialOS connection form 10 minutes to 3 seconds
     * @author Coded
     */
    @Overwrite
    public static <T> Set<Class<? extends T>> getAllSubClassesOf(final Class<T> desiredClass) {
        Set<Class<? extends T>> set = new HashSet<>();
        for (String prefix : prefixes) {
            set.addAll(new Reflections(prefix).getSubTypesOf(desiredClass));
        }
        return set;
    }

}
