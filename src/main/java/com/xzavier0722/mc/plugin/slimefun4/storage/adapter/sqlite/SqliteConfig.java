package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public record SqliteConfig(String path) {
    public HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + path + "?foreign_keys=on");
        config.setMaximumPoolSize(1);
        config.setIdleTimeout(TimeUnit.MINUTES.toMillis(1));
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(10));

        var props = new Properties();
        props.setProperty("dataSource.cachePrepStmts", "true");
        props.setProperty("dataSource.prepStmtCacheSize", "250");
        props.setProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        props.setProperty("dataSource.maintainTimeStats", "false");

        config.setDataSourceProperties(props);

        return new HikariDataSource(config);
    }
}
