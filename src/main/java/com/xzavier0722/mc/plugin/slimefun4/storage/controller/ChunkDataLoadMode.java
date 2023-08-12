package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

public enum ChunkDataLoadMode {
    LOAD_WITH_CHUNK,
    LOAD_ON_STARTUP;

    public boolean readCacheOnly() {
        return this == ChunkDataLoadMode.LOAD_ON_STARTUP;
    }
}
