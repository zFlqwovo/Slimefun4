package com.xzavier0722.mc.plugin.slimefun4.storage.common;

public abstract class FieldValue {
    protected String value;

    protected FieldValue(String val) {
        value = val;
    }

    public abstract String serializeToSql(FieldKey key);
}
