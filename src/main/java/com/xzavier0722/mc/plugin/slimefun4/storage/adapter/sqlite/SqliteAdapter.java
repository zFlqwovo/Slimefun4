package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlite;

import city.norain.slimefun4.timings.entry.SQLEntry;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlCommonAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.sql.SQLException;
import java.util.List;

public class SqliteAdapter extends SqlCommonAdapter<SqliteConfig> {
    @Override
    public void initStorage(DataType type) {
        switch (type) {
            case PLAYER_PROFILE -> createProfileTables();
            case BLOCK_STORAGE -> createBlockStorageTables();
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

        if (!updateFields.isEmpty()) {
            if (key.getConditions().isEmpty()) {
                throw new IllegalArgumentException("Condition is required for update statement!");
            }

            var row = executeUpdate("UPDATE "
                    + table
                    + " SET "
                    + String.join(
                            ", ",
                            updateFields.stream()
                                    .map(field -> {
                                        var val = item.get(field);
                                        if (val == null) {
                                            throw new IllegalArgumentException(
                                                    "Cannot find value in RecordSet for the specific key: " + field);
                                        }
                                        return SqlUtils.buildKvStr(field, val);
                                    })
                                    .toList())
                    + SqlUtils.buildConditionStr(key.getConditions())
                    + ";");
            if (row > 0) {
                return;
            }
        }

        executeSql("INSERT OR IGNORE INTO " + table + " (" + fieldStr.get() + ") VALUES (" + valStr + ");");
    }

    @Override
    public List<RecordSet> getData(RecordKey key, boolean distinct) {
        return executeQuery((distinct ? "SELECT DISTINCT " : "SELECT ")
                + SqlUtils.buildFieldStr(key.getFields()).orElse("*")
                + " FROM "
                + SqlUtils.mapTable(key.getScope())
                + SqlUtils.buildConditionStr(key.getConditions())
                + ";");
    }

    @Override
    public void deleteData(RecordKey key) {
        executeSql("DELETE FROM "
                + SqlUtils.mapTable(key.getScope())
                + SqlUtils.buildConditionStr(key.getConditions())
                + ";");
    }

    public synchronized void executeSql(String sql) {
        super.executeSql(sql);
    }

    private synchronized int executeUpdate(String sql) {
        var entry = new SQLEntry(sql);
        Slimefun.getSQLProfiler().recordEntry(entry);

        try (var conn = ds.getConnection()) {
            return SqlUtils.execUpdate(conn, sql);
        } catch (SQLException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            Slimefun.getSQLProfiler().finishEntry(entry);
        }
    }
}
