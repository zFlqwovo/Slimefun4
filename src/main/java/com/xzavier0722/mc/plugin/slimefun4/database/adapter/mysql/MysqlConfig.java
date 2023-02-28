package com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql;

public record MysqlConfig(
        String host,
        int port,
        String database,
        String tablePrefix,
        String user,
        String passwd,
        boolean useSsl,
        int maxConnection
    ) {
    public String jdbcUrl() {
        return "jdbc:mysql://"
                + host + ":" + port + "/" + database
                + "?serverTimezone=CTT&characterEncoding=utf8&useSSL="+ useSsl;
    }
}
