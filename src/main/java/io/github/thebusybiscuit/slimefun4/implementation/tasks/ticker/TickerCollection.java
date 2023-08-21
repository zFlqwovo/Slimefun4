package io.github.thebusybiscuit.slimefun4.implementation.tasks.ticker;

import io.github.bakedlibs.dough.blocks.BlockPosition;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class TickerCollection {
    private final Map<UUID, Set<TickLocation>> tickLocations = new ConcurrentHashMap<>();

    public Set<TickLocation> getBlocks(@Nonnull BlockPosition block) {
        return tickLocations.computeIfAbsent(block.getWorld().getUID(), k -> Collections.synchronizedSet(new HashSet<>()));
    }

    public void tickBlock(@Nonnull BlockPosition block) {
        getBlocks(block).add(TickLocation.fromBlockPosition(block));
    }

    public void tickBlock(@Nonnull Location location) {
        tickBlock(new BlockPosition(location));
    }

    public void removeBlock(@Nonnull BlockPosition block) {
        var uuid = block.getWorld().getUID();
        var locations = tickLocations.get(uuid);

        if (locations == null) {
            return;
        }

        if (locations.isEmpty()) {
            tickLocations.remove(uuid);
            return;
        }

        locations.removeIf(location -> location.isSameLocation(block));

        if (locations.isEmpty()) {
            tickLocations.remove(uuid);
        }
    }

    public void removeBlock(@Nonnull Location location) {
        removeBlock(new BlockPosition(location));
    }

    public Set<Location> getBlocksLocation() {
        tickLocations.keySet().removeIf(uuid -> Bukkit.getWorld(uuid) == null);
        Set<Location> locations = new HashSet<>();

        for (Map.Entry<UUID, Set<TickLocation>> entry : tickLocations.entrySet()) {
            for (TickLocation location : entry.getValue()) {
                locations.add(location.toLocation(entry.getKey()));
            }
        }

        return Collections.synchronizedSet(locations);
    }
}
