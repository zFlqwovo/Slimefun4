package com.xzavier0722.mc.plugin.slimefun4.storage.common.fields;

import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldValue;

public class PatternValue extends FieldValue {
    public PatternValue(String val) {
        super(val);
    }

    @Override
    public String serializeToSql(FieldKey key) {
        return "LIKE '" + value + "'";
    }
}
