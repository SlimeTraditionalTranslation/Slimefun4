package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

public interface IMigrator {
    boolean isOldDataExists();

    void checkOldData();

    MigrateStatus migrateData();

    String getName();
}
