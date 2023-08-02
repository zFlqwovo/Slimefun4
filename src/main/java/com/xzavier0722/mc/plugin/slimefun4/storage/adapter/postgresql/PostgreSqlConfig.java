package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.postgresql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public record PostgreSqlConfig(
        String host,
        int port,
        String database,
        String tablePrefix,
        String user,
        String passwd,
        boolean useSsl,
        int maxConnection
) {
    public HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(jdbcUrl());

        if (!user.isEmpty()) {
            config.setUsername(user);
        }

        if (!passwd.isEmpty()) {
            config.setPassword(passwd);
        }

        config.setMaximumPoolSize(maxConnection);

        return new HikariDataSource(config);
    }

    public String jdbcUrl() {
        return "jdbc:postgresql://"
                + host + ":" + port + "/" + database
                + "?ssl=" + useSsl;
    }
}
