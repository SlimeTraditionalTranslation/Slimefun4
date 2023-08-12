package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public abstract class SqlCommonConfig implements ISqlCommonConfig {
    protected final String host;
    protected final int port;
    protected final String database;
    protected final String tablePrefix;
    protected final String user;
    protected final String passwd;
    protected final boolean useSsl;
    protected final int maxConnection;

    public SqlCommonConfig(
            String host,
            int port,
            String database,
            String tablePrefix,
            String user,
            String passwd,
            boolean useSsl,
            int maxConnection
    ) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.tablePrefix = tablePrefix;
        this.user = user;
        this.passwd = passwd;
        this.useSsl = useSsl;
        this.maxConnection = maxConnection;
    }

    public HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setDriverClassName(driver());
        config.setJdbcUrl(jdbcUrl());
        config.setPoolName("SlimefunHikariPool");

        if (!user.isEmpty()) {
            config.setUsername(user);
        }

        if (!passwd.isEmpty()) {
            config.setPassword(passwd);
        }

        config.setMaximumPoolSize(Math.max(Runtime.getRuntime().availableProcessors(), maxConnection));
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(10));
        config.setLeakDetectionThreshold(TimeUnit.MINUTES.toMillis(1));

        config.setDataSourceProperties(getProperties());

        return new HikariDataSource(config);
    }

    private static Properties getProperties() {
        var props = new Properties();
        props.setProperty("dataSource.cachePrepStmts", "true");
        props.setProperty("dataSource.prepStmtCacheSize", "250");
        props.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        props.setProperty("dataSource.useServerPrepStmts", "true");
        props.setProperty("dataSource.useLocalSessionState", "true");
        props.setProperty("dataSource.rewriteBatchedStatements", "true");
        props.setProperty("dataSource.cacheResultSetMetadata", "true");
        props.setProperty("dataSource.cacheServerConfiguration", "true");
        props.setProperty("dataSource.elideSetAutoCommits", "true");
        props.setProperty("dataSource.maintainTimeStats", "false");
        return props;
    }

    public String tablePrefix() {
        return tablePrefix;
    }
}
