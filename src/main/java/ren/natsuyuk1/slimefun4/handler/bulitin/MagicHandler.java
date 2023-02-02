package ren.natsuyuk1.slimefun4.handler.bulitin;

import com.elmakers.mine.bukkit.api.block.BlockData;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import ren.natsuyuk1.slimefun4.SlimefunExtended;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;

public class MagicHandler implements IExtendedInteractHandler {
    private Method magicBlockDataMethod = null;

    @Override
    public String name() {
        return "Magic";
    }

    @Override
    public boolean checkEnvironment() {
        return Bukkit.getPluginManager().getPlugin("Magic") != null;
    }

    @Override
    public void initEnvironment() throws Exception {
        var mp = Bukkit.getPluginManager().getPlugin("Magic");
        if (mp != null) {
            if (magicBlockDataMethod == null) {
                magicBlockDataMethod = Class.forName("com.elmakers.mine.bukkit.block.UndoList")
                        .getDeclaredMethod("getBlockData", Location.class);

                Objects.requireNonNull(magicBlockDataMethod, "Unable to get method from Magic");

                magicBlockDataMethod.setAccessible(true);
            }
        }
    }

    @Override
    public void onAndroidMine(@Nonnull AndroidMineEvent event, @Nullable OfflinePlayer owner) {
        var block = event.getBlock();
        try {
            var blockData = magicBlockDataMethod.invoke(null, block.getLocation());

            if (blockData instanceof BlockData) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            SlimefunExtended.getLogger().log(Level.WARNING, "调用 Magic 插件 API 失败", e);
        }
    }

    @Override
    public void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nullable OfflinePlayer owner) {
        var block = event.getBlock();
        try {
            var blockData = magicBlockDataMethod.invoke(null, block.getLocation());

            if (blockData instanceof BlockData) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            SlimefunExtended.getLogger().log(Level.WARNING, "调用 Magic 插件 API 失败", e);
        }
    }

    @Override
    public void cleanup() {
        magicBlockDataMethod = null;
    }
}
