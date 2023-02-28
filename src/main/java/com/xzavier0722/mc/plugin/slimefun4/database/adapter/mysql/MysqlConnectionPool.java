package com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class MysqlConnectionPool {
    private final MysqlConfig config;
    private final int maxConnCount;
    private final Deque<Connection> freeConn;
    private final Set<Connection> usingConn;

    private boolean destroyed = false;
    private int currConnCount = 0;
    private int waitingCount = 0;

    public MysqlConnectionPool(MysqlConfig config) {
        this.config = config;
        this.maxConnCount = config.maxConnection();
        this.freeConn = new LinkedList<>();
        this.usingConn = new HashSet<>();
    }

    public synchronized Connection getConn() throws SQLException, InterruptedException {
        checkDestroy();

        if (freeConn.isEmpty()) {
            if (currConnCount >= maxConnCount) {
                waitingCount++;
                wait();
                return getConn();
            }

            var re = newConn();
            currConnCount++;
            usingConn.add(re);
            return re;
        } else {
            var re = freeConn.getFirst();
            if (!testConn(re)) {
                currConnCount--;
                return getConn();
            }
            usingConn.add(re);
            return re;
        }
    }

    public synchronized void releaseConn(Connection conn) {
        checkDestroy();

        if (!usingConn.remove(conn)) {
            return;
        }

        freeConn.add(conn);

        if (waitingCount > 0) {
            notifyAll();
            waitingCount--;
        }
    }

    public synchronized void destroy() {
        checkDestroy();

        destroyed = true;
        freeConn.forEach(this::tryClose);
        usingConn.forEach(this::tryClose);
    }

    private boolean testConn(Connection conn) {
        try (var stmt = conn.createStatement()) {
            stmt.execute("/* ping */ SHOW DATABASES");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection newConn() throws SQLException {
        return DriverManager.getConnection(config.jdbcUrl(), config.user(), config.passwd());
    }

    private void checkDestroy() {
        if (destroyed) {
            throw new IllegalStateException("Connection pool cannot be accessed after destroy() called");
        }
    }

    private void tryClose(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
