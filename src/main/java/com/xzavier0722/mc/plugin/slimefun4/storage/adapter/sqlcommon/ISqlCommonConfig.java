package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon;

import com.zaxxer.hikari.HikariDataSource;

public interface ISqlCommonConfig {
    String driver();
    String jdbcUrl();
    HikariDataSource createDataSource();

}
