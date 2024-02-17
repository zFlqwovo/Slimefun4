package io.github.thebusybiscuit.slimefun4.core.attributes;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.BlockFace;

/**
 * Implement this interface for any {@link SlimefunItem} to prevent
 * that {@link SlimefunItem} from being rotated to
 * {@link BlockFace}.NORTH_EAST
 * {@link BlockFace}.NORTH_WEST
 * {@link BlockFace}.SOUTH_EAST
 * {@link BlockFace}.SOUTH_WEST
 *
 * @author Ddggdd135
 *
 */
public interface NotDiagonallyRotatable extends ItemAttribute {}
