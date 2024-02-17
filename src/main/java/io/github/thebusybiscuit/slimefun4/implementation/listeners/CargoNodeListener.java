package io.github.thebusybiscuit.slimefun4.implementation.listeners;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.cargo.CargoNode;
import javax.annotation.Nonnull;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * This {@link Listener} is solely responsible for preventing Cargo Nodes from being placed
 * on the top or bottom of a block.
 *
 * @author TheBusyBiscuit
 *
 */
public class CargoNodeListener implements Listener {

    public CargoNodeListener(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCargoNodePlace(BlockPlaceEvent e) {
        if (isCargoNode(e.getItemInHand())) {
            Block b = e.getBlock();

            // || !e.getBlockReplacedState().getType().isAir() 这会导致#832
            Block against = e.getBlock();
            BlockData blockData = b.getBlockData();
            BlockFace blockFace = null;
            Vector vector;
            if (blockData instanceof Directional directional){
                blockFace = directional.getFacing();
            }
            else if (blockData instanceof Rotatable rotatable){
                blockFace = rotatable.getRotation();
            }
            if (blockFace == null){
                vector = new Vector();
            }
            else {
                vector = blockFace.getOppositeFace().getDirection();
            }
            Block realAgainst = against.getWorld().getBlockAt(against.getX() + (int) vector.getX(), against.getY() + (int) vector.getY(), against.getZ() + (int) vector.getZ());
            if (!isContainer(realAgainst)) {
                Slimefun.getLocalization().sendMessage(e.getPlayer(), "machines.CARGO_NODES.must-be-placed", true);
                e.setCancelled(true);
            }
        }
    }

    private boolean isCargoNode(@Nonnull ItemStack item) {
        return SlimefunItem.getByItem(item) instanceof CargoNode;
    }
    private boolean isContainer(@Nonnull Block block) {
        return block.getState() instanceof Container || Slimefun.getDatabaseManager().getBlockDataController().getBlockData(block.getLocation()) != null;
    }
}
