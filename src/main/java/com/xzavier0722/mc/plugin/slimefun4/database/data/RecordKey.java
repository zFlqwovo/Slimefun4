package com.xzavier0722.mc.plugin.slimefun4.database.data;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecordKey {
    private final DataScope scope;
    private final Set<String> fields;
    private final Map<String, String> conditions;

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope) {
        this(scope, new HashSet<>());
    }

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope, Set<String> fields) {
        this(scope, fields, new HashMap<>());
    }

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope, Set<String> fields, Map<String, String> conditions) {
        this.scope = scope;
        this.fields = fields.isEmpty() ? fields : new HashSet<>(fields);
        this.conditions = conditions.isEmpty() ? conditions : new HashMap<>(conditions);
    }

    @Nonnull
    public DataScope getScope() {
        return scope;
    }

    @ParametersAreNonnullByDefault
    public void addField(String field) {
        fields.add(field);
    }

    @ParametersAreNonnullByDefault
    public void removeField(String field) {
        fields.remove(field);
    }

    @Nonnull
    public Set<String> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    @ParametersAreNonnullByDefault
    public void addCondition(String key, String val) {
        conditions.put(key, val);
    }

    @ParametersAreNonnullByDefault
    public void removeCondition(String key) {
        conditions.remove(key);
    }

    @Nonnull
    public Map<String, String> getConditions() {
        return Collections.unmodifiableMap(conditions);
    }
}
