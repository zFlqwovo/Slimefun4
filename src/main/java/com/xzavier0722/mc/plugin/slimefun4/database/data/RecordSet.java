package com.xzavier0722.mc.plugin.slimefun4.database.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class RecordSet {
    private final Map<String, String> data;

    public RecordSet() {
        data = new HashMap<>();
    }

    public void setData(String key, String val) {
        data.put(key, val);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(data.get(key));
    }

    public OptionalInt getInt(String key) {
        var data = get(key);
        if (data.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(data.get()));
    }

}
