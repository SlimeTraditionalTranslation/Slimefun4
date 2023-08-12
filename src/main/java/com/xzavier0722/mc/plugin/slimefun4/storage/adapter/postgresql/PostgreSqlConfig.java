package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.postgresql;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlCommonConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PostgreSqlConfig extends SqlCommonConfig {
    public PostgreSqlConfig(String host, int port, String database, String tablePrefix, String user, String passwd, boolean useSsl, int maxConnection) {
        super(host, port, database, tablePrefix, user, passwd, useSsl, maxConnection);
    }

    @Override
    public String jdbcUrl() {
        return "jdbc:postgresql://"
                + host + ":" + port + "/" + database
                + "?ssl=" + useSsl;
    }

    @Override
    public String driver() {
        return "org.postgresql.Driver";
    }
}
