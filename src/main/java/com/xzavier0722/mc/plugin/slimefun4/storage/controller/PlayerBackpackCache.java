package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class PlayerBackpackCache {
    private final Map<String, Map<Integer, PlayerBackpack>> cache;

    PlayerBackpackCache() {
        cache = new ConcurrentHashMap<>();
    }

    void put(String uuid, int num, PlayerBackpack backpack) {
        var map = cache.computeIfAbsent(uuid, k -> new HashMap<>());
        map.put(num, backpack);
    }

    PlayerBackpack get(String uuid, int num) {
        var map = cache.get(uuid);
        return map == null ? null : map.get(num);
    }

}
