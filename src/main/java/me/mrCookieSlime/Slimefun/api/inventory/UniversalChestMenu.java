package me.mrCookieSlime.Slimefun.api.inventory;

import java.util.UUID;
import javax.annotation.Nonnull;
import org.bukkit.inventory.ItemStack;

/**
 * This class represents a universal chest menu
 * which a menu located by certain identify id instead of location.
 */
public class UniversalChestMenu extends DirtyChestMenu {
    private final UUID uuid;

    public UniversalChestMenu(@Nonnull BlockMenuPreset preset, @Nonnull UUID uuid) {
        super(preset);

        this.uuid = uuid;
    }

    public UniversalChestMenu(BlockMenuPreset preset, @Nonnull UUID uuid, ItemStack[] contents) {
        super(preset);
        this.uuid = uuid;

        for (int i = 0; i < contents.length; i++) {
            var item = contents[i];
            if (item == null) {
                continue;
            }
            addItem(i, item);
        }

        preset.clone(this);
        this.getContents();
    }
}
