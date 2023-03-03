package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.backpacks.Cooler;
import io.github.thebusybiscuit.slimefun4.implementation.items.backpacks.SlimefunBackpack;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ren.natsuyuk1.slimefun4.inventoryholder.SlimefunBackpackHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This {@link Listener} is responsible for all events centered around a {@link SlimefunBackpack}.
 * This also includes the {@link Cooler}
 * 
 * @author TheBusyBiscuit
 * @author Walshy
 * @author NihilistBrew
 * @author AtomicScience
 * @author VoidAngel
 * @author John000708
 * 
 * @see SlimefunBackpack
 * @see PlayerBackpack
 *
 */
public class BackpackListener implements Listener {

    private final Map<UUID, ItemStack> backpacks = new HashMap<>();
    private final Map<UUID, Set<Integer>> changedSlots = new HashMap<>();

    public void register(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        if (e.getInventory().getHolder() instanceof SlimefunBackpackHolder holder) {
            var item = backpacks.get(p.getUniqueId());

            PlayerBackpack.getAsync(item, (bp -> {
                if (bp == holder.getBackpack()) {
                    saveBackpackInv(bp);
                    p.playSound(p.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1F, 1F);
                }
            }), true);
        }
    }

    private void saveBackpackInv(PlayerBackpack bp) {
        var slots = changedSlots.remove(bp.getUniqueId());
        if (slots == null) {
            return;
        }

        Slimefun.getRegistry().getProfileDataController().saveBackpackInventory(bp, slots);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (backpacks.containsKey(e.getPlayer().getUniqueId())) {
            ItemStack item = e.getItemDrop().getItemStack();
            SlimefunItem sfItem = SlimefunItem.getByItem(item);

            if (sfItem instanceof SlimefunBackpack) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        ItemStack item = backpacks.get(e.getWhoClicked().getUniqueId());

        if (item != null) {
            SlimefunItem backpack = SlimefunItem.getByItem(item);

            if (backpack instanceof SlimefunBackpack slimefunBackpack) {
                if (e.getClick() == ClickType.NUMBER_KEY) {
                    // Prevent disallowed items from being moved using number keys.
                    if (e.getClickedInventory().getType() != InventoryType.PLAYER) {
                        ItemStack hotbarItem = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());

                        if (!isAllowed(slimefunBackpack, hotbarItem)) {
                            e.setCancelled(true);
                        }
                    }
                } else if (e.getClick() == ClickType.SWAP_OFFHAND) {
                    if (e.getClickedInventory().getType() != InventoryType.PLAYER) {
                        // Fixes #3265 - Don't move disallowed items using the off hand.
                        ItemStack offHandItem = e.getWhoClicked().getInventory().getItemInOffHand();

                        if (!isAllowed(slimefunBackpack, offHandItem)) {
                            e.setCancelled(true);
                        }
                    } else {
                        // Fixes #3664 - Do not swap the backpack to your off hand.
                        if (e.getCurrentItem() != null && e.getCurrentItem().isSimilar(item)) {
                            e.setCancelled(true);
                        }
                    }
                } else if (!isAllowed(slimefunBackpack, e.getCurrentItem())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemChanged(InventoryClickEvent e) {
        var p = e.getWhoClicked();
        if (!backpacks.containsKey(p.getUniqueId())) {
            return;
        }

        if (!(e.getInventory().getHolder() instanceof SlimefunBackpackHolder holder)) {
            return;
        }

        var bp = holder.getBackpack();
        var slot = e.getRawSlot();
        if (slot >= bp.getSize()) {
            return;
        }

        changedSlots.computeIfAbsent(bp.getUniqueId(), k -> new HashSet<>()).add(slot);
    }

    private boolean isAllowed(@Nonnull SlimefunBackpack backpack, @Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return true;
        }

        return backpack.isItemAllowed(item, SlimefunItem.getByItem(item));
    }

    @ParametersAreNonnullByDefault
    public void openBackpack(Player p, ItemStack item, SlimefunBackpack backpack) {
        if (item.getAmount() == 1) {
            if (backpack.canUse(p, true) && !PlayerProfile.get(p, profile -> openBackpack(p, item, profile, backpack.getSize()))) {
                Slimefun.getLocalization().sendMessage(p, "messages.opening-backpack");
            }
        } else {
            Slimefun.getLocalization().sendMessage(p, "backpack.no-stack", true);
        }
    }

    @ParametersAreNonnullByDefault
    private void openBackpack(Player p, ItemStack item, PlayerProfile profile, int size) {
        var meta = item.getItemMeta();
        if (PlayerBackpack.getUuid(meta).isEmpty() && PlayerBackpack.getNum(meta).isEmpty()) {
            // Create backpack
            PlayerBackpack.setUuid(
                    item,
                    Slimefun.getRegistry().getProfileDataController().createBackpack(
                            p,
                            profile.nextBackpackNum(),
                            size
                    ).getUniqueId().toString()
            );
        }

        /*
         * If the current Player is already viewing a backpack (for whatever reason),
         * terminate that view.
         */
        if (backpacks.containsKey(p.getUniqueId())) {
            p.closeInventory();
        }

        // Check if someone else is currently viewing this backpack
        if (!backpacks.containsValue(item)) {
            p.playSound(p.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1F, 1F);

            PlayerBackpack.getAsync(
                    item,
                    backpack -> backpack.open(p, () -> backpacks.put(p.getUniqueId(), item)),
                    false);
        } else {
            Slimefun.getLocalization().sendMessage(p, "backpack.already-open", true);
        }
    }

    /**
     * This method sets the id for a backpack onto the given {@link ItemStack}.
     * 
     * @param backpackOwner
     *            The owner of this backpack
     * @param item
     *            The {@link ItemStack} to modify
     * @param line
     *            The line at which the ID should be replaced
     * @param id
     *            The id of this backpack
     */
    public void setBackpackId(@Nonnull OfflinePlayer backpackOwner, @Nonnull ItemStack item, int line, int id) {
        Validate.notNull(backpackOwner, "Backpacks must have an owner!");
        Validate.notNull(item, "Cannot set the id onto null!");

        ItemMeta im = item.getItemMeta();

        if (!im.hasLore()) {
            throw new IllegalArgumentException("This backpack does not have any lore!");
        }

        List<String> lore = im.getLore();

        if (line >= lore.size() || !lore.get(line).contains("<ID>")) {
            throw new IllegalArgumentException("Specified a line that is out of bounds or invalid!");
        }

        lore.set(line, lore.get(line).replace("<ID>", backpackOwner.getUniqueId() + "#" + id));
        im.setLore(lore);
        item.setItemMeta(im);
    }
}
