package com.xzavier0722.mc.plugin.slimefun4.database.common;

import com.xzavier0722.mc.plugin.slimefun4.util.DataUtils;
import org.bukkit.inventory.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class RecordSet {
    private final Map<FieldKey, String> data;
    private boolean readonly = false;

    public RecordSet() {
        data = new HashMap<>();
    }

    @ParametersAreNonnullByDefault
    public void put(FieldKey key, String val) {
        checkReadonly();
        data.put(key, val);
    }

    @ParametersAreNonnullByDefault
    public void put(FieldKey key, ItemStack itemStack) {
        checkReadonly();
        data.put(key, DataUtils.itemStack2String(itemStack));
    }

    public Map<FieldKey, String> getAll() {
        return Collections.unmodifiableMap(data);
    }

    @ParametersAreNonnullByDefault
    public Optional<String> get(FieldKey key) {
        return Optional.ofNullable(data.get(key));
    }

    @ParametersAreNonnullByDefault
    public OptionalInt getInt(FieldKey key) {
        return get(key).map(s -> OptionalInt.of(Integer.parseInt(s))).orElseGet(OptionalInt::empty);
    }

    @ParametersAreNonnullByDefault
    public Optional<ItemStack> getItemStack(FieldKey key) {
        return get(key).flatMap(DataUtils::string2ItemStack);
    }

    public void readonly() {
        readonly = true;
    }

    private void checkReadonly() {
        if (readonly) {
            throw new IllegalStateException("RecordSet cannot be modified after readonly() called.");
        }
    }

}
