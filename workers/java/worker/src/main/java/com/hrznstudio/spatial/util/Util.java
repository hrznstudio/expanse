package com.hrznstudio.spatial.util;

import improbable.worker.ComponentMetaclass;
import improbable.worker.Entity;

public class Util {
    public static <META extends ComponentMetaclass<DATA, ?>, DATA> DATA getData(Entity entity, META meta) {
        return entity.get(meta).orElseThrow(new NullPointerException());
    }
}
