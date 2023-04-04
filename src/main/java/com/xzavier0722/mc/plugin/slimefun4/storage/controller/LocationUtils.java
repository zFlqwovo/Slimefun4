package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class LocationUtils {
    public static String getLocKey(Location l) {
        return l.getWorld().getName() + ";" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public static String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ":" + chunk.getZ();
    }

    public static Location toLocation(String lKey) {
        var strArr = lKey.split(";");
        var loc = strArr[1].split(":");
        return new Location(
                Bukkit.getWorld(strArr[0]),
                Double.parseDouble(loc[0]),
                Double.parseDouble(loc[1]),
                Double.parseDouble(loc[2])
        );
    }
}
