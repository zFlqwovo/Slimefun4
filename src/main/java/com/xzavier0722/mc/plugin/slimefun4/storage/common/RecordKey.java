package com.xzavier0722.mc.plugin.slimefun4.storage.common;

import io.github.bakedlibs.dough.collections.Pair;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RecordKey {
    private final DataScope scope;
    private final Set<FieldKey> fields;
    private final List<Pair<FieldKey, String>> conditions;

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope) {
        this(scope, new HashSet<>());
    }

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope, Set<FieldKey> fields) {
        this(scope, fields, new LinkedList<>());
    }

    @ParametersAreNonnullByDefault
    public RecordKey(DataScope scope, Set<FieldKey> fields, List<Pair<FieldKey, String>> conditions) {
        this.scope = scope;
        this.fields = fields.isEmpty() ? fields : new HashSet<>(fields);
        this.conditions = conditions.isEmpty() ? conditions : new LinkedList<>(conditions);
    }

    @Nonnull
    public DataScope getScope() {
        return scope;
    }

    @ParametersAreNonnullByDefault
    public void addField(FieldKey field) {
        fields.add(field);
    }

    @Nonnull
    public Set<FieldKey> getFields() {
        return Collections.unmodifiableSet(fields);
    }

    @ParametersAreNonnullByDefault
    public void addCondition(FieldKey key, String val) {
        conditions.add(new Pair<>(key, val));
    }

    @Nonnull
    public List<Pair<FieldKey, String>> getConditions() {
        return Collections.unmodifiableList(conditions);
    }
}
