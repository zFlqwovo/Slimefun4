package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public record SqliteConfig(String path) {
    public HikariDataSource createDataSource() {
        var config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + path + "?foreign_keys=on");

        return new HikariDataSource(config);
    }
}
