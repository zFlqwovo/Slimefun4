package io.github.thebusybiscuit.slimefun4.core.attributes;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.block.BlockFace;

/**
 * Implement this interface for any {@link SlimefunItem} to prevent
 * that {@link SlimefunItem} from being rotated to
 * {@link BlockFace}.NORTH
 * {@link BlockFace}.EAST
 * {@link BlockFace}.SOUTH
 * {@link BlockFace}.WEST
 *
 * @author Ddggdd135
 *
 */
public interface NotCardinallyRotatable extends ItemAttribute {}
