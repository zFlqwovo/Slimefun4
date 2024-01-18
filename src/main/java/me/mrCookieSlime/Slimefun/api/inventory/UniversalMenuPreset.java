package me.mrCookieSlime.Slimefun.api.inventory;

import javax.annotation.Nonnull;
import org.bukkit.block.Block;

public abstract class UniversalMenuPreset extends BlockMenuPreset {
    /**
     * Creates a new ChestMenu with the specified
     * Title
     *
     * @param title The title of the Menu
     */
    public UniversalMenuPreset(@Nonnull String id, @Nonnull String title) {
        super(id, title);
    }

    public void newInstance(@Nonnull UniversalChestMenu menu, @Nonnull Block b) {
        // This method can optionally be overridden by implementations
    }

    protected void clone(@Nonnull DirtyChestMenu menu, @Nonnull Block block) {
        menu.setPlayerInventoryClickable(true);

        for (int slot : occupiedSlots) {
            menu.addItem(slot, getItemInSlot(slot));
        }

        if (size > -1) {
            menu.addItem(size - 1, null);
        }

        if (menu instanceof UniversalChestMenu universalChestMenu) {
            newInstance(universalChestMenu, block);
        }

        for (int slot = 0; slot < 54; slot++) {
            if (getMenuClickHandler(slot) != null) {
                menu.addMenuClickHandler(slot, getMenuClickHandler(slot));
            }
        }

        menu.addMenuOpeningHandler(getMenuOpeningHandler());
        menu.addMenuCloseHandler(getMenuCloseHandler());
    }
}
