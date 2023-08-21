package io.github.thebusybiscuit.slimefun4.implementation.tasks.ticker;

import io.github.bakedlibs.dough.blocks.BlockPosition;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public record TickLocation(int x, int y, int z) {
    public static TickLocation fromBlockPosition(BlockPosition bp) {
        return new TickLocation(bp.getX(), bp.getY(), bp.getZ());
    }

    public static TickLocation fromLocation(Location l) {
        return new TickLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public Location toLocation(UUID uid) {
        var world = Bukkit.getWorld(uid);
        Objects.requireNonNull(world, "World cannot be null!");

        return new Location(world, x, y, z);
    }

    public boolean isSameLocation(Object obj) {
        if (obj instanceof TickLocation tl) {
            return tl.x == this.x && tl.y == this.y && tl.z == this.z;
        } else if (obj instanceof Location l) {
            return l.getBlockX() == this.x && l.getBlockY() == this.y && l.getBlockZ() == this.z;
        } else if (obj instanceof BlockPosition bp) {
            return bp.getX() == this.x && bp.getY() == this.y && bp.getZ() == this.z;
        } else {
            return false;
        }
    }
}
