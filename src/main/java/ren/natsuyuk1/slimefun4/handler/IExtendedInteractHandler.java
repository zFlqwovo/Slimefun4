package ren.natsuyuk1.slimefun4.handler;

import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import ren.natsuyuk1.slimefun4.event.AndroidMoveEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 */
public interface IExtendedInteractHandler {
    String name();

    boolean checkEnvironment();

    void initEnvironment() throws Exception;

    default void onAndroidMine(@Nonnull AndroidMineEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onAndroidMove(@Nonnull AndroidMoveEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onExplosiveToolBreakBlocks(@Nonnull ExplosiveToolBreakBlocksEvent event) {
    }

    default boolean checkInteraction(@Nullable OfflinePlayer player, @Nonnull Block block, @Nonnull Interaction interaction) {
        return true;
    }

    default void cleanup() {
    }
}
