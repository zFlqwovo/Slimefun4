package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.ConnectionPool;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_ID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_NUM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_SIZE;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_INVENTORY_ITEM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_INVENTORY_SLOT;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_PLAYER_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_PLAYER_UUID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_RESEARCH_KEY;

public class SqliteAdapter implements IDataSourceAdapter<SqliteConfig> {
    private SqliteConfig config;
    private Connection writeConn;
    private ConnectionPool readPool;

    @Override
    public void prepare(SqliteConfig config) {
        this.config = config;
        writeConn = createConn();
        readPool = new ConnectionPool(this::createConn, config.maxReadConnection());
        createTables();
    }

    @Override
    public void shutdown() {
        readPool.destroy();
        try {
            writeConn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setData(RecordKey key, RecordSet item) {
        var data = item.getAll();
        var fields = data.keySet();
        var fieldStr = SqlUtils.buildFieldStr(fields);
        if (fieldStr.isEmpty()) {
            throw new IllegalArgumentException("No data provided in RecordSet.");
        }

        var valStr = new StringBuilder();
        var flag = false;
        for (var field : fields) {
            if (flag) {
                valStr.append(", ");
            } else {
                flag = true;
            }
            valStr.append(SqlUtils.toSqlValStr(field, data.get(field)));
        }

        var updateFields = key.getFields();
        var table = SqlUtils.mapTable(key.getScope());
        executeSql(
                "INSERT OR IGNORE INTO " + table + " (" + fieldStr.get() + ") VALUES (" + valStr + ");"
        );

        if (updateFields.isEmpty()) {
            return;
        }

        executeSql(
                "UPDATE " + table + " SET "
                + String.join(", ", updateFields.stream().map(field -> {
                    var val = item.get(field);
                    if (val == null) {
                        throw new IllegalArgumentException("Cannot find value in RecordSet for the specific key: " + field);
                    }
                    return SqlUtils.buildKvStr(field, val);
                }).toList()) + SqlUtils.buildConditionStr(key.getConditions()) + ";"
        );
    }

    @Override
    public List<RecordSet> getData(RecordKey key) {
        return executeQuery(
                "SELECT " + SqlUtils.buildFieldStr(key.getFields()).orElse("*")
                +" FROM " + SqlUtils.mapTable(key.getScope())
                + SqlUtils.buildConditionStr(key.getConditions()) + ";"
        );
    }

    @Override
    public void deleteData(RecordKey key) {
        executeSql("DELETE FROM " + SqlUtils.mapTable(key.getScope()) + SqlUtils.buildConditionStr(key.getConditions()) + ";");
    }

    private void createTables() {
        createProfileTable();
        createResearchTable();
        createBackpackTable();
        createInventoryTable();
    }

    private void createProfileTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.PLAYER_PROFILE) + "("
                + FIELD_PLAYER_UUID + " TEXT PRIMARY KEY NOT NULL, "
                + FIELD_PLAYER_NAME + " TEXT NOT NULL, "
                + FIELD_BACKPACK_NUM + " INTEGER DEFAULT 0"
                + ");"
        );
    }

    private void createResearchTable() {
        var table = SqlUtils.mapTable(DataScope.PLAYER_RESEARCH);
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + table + "("
                + FIELD_PLAYER_UUID + " TEXT NOT NULL, "
                + FIELD_RESEARCH_KEY + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + FIELD_PLAYER_UUID + ") "
                + "REFERENCES " + SqlUtils.mapTable(DataScope.PLAYER_PROFILE) + "(" + FIELD_PLAYER_UUID + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE);"
        );

        executeSql(
                "CREATE INDEX IF NOT EXISTS player_researches ON " + table + " (" + FIELD_PLAYER_UUID + ", " + FIELD_RESEARCH_KEY + ");"
        );
    }

    private void createBackpackTable() {
        var table = SqlUtils.mapTable(DataScope.BACKPACK_PROFILE);
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                        + table + "("
                        + FIELD_BACKPACK_ID + " TEXT PRIMARY KEY NOT NULL, "
                        + FIELD_PLAYER_UUID + " TEXT NOT NULL, "
                        + FIELD_BACKPACK_NUM + " INTEGER NOT NULL, "
                        + FIELD_BACKPACK_NAME + " TEXT NULL, "
                        + FIELD_BACKPACK_SIZE + " INTEGER NOT NULL, "
                        + "FOREIGN KEY (" + FIELD_PLAYER_UUID + ") "
                        + "REFERENCES " + SqlUtils.mapTable(DataScope.PLAYER_PROFILE) + "(" + FIELD_PLAYER_UUID + ") "
                        + "ON UPDATE CASCADE ON DELETE CASCADE);"
        );

        executeSql(
                "CREATE INDEX IF NOT EXISTS player_backpack ON " + table + " (" + FIELD_PLAYER_UUID + ", " + FIELD_BACKPACK_NUM + ");"
        );
    }

    private void createInventoryTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                        + SqlUtils.mapTable(DataScope.BACKPACK_INVENTORY) + "("
                        + FIELD_BACKPACK_ID + " TEXT NOT NULL, "
                        + FIELD_INVENTORY_SLOT + " INTEGER NOT NULL, "
                        + FIELD_INVENTORY_ITEM + " TEXT NOT NULL, "
                        + "FOREIGN KEY (" + FIELD_BACKPACK_ID + ") "
                        + "REFERENCES " + SqlUtils.mapTable(DataScope.BACKPACK_PROFILE) + "(" + FIELD_BACKPACK_ID + ") "
                        + "ON UPDATE CASCADE ON DELETE CASCADE, "
                        + "PRIMARY KEY (" + FIELD_BACKPACK_ID + ", " + FIELD_INVENTORY_SLOT + ")"
                        + ");"
        );
    }

    private synchronized void executeSql(String sql) {
        try {
            SqlUtils.execSql(writeConn, sql);
        } catch (SQLException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        }
    }

    private List<RecordSet> executeQuery(String sql) {
        Connection conn = null;
        try {
            conn = readPool.getConn();
            return SqlUtils.execQuery(conn, sql);
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            if (conn != null) {
                readPool.releaseConn(conn);
            }
        }
    }

    private Connection createConn() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + config.path());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create Sqlite connection: ", e);
        }
    }
}
