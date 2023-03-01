package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldMapper;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import io.github.bakedlibs.dough.collections.Pair;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_BACKPACK_ID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_BACKPACK_NUM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_BACKPACK_SIZE;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_INVENTORY_ITEM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_INVENTORY_SLOT;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_PLAYER_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_PLAYER_UUID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.FIELD_RESEARCH_KEY;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.TABLE_NAME_BACKPACK;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.TABLE_NAME_BACKPACK_INVENTORY;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.TABLE_NAME_PLAYER_PROFILE;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql.SqlConstants.TABLE_NAME_PLAYER_RESEARCH;

public class MysqlAdapter implements IDataSourceAdapter<MysqlConfig> {
    private FieldMapper<String> mapper;
    private MysqlConnectionPool pool;
    private MysqlConfig config;
    private String profileTable;
    private String researchTable;
    private String backpackTable;
    private String inventoryTable;

    @Override
    public void prepare(MysqlConfig config) {
        pool = new MysqlConnectionPool(config);
        this.config = config;
        profileTable = config.tablePrefix() + TABLE_NAME_PLAYER_PROFILE;
        researchTable = config.tablePrefix() + TABLE_NAME_PLAYER_RESEARCH;
        backpackTable = config.tablePrefix() + TABLE_NAME_BACKPACK;
        inventoryTable = config.tablePrefix() + TABLE_NAME_BACKPACK_INVENTORY;

        var fieldMap = new HashMap<FieldKey, String>();
        fieldMap.put(FieldKey.PLAYER_UUID, FIELD_PLAYER_UUID);
        fieldMap.put(FieldKey.PLAYER_NAME, FIELD_PLAYER_NAME);
        fieldMap.put(FieldKey.RESEARCH_ID, FIELD_RESEARCH_KEY);
        fieldMap.put(FieldKey.BACKPACK_ID, FIELD_BACKPACK_ID);
        fieldMap.put(FieldKey.BACKPACK_NUMBER, FIELD_BACKPACK_NUM);
        fieldMap.put(FieldKey.BACKPACK_SIZE, FIELD_BACKPACK_SIZE);
        fieldMap.put(FieldKey.INVENTORY_SLOT, FIELD_INVENTORY_SLOT);
        fieldMap.put(FieldKey.INVENTORY_ITEM, FIELD_INVENTORY_ITEM);
        mapper = new FieldMapper<>(fieldMap);

        createTables();
    }

    @Override
    public void shutdown() {
        pool.destroy();
    }

    @Override
    public void setData(RecordKey key, RecordSet item) {
        var data = item.getAll();
        var fields = data.keySet();
        var fieldStr = buildFieldStr(fields);
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
            valStr.append(toSqlValStr(field, data.get(field)));
        }

        var updateFields = key.getFields();
        executeSql(
                "INSERT INTO " + mapTable(key.getScope()) + " (" + fieldStr.get() + ") "
                + "VALUES (" + valStr + ")"
                + (updateFields.isEmpty() ? "" : " ON DUPLICATE KEY UPDATE "
                        + String.join(", ", updateFields.stream().map(field -> {
                            var val = item.get(field);
                            if (val == null) {
                                throw new IllegalArgumentException("Cannot find value in RecordSet for the specific key: " + field);
                            }
                            return buildKvStr(field, val);
                        }).toList())
                ) + ";"
        );
    }

    @Override
    public List<RecordSet> getData(RecordKey key) {
        return executeQuery(
                "SELECT " + buildFieldStr(key.getFields()).orElse("*") +
                " FROM " + mapTable(key.getScope()) +
                buildConditionStr(key.getConditions()) + ";"
        );
    }

    @Override
    public void deleteData(RecordKey key) {
        executeSql("DELETE FROM " + mapTable(key.getScope()) + buildConditionStr(key.getConditions()) + ";");
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
                + profileTable + "("
                + FIELD_PLAYER_UUID + " CHAR(64) PRIMARY KEY NOT NULL, "
                + FIELD_PLAYER_NAME + " CHAR(64) NOT NULL, "
                + FIELD_BACKPACK_NUM + " INT UNSIGNED DEFAULT 0"
                + ");"
        );
    }

    private void createResearchTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + researchTable + "("
                + FIELD_PLAYER_UUID + " CHAR(64) NOT NULL, "
                + FIELD_RESEARCH_KEY + " CHAR(64) NOT NULL, "
                + "FOREIGN KEY (" + FIELD_PLAYER_UUID + ") "
                + "REFERENCES " + profileTable + "(" + FIELD_PLAYER_UUID + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "INDEX player_uuid (" + FIELD_PLAYER_UUID + ")"
                + ");"
        );
    }

    private void createBackpackTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + backpackTable + "("
                + FIELD_BACKPACK_ID + " BIGINT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT, "
                + FIELD_PLAYER_UUID + " CHAR(64) NOT NULL, "
                + FIELD_BACKPACK_NUM + " INT UNSIGNED NOT NULL, "
                + FIELD_BACKPACK_SIZE + " TINYINT UNSIGNED NOT NULL, "
                + "FOREIGN KEY (" + FIELD_PLAYER_UUID + ") "
                + "REFERENCES " + profileTable + "(" + FIELD_PLAYER_UUID + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "INDEX player_backpack (" + FIELD_PLAYER_UUID + ", " + FIELD_BACKPACK_NUM + ")"
                + ");"
        );
    }

    private void createInventoryTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + inventoryTable + "("
                + FIELD_BACKPACK_ID + " BIGINT UNSIGNED NOT NULL, "
                + FIELD_INVENTORY_SLOT + " TINYINT UNSIGNED NOT NULL, "
                + FIELD_INVENTORY_ITEM + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + FIELD_BACKPACK_ID + ") "
                + "REFERENCES " + backpackTable + "(" + FIELD_BACKPACK_ID + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY (" + FIELD_BACKPACK_ID + ", " + FIELD_INVENTORY_SLOT + ")"
                + ");"
        );
    }

    private void executeSql(String sql) {
        Connection conn = null;
        try {
            conn = pool.getConn();
            try (var stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            if (conn != null) {
                pool.releaseConn(conn);
            }
        }
    }

    private List<RecordSet> executeQuery(String sql) {
        Connection conn = null;
        try {
            conn = pool.getConn();
            try (var stmt = conn.createStatement()) {
                try (var result = stmt.executeQuery(sql)) {
                    List<RecordSet> re = null;
                    ResultSetMetaData metaData = null;
                    int columnCount = 0;
                    while (result.next()) {
                        if (re == null) {
                            re = new ArrayList<>();
                            metaData = result.getMetaData();
                            columnCount = metaData.getColumnCount();
                        }
                        var row = new RecordSet();
                        for (var i = 1; i <= columnCount; i++) {
                            row.put(mapper.get(metaData.getColumnName(i)), result.getString(i));
                        }
                        row.readonly();
                        re.add(row);
                    }
                    return re == null ? Collections.emptyList() : Collections.unmodifiableList(re);
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            if (conn != null) {
                pool.releaseConn(conn);
            }
        }
    }

    private String mapTable(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE -> profileTable;
            case PLAYER_RESEARCH -> researchTable;
            case BACKPACK_PROFILE -> backpackTable;
            case BACKPACK_INVENTORY -> inventoryTable;
        };
    }

    private String mapField(FieldKey key) {
        if (key == FieldKey.PLAYER_BACKPACK_NUM) {
            key = FieldKey.BACKPACK_NUMBER;
        }
        return mapper.get(key);
    }

    private Optional<String> buildFieldStr(Set<FieldKey> fields) {
        if (fields.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(String.join(", ", fields.stream().map(this::mapField).toList()));
    }
    private String buildConditionStr(List<Pair<FieldKey, String>> conditions) {
        if (conditions.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(
                " AND ",
                conditions.stream().map(
                        condition -> buildKvStr(condition.getFirstValue(), condition.getSecondValue())
                ).toList()
        );
    }

    private String buildKvStr(FieldKey key, String val) {
        return mapField(key) + "=" + toSqlValStr(key, val);
    }

    private String toSqlValStr(FieldKey key, String val) {
        return key.isNumType() ? val : "'" + val + "'";
    }
}
