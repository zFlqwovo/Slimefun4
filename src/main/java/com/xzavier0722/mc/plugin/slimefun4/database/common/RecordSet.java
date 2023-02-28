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
    private final Map<String, String> data;

    public RecordSet() {
        data = new HashMap<>();
    }

    @ParametersAreNonnullByDefault
    public void setData(String key, String val) {
        data.put(key, val);
    }

    @ParametersAreNonnullByDefault
    public void setData(String key, ItemStack itemStack) {
        data.put(key, DataUtils.itemStack2String(itemStack));
    }

    public Map<String, String> getAll() {
        return Collections.unmodifiableMap(data);
    }

    @ParametersAreNonnullByDefault
    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    @ParametersAreNonnullByDefault
    public OptionalInt getInt(String key) {
        return get(key).map(s -> OptionalInt.of(Integer.parseInt(s))).orElseGet(OptionalInt::empty);
    }

    @ParametersAreNonnullByDefault
    public Optional<ItemStack> getItemStack(String key) {
        return get(key).flatMap(DataUtils::string2ItemStack);
    }

}
