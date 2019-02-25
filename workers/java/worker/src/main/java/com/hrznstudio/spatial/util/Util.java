package com.hrznstudio.spatial.util;

import improbable.Metadata;
import improbable.worker.ComponentMetaclass;
import improbable.worker.Entity;

import javax.annotation.Nullable;

public class Util {
    public static <META extends ComponentMetaclass<DATA, ?>, DATA> DATA getData(Entity entity, META meta) {
        return entity.get(meta).orElseThrow(new NullPointerException());
    }

    public static String getType(@Nullable Entity entity) {
        if (entity == null)
            return "ERROR";
        return getData(entity, Metadata.COMPONENT).getEntityType();
    }

    public static boolean isWithin(double a, double b, double dist) {
        return Math.max(a - dist, b) == Math.min(b, a + dist);
    }
}