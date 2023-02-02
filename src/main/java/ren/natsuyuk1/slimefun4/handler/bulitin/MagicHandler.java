package ren.natsuyuk1.slimefun4.handler.bulitin;

import com.elmakers.mine.bukkit.api.block.BlockData;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MagicHandler implements IExtendedInteractHandler {
    private static Logger logger = Logger.getLogger("SFMagicHandler");
    private Method magicBlockDataMethod = null;

    @Override
    public String name() {
        return "Magic";
    }

    @Override
    public boolean checkEnvironment() {
        return Bukkit.getPluginManager().isPluginEnabled("Magic");
    }

    @Override
    public void initEnvironment() {
        var mp = Bukkit.getPluginManager().getPlugin("Magic");
        if (mp != null) {
            try {
                if (magicBlockDataMethod == null) {
                    magicBlockDataMethod = Class.forName("com.elmakers.mine.bukkit.block.UndoList").getDeclaredMethod("getBlockData", Location.class);
                    magicBlockDataMethod.setAccessible(true);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "找不到 Magic 插件兼容所需类", e);
            }
        }
    }

    @Override
    public void onAndroidMine(@Nonnull AndroidMineEvent event, @Nullable OfflinePlayer owner) {
        var block = event.getBlock();
        try {
            var blockData = magicBlockDataMethod.invoke(null, block.getLocation());

            if (blockData != null && blockData instanceof BlockData) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "调用 Magic 插件 API 失败", e);
        }
    }

    @Override
    public void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nullable OfflinePlayer owner) {
        var block = event.getBlock();
        try {
            var blockData = magicBlockDataMethod.invoke(null, block.getLocation());

            if (blockData != null && blockData instanceof BlockData) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "调用 Magic 插件 API 失败", e);
        }
    }

    @Override
    public void cleanup() {
        magicBlockDataMethod = null;
    }
}
