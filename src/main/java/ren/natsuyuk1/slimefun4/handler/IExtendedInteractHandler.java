package ren.natsuyuk1.slimefun4.handler;

import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import org.bukkit.OfflinePlayer;
import ren.natsuyuk1.slimefun4.event.AndroidMoveEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 */
public interface IExtendedInteractHandler {
    String name();

    boolean checkEnvironment();

    void initEnvironment();

    default void onAndroidMine(@Nonnull AndroidMineEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onAndroidMove(@Nonnull AndroidMoveEvent event, @Nullable OfflinePlayer owner) {
    }

    default void onExplosiveToolBreakBlocks(@Nonnull ExplosiveToolBreakBlocksEvent event) {
    }

    default void cleanup() {
    }
}
