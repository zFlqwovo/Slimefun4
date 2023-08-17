package city.norain.slimefun4.utils;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

import java.util.LinkedList;

public class InventoryUtil {
    /**
     * Close inventory for all viewers.
     *
     * @param inventory {@link Inventory}
     */
    public static void closeInventory(Inventory inventory) {
        if (inventory != null) {
            new LinkedList<>(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }

    public static void closeInventory(Inventory inventory, Runnable callback) {
        if (Bukkit.isPrimaryThread()) {
            closeInventory(inventory);
            callback.run();
        } else {
            Slimefun.runSync(() -> {
                closeInventory(inventory);
                callback.run();
            });
        }
    }
}
