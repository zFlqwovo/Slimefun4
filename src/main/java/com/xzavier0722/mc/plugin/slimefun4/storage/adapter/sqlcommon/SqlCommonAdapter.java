package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon;

import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_ID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_NUM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_BACKPACK_SIZE;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_CHUNK;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_DATA_KEY;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_DATA_VALUE;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_INVENTORY_ITEM;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_INVENTORY_SLOT;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_LAST_PRESENT;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_LOCATION;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_PLAYER_NAME;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_PLAYER_UUID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_RESEARCH_KEY;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_SLIMEFUN_ID;
import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.FIELD_UNIVERSAL_UUID;

import city.norain.slimefun4.timings.entry.SQLEntry;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import com.zaxxer.hikari.HikariDataSource;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.sql.SQLException;
import java.util.List;

public abstract class SqlCommonAdapter<T extends ISqlCommonConfig> implements IDataSourceAdapter<T> {
    protected HikariDataSource ds;
    protected String profileTable, researchTable, backpackTable, bpInvTable;
    protected String blockRecordTable,
            blockDataTable,
            universalRecordTable,
            universalDataTable,
            chunkDataTable,
            blockInvTable,
            universalInvTable;
    protected T config;

    @Override
    public void prepare(T config) {
        this.config = config;
        ds = config.createDataSource();
    }

    protected void executeSql(String sql) {
        var entry = new SQLEntry(sql);
        Slimefun.getSQLProfiler().recordEntry(entry);
        try (var conn = ds.getConnection()) {
            SqlUtils.execSql(conn, sql);
        } catch (SQLException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            Slimefun.getSQLProfiler().finishEntry(entry);
        }
    }

    protected List<RecordSet> executeQuery(String sql) {
        var entry = new SQLEntry(sql);
        Slimefun.getSQLProfiler().recordEntry(entry);

        try (var conn = ds.getConnection()) {
            return SqlUtils.execQuery(conn, sql);
        } catch (SQLException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            Slimefun.getSQLProfiler().finishEntry(entry);
        }
    }

    protected String mapTable(DataScope scope) {
        return switch (scope) {
            case PLAYER_PROFILE -> profileTable;
            case BACKPACK_INVENTORY -> bpInvTable;
            case BACKPACK_PROFILE -> backpackTable;
            case PLAYER_RESEARCH -> researchTable;
            case BLOCK_INVENTORY -> blockInvTable;
            case CHUNK_DATA -> chunkDataTable;
            case BLOCK_DATA -> blockDataTable;
            case BLOCK_RECORD -> blockRecordTable;
            case UNIVERSAL_INVENTORY -> universalInvTable;
            case UNIVERSAL_RECORD -> universalRecordTable;
            case UNIVERSAL_DATA -> universalDataTable;
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }

    @Override
    public void shutdown() {
        ds.close();
        ds = null;
        profileTable = null;
        researchTable = null;
        backpackTable = null;
        bpInvTable = null;
        blockDataTable = null;
        blockRecordTable = null;
        chunkDataTable = null;
        blockInvTable = null;
        universalInvTable = null;
    }

    protected void createProfileTables() {
        createProfileTable();
        createResearchTable();
        createBackpackTable();
        createBackpackInventoryTable();
    }

    protected void createBlockStorageTables() {
        createBlockRecordTable();
        createBlockDataTable();
        createBlockInvTable();
        createChunkDataTable();
        createUniversalRecordTable();
        createUniversalInventoryTable();
        createUniversalDataTable();
    }

    protected void createProfileTable() {
        var table = SqlUtils.mapTable(DataScope.PLAYER_PROFILE);
        executeSql("CREATE TABLE IF NOT EXISTS "
                + table
                + "("
                + FIELD_PLAYER_UUID
                + " TEXT PRIMARY KEY NOT NULL, "
                + FIELD_PLAYER_NAME
                + " TEXT NOT NULL, "
                + FIELD_BACKPACK_NUM
                + " INTEGER DEFAULT 0"
                + ");");

        executeSql("CREATE INDEX IF NOT EXISTS index_player_name ON " + table + " (" + FIELD_PLAYER_NAME + ");");
    }

    protected void createResearchTable() {
        var table = SqlUtils.mapTable(DataScope.PLAYER_RESEARCH);
        executeSql("CREATE TABLE IF NOT EXISTS "
                + table
                + "("
                + FIELD_PLAYER_UUID
                + " TEXT NOT NULL, "
                + FIELD_RESEARCH_KEY
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_PLAYER_UUID
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.PLAYER_PROFILE)
                + "("
                + FIELD_PLAYER_UUID
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE);");

        executeSql("CREATE INDEX IF NOT EXISTS index_player_researches ON "
                + table
                + " ("
                + FIELD_PLAYER_UUID
                + ", "
                + FIELD_RESEARCH_KEY
                + ");");
    }

    protected void createBackpackTable() {
        var table = SqlUtils.mapTable(DataScope.BACKPACK_PROFILE);
        executeSql("CREATE TABLE IF NOT EXISTS "
                + table
                + "("
                + FIELD_BACKPACK_ID
                + " TEXT PRIMARY KEY NOT NULL, "
                + FIELD_PLAYER_UUID
                + " TEXT NOT NULL, "
                + FIELD_BACKPACK_NUM
                + " INTEGER NOT NULL, "
                + FIELD_BACKPACK_NAME
                + " TEXT NULL, "
                + FIELD_BACKPACK_SIZE
                + " INTEGER NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_PLAYER_UUID
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.PLAYER_PROFILE)
                + "("
                + FIELD_PLAYER_UUID
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE);");

        executeSql("CREATE INDEX IF NOT EXISTS index_player_backpack ON "
                + table
                + " ("
                + FIELD_PLAYER_UUID
                + ", "
                + FIELD_BACKPACK_NUM
                + ");");
    }

    protected void createBackpackInventoryTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.BACKPACK_INVENTORY)
                + "("
                + FIELD_BACKPACK_ID
                + " TEXT NOT NULL, "
                + FIELD_INVENTORY_SLOT
                + " INTEGER NOT NULL, "
                + FIELD_INVENTORY_ITEM
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_BACKPACK_ID
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.BACKPACK_PROFILE)
                + "("
                + FIELD_BACKPACK_ID
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY ("
                + FIELD_BACKPACK_ID
                + ", "
                + FIELD_INVENTORY_SLOT
                + ")"
                + ");");
    }

    protected void createBlockRecordTable() {
        var table = SqlUtils.mapTable(DataScope.BLOCK_RECORD);
        executeSql("CREATE TABLE IF NOT EXISTS "
                + table
                + "("
                + FIELD_LOCATION
                + " TEXT PRIMARY KEY NOT NULL, "
                + FIELD_CHUNK
                + " TEXT NOT NULL, "
                + FIELD_SLIMEFUN_ID
                + " TEXT NOT NULL"
                + ");");

        executeSql("CREATE INDEX IF NOT EXISTS index_chunk ON " + table + "(" + FIELD_CHUNK + ");");
    }

    protected void createBlockDataTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.BLOCK_DATA)
                + "("
                + FIELD_LOCATION
                + " TEXT NOT NULL, "
                + FIELD_DATA_KEY
                + " TEXT NOT NULL, "
                + FIELD_DATA_VALUE
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_LOCATION
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.BLOCK_RECORD)
                + "("
                + FIELD_LOCATION
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY ("
                + FIELD_LOCATION
                + ", "
                + FIELD_DATA_KEY
                + ")"
                + ");");
    }

    protected void createChunkDataTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.CHUNK_DATA)
                + "("
                + FIELD_CHUNK
                + " TEXT NOT NULL, "
                + FIELD_DATA_KEY
                + " TEXT NOT NULL, "
                + FIELD_DATA_VALUE
                + " TEXT NOT NULL, "
                + "PRIMARY KEY ("
                + FIELD_CHUNK
                + ", "
                + FIELD_DATA_KEY
                + ")"
                + ");");
    }

    protected void createBlockInvTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.BLOCK_INVENTORY)
                + "("
                + FIELD_LOCATION
                + " TEXT NOT NULL, "
                + FIELD_INVENTORY_SLOT
                + " INTEGER NOT NULL, "
                + FIELD_INVENTORY_ITEM
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_LOCATION
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.BLOCK_RECORD)
                + "("
                + FIELD_LOCATION
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY ("
                + FIELD_LOCATION
                + ", "
                + FIELD_INVENTORY_SLOT
                + ")"
                + ");");
    }

    protected void createUniversalRecordTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.UNIVERSAL_RECORD)
                + "("
                + FIELD_UNIVERSAL_UUID
                + " UUID NOT NULL, "
                + FIELD_SLIMEFUN_ID
                + " TEXT NOT NULL, "
                + FIELD_LAST_PRESENT
                + " TEXT NOT NULL, "
                + "PRIMARY KEY ("
                + FIELD_UNIVERSAL_UUID
                + ")"
                + ");");
    }

    protected void createUniversalInventoryTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.UNIVERSAL_INVENTORY)
                + "("
                + FIELD_UNIVERSAL_UUID
                + " UUID NOT NULL, "
                + FIELD_INVENTORY_SLOT
                + " TINYINT UNSIGNED NOT NULL, "
                + FIELD_INVENTORY_ITEM
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_UNIVERSAL_UUID
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.UNIVERSAL_RECORD)
                + "("
                + FIELD_UNIVERSAL_UUID
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY ("
                + FIELD_UNIVERSAL_UUID
                + ", "
                + FIELD_INVENTORY_SLOT
                + ")"
                + ");");
    }

    protected void createUniversalDataTable() {
        executeSql("CREATE TABLE IF NOT EXISTS "
                + SqlUtils.mapTable(DataScope.UNIVERSAL_DATA)
                + "("
                + FIELD_UNIVERSAL_UUID
                + " UUID NOT NULL, "
                + FIELD_DATA_KEY
                + " UUID NOT NULL, "
                + FIELD_DATA_VALUE
                + " TEXT NOT NULL, "
                + "FOREIGN KEY ("
                + FIELD_UNIVERSAL_UUID
                + ") "
                + "REFERENCES "
                + SqlUtils.mapTable(DataScope.UNIVERSAL_RECORD)
                + "("
                + FIELD_UNIVERSAL_UUID
                + ") "
                + "ON UPDATE CASCADE ON DELETE CASCADE, "
                + "PRIMARY KEY ("
                + FIELD_UNIVERSAL_UUID
                + ", "
                + FIELD_DATA_KEY
                + ")"
                + ");");
    }
}
