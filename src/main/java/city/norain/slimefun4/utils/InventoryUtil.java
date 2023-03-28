package city.norain.slimefun4.utils;

import java.util.LinkedList;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;

public class InventoryUtil {
    public static void closeInventory(Inventory inventory) {
        if (inventory != null) {
            new LinkedList<>(inventory.getViewers()).forEach(HumanEntity::closeInventory);
        }
    }
}
