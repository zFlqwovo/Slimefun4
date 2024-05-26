package com.xzavier0722.mc.plugin.slimefun4.storage.adapter.mysql;

import static com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlConstants.*;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlCommonAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import java.util.List;

public class MysqlAdapter extends SqlCommonAdapter<MysqlConfig> {
    @Override
    public void initStorage(DataType type) {
        switch (type) {
            case PLAYER_PROFILE -> {
                profileTable = SqlUtils.mapTable(DataScope.PLAYER_PROFILE, config.tablePrefix());
                researchTable = SqlUtils.mapTable(DataScope.PLAYER_RESEARCH, config.tablePrefix());
                backpackTable = SqlUtils.mapTable(DataScope.BACKPACK_PROFILE, config.tablePrefix());
                bpInvTable = SqlUtils.mapTable(DataScope.BACKPACK_INVENTORY, config.tablePrefix());
                createProfileTables();
            }
            case BLOCK_STORAGE -> {
                blockRecordTable = SqlUtils.mapTable(DataScope.BLOCK_RECORD, config.tablePrefix());
                blockDataTable = SqlUtils.mapTable(DataScope.BLOCK_DATA, config.tablePrefix());
                blockInvTable = SqlUtils.mapTable(DataScope.BLOCK_INVENTORY, config.tablePrefix());
                chunkDataTable = SqlUtils.mapTable(DataScope.CHUNK_DATA, config.tablePrefix());
                universalInvTable = SqlUtils.mapTable(DataScope.UNIVERSAL_INVENTORY, config.tablePrefix());
                createBlockStorageTables();
            }
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

        if (!updateFields.isEmpty() && key.getConditions().isEmpty()) {
            throw new IllegalArgumentException("Condition is required for update statement!");
        }

        executeSql("INSERT INTO "
                + mapTable(key.getScope())
                + " ("
                + fieldStr.get()
                + ") "
                + "VALUES ("
                + valStr
                + ")"
                + (updateFields.isEmpty()
                        ? ""
                        : " ON DUPLICATE KEY UPDATE "
                                + String.join(
                                        ", ",
                                        updateFields.stream()
                                                .map(field -> {
                                                    var val = item.get(field);
                                                    if (val == null) {
                                                        throw new IllegalArgumentException(
                                                                "Cannot find value in RecordSet for the specific key: "
                                                                        + field);
                                                    }
                                                    return SqlUtils.buildKvStr(field, val);
                                                })
                                                .toList()))
                + ";");
    }

    @Override
    public List<RecordSet> getData(RecordKey key, boolean distinct) {
        return executeQuery((distinct ? "SELECT DISTINCT " : "SELECT ")
                + SqlUtils.buildFieldStr(key.getFields()).orElse("*")
                + " FROM "
                + mapTable(key.getScope())
                + SqlUtils.buildConditionStr(key.getConditions())
                + ";");
    }

    @Override
    public void deleteData(RecordKey key) {
        executeSql("DELETE FROM " + mapTable(key.getScope()) + SqlUtils.buildConditionStr(key.getConditions()) + ";");
    }
}
