package ren.natsuyuk1.slimefun4.handler.bulitin;

import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.maxgamer.quickshop.api.QuickShopAPI;
import ren.natsuyuk1.slimefun4.SlimefunExtended;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.Level;

public class QuickShopHandler implements IExtendedInteractHandler {
    private static Object shopAPI = null;
    private static Method qsMethod = null;

    @Override
    public String name() {
        return "Quickshop";
    }

    @Override
    public boolean checkEnvironment() {
        return Bukkit.getPluginManager().getPlugin("Quickshop") != null;
    }

    @Override
    public void initEnvironment() throws Exception {
        var plugin = Bukkit.getPluginManager().getPlugin("Quickshop");
        var version = plugin.getDescription().getVersion();
        var splitVersion = version.split("-")[0].split("\\.");

        try {
            var major = Integer.parseInt(splitVersion[0]);
            var sub = Integer.parseInt(splitVersion[2]);
            var last = Integer.parseInt(splitVersion[3]);

            if (major < 5) {
                SlimefunExtended.getLogger().warning("QuickShop 版本过低, 建议你更新到 5.0.0+!");

                try {
                    var shopAPIMethod = Class.forName("org.maxgamer.quickshop.QuickShopAPI").getDeclaredMethod("getShopAPI");
                    shopAPIMethod.setAccessible(true);
                    shopAPI = shopAPIMethod.invoke(null);

                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException e) {
                    SlimefunExtended.getLogger().log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                    throw e;
                }

                if (sub >= 8 && last >= 2) {
                    // For 5.0.0-
                    try {
                        qsMethod = Class.forName("org.maxgamer.quickshop.api.ShopAPI").getDeclaredMethod("getShop", Location.class);
                        qsMethod.setAccessible(true);
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        SlimefunExtended.getLogger().log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                        throw e;
                    }
                } else {
                    // For 4.0.8-
                    try {
                        qsMethod = Class.forName("org.maxgamer.quickshop.api.ShopAPI").getDeclaredMethod("getShopWithCaching", Location.class);
                        qsMethod.setAccessible(true);
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        SlimefunExtended.getLogger().log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                        throw e;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            SlimefunExtended.getLogger().log(Level.WARNING, "解析 Quickshop-Reremake 版本失败, 实际为 " + version + ".");
            throw e;
        }
    }

    @Override
    public void onAndroidMine(@Nonnull AndroidMineEvent event, @Nonnull OfflinePlayer owner) {
        if (isQuickshop(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onAndroidFarm(@Nonnull AndroidFarmEvent event, @Nonnull OfflinePlayer owner) {
        if (isQuickshop(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onExplosiveToolBreakBlocks(@Nonnull ExplosiveToolBreakBlocksEvent event) {
        if (isQuickshop(event.getPrimaryBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        event.getAdditionalBlocks().removeIf(block -> isQuickshop(block.getLocation()));
    }

    @Override
    public boolean checkInteraction(@Nullable OfflinePlayer player, @Nonnull Block block, @Nonnull Interaction interaction) {
        if (interaction == Interaction.BREAK_BLOCK || interaction == Interaction.INTERACT_BLOCK) {
            return isQuickshop(block.getLocation());
        } else {
            return true;
        }
    }

    private boolean isQuickshop(@Nonnull Location l) {
        var qsPlugin = Bukkit.getPluginManager().getPlugin("QuickShop");

        if (qsPlugin == null) {
            return false;
        }

        if (qsMethod != null) {
            try {
                if (shopAPI == null) {
                    return false;
                }

                var result = qsMethod.invoke(shopAPI, l);

                if (result instanceof Optional optional) {
                    return optional.isPresent();
                } else {
                    return result != null;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                SlimefunExtended.getLogger().log(Level.WARNING, "在获取箱子商店时出现了问题", e);
                return true;
            }
        }

        if (qsPlugin instanceof QuickShopAPI qsAPI) {
            return qsAPI.getShopManager().getShop(l) != null;
        } else {
            SlimefunExtended.getLogger().log(Level.WARNING, "检查 QuickShop 失败，请避免使用热重载更换插件版本。如频繁出现该报错请反馈。");
            return false;
        }
    }

    @Override
    public void cleanup() {
        shopAPI = null;
        qsMethod = null;
    }
}
