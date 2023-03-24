package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class LocationUtils {
    public static String getLocKey(Location l) {
        return l.getWorld().getName() + ";" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public static String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ":" + chunk.getZ();
    }
}
