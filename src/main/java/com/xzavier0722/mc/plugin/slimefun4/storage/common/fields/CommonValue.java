package com.xzavier0722.mc.plugin.slimefun4.storage.common.fields;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.sqlcommon.SqlUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldValue;

public class CommonValue extends FieldValue {
    public CommonValue(String val) {
        super(val);
    }

    @Override
    public String serializeToSql(FieldKey key) {
        return " = " + SqlUtils.toSqlValStr(key, value);
    }
}
