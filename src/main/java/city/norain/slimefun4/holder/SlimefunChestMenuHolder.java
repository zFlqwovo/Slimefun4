package city.norain.slimefun4.holder;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nonnull;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.entity.Player;

public class SlimefunChestMenuHolder extends SlimefunInventoryHolder {
    private final Set<UUID> viewers = new CopyOnWriteArraySet<>();
    private final ChestMenu chestMenu;

    public SlimefunChestMenuHolder(@Nonnull ChestMenu chestMenu) {
        this.chestMenu = chestMenu;
    }

    public void addViewer(@Nonnull UUID uuid) {
        viewers.add(uuid);
    }

    public void removeViewer(@Nonnull UUID uuid) {
        viewers.remove(uuid);
    }

    public boolean contains(@Nonnull Player viewer) {
        return viewers.contains(viewer.getUniqueId());
    }

    public @Nonnull ChestMenu getChestMenu() {
        return chestMenu;
    }
}
