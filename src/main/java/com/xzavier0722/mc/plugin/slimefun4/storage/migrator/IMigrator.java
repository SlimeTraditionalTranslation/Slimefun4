package com.xzavier0722.mc.plugin.slimefun4.storage.migrator;

public interface IMigrator {
    boolean hasOldData();

    MigrateStatus migrateData();

    String getName();
}
