package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.postgresql;

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
    public String jdbcUrl() {
        return "jdbc:postgresql://"
                + host + ":" + port + "/" + database
                + "?ssl=" + useSsl;
    }
}
