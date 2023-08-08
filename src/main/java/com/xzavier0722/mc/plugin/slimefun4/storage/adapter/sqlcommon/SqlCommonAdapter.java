package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class SqlCommonAdapter<T> implements IDataSourceAdapter<T> {
    protected ConnectionPool pool;
    protected String profileTable, researchTable, backpackTable, bpInvTable;
    protected String blockRecordTable, blockDataTable, chunkDataTable, blockInvTable;

    protected void executeSql(String sql) {
        Connection conn = null;
        try {
            conn = pool.getConn();
            SqlUtils.execSql(conn, sql);
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            if (conn != null) {
                pool.releaseConn(conn);
            }
        }
    }

    protected List<RecordSet> executeQuery(String sql) {
        Connection conn = null;
        try {
            conn = pool.getConn();
            return SqlUtils.execQuery(conn, sql);
        } catch (SQLException | InterruptedException e) {
            throw new IllegalStateException("An exception thrown while executing sql: " + sql, e);
        } finally {
            if (conn != null) {
                pool.releaseConn(conn);
            }
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
            case NONE -> throw new IllegalArgumentException("NONE cannot be a storage data scope!");
        };
    }
}
