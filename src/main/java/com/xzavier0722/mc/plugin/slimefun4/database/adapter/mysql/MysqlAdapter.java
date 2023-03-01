package com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql;

import com.xzavier0722.mc.plugin.slimefun4.database.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.database.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.database.common.FieldMapper;
import com.xzavier0722.mc.plugin.slimefun4.database.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.database.common.RecordSet;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_BACKPACK_ID;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_BACKPACK_NUM;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_BACKPACK_SIZE;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_INVENTORY_ITEM;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_INVENTORY_SLOT;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_PLAYER_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_PLAYER_UUID;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.FIELD_RESEARCH_KEY;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.TABLE_NAME_BACKPACK;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.TABLE_NAME_BACKPACK_INVENTORY;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.TABLE_NAME_PLAYER_PROFILE;
import static com.xzavier0722.mc.plugin.slimefun4.database.adapter.mysql.SqlConstants.TABLE_NAME_PLAYER_RESEARCH;

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
        // TODO
    }

    @Override
    public Set<RecordSet> getData(RecordKey key) {
        // TODO
        return null;
    }

    @Override
    public void deleteData(RecordKey key) {
        // TODO
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
                + FIELD_BACKPACK_NUM + " INT UNSIGNED NOT NULL"
                + ");"
        );
    }

    private void createResearchTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + researchTable + "("
                + FIELD_PLAYER_UUID + " CHAR(64) REFERENCES "
                    + profileTable + "(" + FIELD_PLAYER_UUID + ") "
                    + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + FIELD_RESEARCH_KEY + " CHAR(64) NOT NULL, "
                + "INDEX player_uuid (" + FIELD_PLAYER_UUID + ")"
                + ");"
        );
    }

    private void createBackpackTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + backpackTable + "("
                + FIELD_BACKPACK_ID + " BIGINT UNSIGNED PRIMARY KEY NOT NULL AUTO_INCREMENT, "
                + FIELD_PLAYER_UUID + " CHAR(64) REFERENCES "
                    + profileTable + "(" + FIELD_PLAYER_UUID + ") "
                    + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + FIELD_BACKPACK_NUM + " INT UNSIGNED NOT NULL, "
                + FIELD_BACKPACK_SIZE + " TINYINT UNSIGNED NOT NULL, "
                + "INDEX player_backpack (" + FIELD_PLAYER_UUID + ", " + FIELD_BACKPACK_NUM + ")"
                + ");"
        );
    }

    private void createInventoryTable() {
        executeSql(
                "CREATE TABLE IF NOT EXISTS "
                + inventoryTable + "("
                + FIELD_BACKPACK_ID + " BIGINT UNSIGNED REFERENCES "
                    + backpackTable + "(" + FIELD_BACKPACK_ID + ") "
                    + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + FIELD_INVENTORY_SLOT + " TINYINT UNSIGNED NOT NULL, "
                + FIELD_INVENTORY_ITEM + " TEXT NOT NULL, "
                + "INDEX backpack_inv (" + FIELD_BACKPACK_ID + ", " + FIELD_INVENTORY_SLOT + ")"
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

    private Optional<RecordSet> executeQuery(String sql) {
        Connection conn = null;
        try {
            conn = pool.getConn();
            try (var stmt = conn.createStatement()) {
                try (var result = stmt.executeQuery(sql)) {
                    RecordSet re = null;
                    ResultSetMetaData metaData = null;
                    int columnCount = 0;
                    while (result.next()) {
                        if (re == null) {
                            re = new RecordSet();
                            metaData = result.getMetaData();
                            columnCount = metaData.getColumnCount();
                        }

                        for (var i = 1; i <= columnCount; i++) {
                            re.put(mapper.get(metaData.getCatalogName(i)), result.getString(i));
                        }
                    }
                    if (re != null) {
                        re.readonly();
                    }
                    return Optional.ofNullable(re);
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
}
