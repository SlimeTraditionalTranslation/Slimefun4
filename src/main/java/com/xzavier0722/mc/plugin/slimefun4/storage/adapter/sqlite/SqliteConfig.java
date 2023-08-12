package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.ISqlCommonConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public record SqliteConfig(String path, int maxConnection) implements ISqlCommonConfig {
    public HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setDriverClassName(driver());
        config.setJdbcUrl(jdbcUrl());
        config.setPoolName("SlimefunHikariPool");
        config.setMaximumPoolSize(maxConnection);

        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(10));

        var props = new Properties();
        props.setProperty("dataSource.cachePrepStmts", "true");
        props.setProperty("dataSource.prepStmtCacheSize", "250");
        props.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        props.setProperty("dataSource.maintainTimeStats", "false");

        config.setDataSourceProperties(props);

        return new HikariDataSource(config);
    }

    public String jdbcUrl() {
        return "jdbc:sqlite:" + path + "?foreign_keys=on&journal_mode=WAL";
    }

    public String driver() {
        return "org.sqlite.JDBC";
    }
}
