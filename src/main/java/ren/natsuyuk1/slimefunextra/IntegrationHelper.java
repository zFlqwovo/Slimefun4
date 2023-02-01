package ren.natsuyuk1.slimefunextra;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.elmakers.mine.bukkit.api.block.BlockData;
import io.github.bakedlibs.dough.protection.ActionType;
import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.JsonUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.QuickShopAPI;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 保护插件权限检查器
 *
 * @author StarWishsama
 */
public class IntegrationHelper implements Listener {

    private static final String RESIDENCE = "Residence";
    private static final String QUICKSHOP = "QuickShop";
    private static final String MAGIC = "Magic";

    private static boolean resInstalled = false;
    private static boolean qsInstalled = false;
    private static boolean magicInstalled = false;
    private static Method qsMethod = null;
    private static Object shopAPI = null;
    private static Method magicBlockDataMethod = null;
    private static Logger logger;

    private static final IntegrationHelper instance = new IntegrationHelper();

    private IntegrationHelper() {
    }

    public static void register(@Nonnull Slimefun plugin) {
        resInstalled = plugin.getServer().getPluginManager().getPlugin(RESIDENCE) != null;
        qsInstalled = plugin.getServer().getPluginManager().getPlugin(QUICKSHOP) != null;
        magicInstalled = plugin.getServer().getPluginManager().getPlugin(MAGIC) != null;
        logger = plugin.getLogger();

        if (qsInstalled) {
            logger.log(Level.INFO, "检测到 Quickshop, 相关功能已开启");
            registerQuickShop(plugin);
        }

        if (resInstalled) {
            logger.log(Level.INFO, "检测到 Residence, 相关功能已开启");
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        }

        if (magicInstalled) {
            logger.log(Level.INFO, "检测到 Magic, 相关功能已开启");
        }
    }

    public static void shutdown() {
        qsMethod = null;
        shopAPI = null;
        logger = null;
    }

    @EventHandler
    public void onAndroidFarm(AndroidFarmEvent e) {
        handleAndroidBreak(e);
    }

    @EventHandler
    public void onAndroidMine(AndroidMineEvent e) {
        handleAndroidBreak(e);
    }

    /**
     * 处理机器人破坏方块
     *
     * @param event 机器人破坏事件
     */
    private void handleAndroidBreak(@Nonnull AndroidMineEvent event) {
        try {
            var android = event.getAndroid().getBlock();
            var block = event.getBlock();
            var p = Bukkit.getOfflinePlayer(getOwnerFromJson(BlockStorage.getBlockInfoAsJson(android)));

            if (!checkResidence(p, android, Interaction.BREAK_BLOCK)) {
                event.setCancelled(true);
                if (p.isOnline() && p.getPlayer() != null) {
                    Slimefun.getLocalization().sendMessage(p.getPlayer(), "android.no-permission");
                }
            }

            if (!checkMagicBlock(block)) {
                event.setCancelled(true);
            }
        } catch (Exception x) {
            Slimefun.logger().log(Level.WARNING, "在处理机器人破坏方块时遇到了意外", x);
        }
    }

    /**
     * 处理机器人破坏方块
     *
     * @param event 机器人破坏事件
     */
    private void handleAndroidBreak(@Nonnull AndroidFarmEvent event) {
        try {
            var android = event.getAndroid().getBlock();
            var block = event.getBlock();
            var p = Bukkit.getOfflinePlayer(getOwnerFromJson(BlockStorage.getBlockInfoAsJson(android)));

            if (!checkResidence(p, android, Interaction.BREAK_BLOCK)) {
                event.setCancelled(true);
                if (p.isOnline() && p.getPlayer() != null) {
                    Slimefun.getLocalization().sendMessage(p.getPlayer(), "android.no-permission");
                }
            }

            if (!checkMagicBlock(block)) {
                event.setCancelled(true);
            }
        } catch (Exception x) {
            Slimefun.logger().log(Level.WARNING, "在处理机器人破坏方块时遇到了意外", x);
        }
    }

    /**
     * 检查是否可以在领地内破坏/交互方块
     * <p>
     * 领地已支持 Slimefun
     * <p>
     * 详见: <a href="https://github.com/Zrips/Residence/blob/master/src/com/bekvon/bukkit/residence/slimeFun/SlimeFunResidenceModule.java">...</a>
     *
     * @param p      玩家
     * @param block  被破坏的方块
     * @param action 交互类型
     * @return 是否可以破坏
     */
    public static boolean checkResidence(OfflinePlayer p, Block block, Interaction action) {
        if (!resInstalled || block == null || p == null || !p.isOnline() || p.isOp()) {
            return true;
        }

        var res = Residence.getInstance().getResidenceManager().getByLoc(block.getLocation());

        if (res != null) {
            if (res.getOwnerUUID() == p.getUniqueId()) {
                return true;
            }

            var onlinePlayer = p.getPlayer();

            if (onlinePlayer == null) {
                return false;
            }

            if (onlinePlayer.hasPermission("residence.admin")) {
                return true;
            }

            var perms = res.getPermissions();

            if (perms != null) {
                if (action.getType() == ActionType.BLOCK && perms.playerHas(onlinePlayer, Flags.admin, FlagPermissions.FlagCombo.OnlyTrue)) {
                    return true;
                }

                switch (action) {
                    case BREAK_BLOCK:
                        return perms.playerHas(onlinePlayer, Flags.destroy, FlagPermissions.FlagCombo.OnlyTrue);
                    case INTERACT_BLOCK:
                        return perms.playerHas(onlinePlayer, Flags.container, FlagPermissions.FlagCombo.OnlyTrue);
                    case PLACE_BLOCK:
                        // move 是为了机器人而检查的, 防止机器人跑进别人领地然后还出不来
                        return perms.playerHas(onlinePlayer, Flags.place, FlagPermissions.FlagCombo.OnlyTrue) || perms.playerHas(onlinePlayer, Flags.build, FlagPermissions.FlagCombo.OnlyTrue) && perms.playerHas(onlinePlayer, Flags.move, FlagPermissions.FlagCombo.TrueOrNone);
                }
            }
        }
        return true;
    }

    public static UUID getOwnerFromJson(String json) {
        if (json != null) {
            var element = JsonUtils.parseString(json);
            if (!element.isJsonNull()) {
                var object = element.getAsJsonObject();
                return UUID.fromString(object.get("owner").getAsString());
            }
        }
        return null;
    }

    public static boolean checkQuickShop(@Nonnull Location l) {
        if (!qsInstalled) {
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
                logger.log(Level.WARNING, "在获取箱子商店时出现了问题", e);
                return true;
            }
        }

        var qsPlugin = Bukkit.getPluginManager().getPlugin("QuickShop");

        if (qsPlugin instanceof QuickShopAPI qsAPI) {
            return qsAPI.getShopManager().getShop(l) != null;
        }

        logger.log(Level.WARNING, "与QuickShop的兼容出现问题，请避免使用热重载更换插件版本。如频繁出现该问题请反馈至粘液科技汉化版。");
        return false;
    }

    private static void registerQuickShop(@Nonnull Slimefun plugin) {
        var version = plugin.getServer().getPluginManager().getPlugin(QUICKSHOP).getDescription().getVersion();
        var splitVersion = version.split("-")[0].split("\\.");

        try {
            var major = Integer.parseInt(splitVersion[0]);
            var sub = Integer.parseInt(splitVersion[2]);
            var last = Integer.parseInt(splitVersion[3]);

            if (major < 5) {
                logger.warning("QuickShop 版本过低, 建议你更新到 5.0.0+!");

                try {
                    var shopAPIMethod = Class.forName("org.maxgamer.quickshop.QuickShopAPI").getDeclaredMethod("getShopAPI");
                    shopAPIMethod.setAccessible(true);
                    shopAPI = shopAPIMethod.invoke(null);

                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InvocationTargetException ignored) {
                    logger.log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                    qsInstalled = false;
                }

                if (sub >= 8 && last >= 2) {
                    // For 5.0.0-
                    try {
                        qsMethod = Class.forName("org.maxgamer.quickshop.api.ShopAPI").getDeclaredMethod("getShop", Location.class);
                        qsMethod.setAccessible(true);
                    } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                        logger.log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                        qsInstalled = false;
                    }
                } else {
                    // For 4.0.8-
                    try {
                        qsMethod = Class.forName("org.maxgamer.quickshop.api.ShopAPI").getDeclaredMethod("getShopWithCaching", Location.class);
                        qsMethod.setAccessible(true);
                    } catch (ClassNotFoundException | NoSuchMethodException ignored) {
                        logger.log(Level.INFO, "无法接入 Quickshop-Reremake " + version + " , 请更新到最新版, 相关功能将自动关闭");
                        qsInstalled = false;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.WARNING, "无法解析 Quickshop-Reremake 版本, 实际为 " + version + ".");
            qsInstalled = false;
        }
    }

    public static boolean checkMagicBlock(@Nonnull Block block) {
        var mp = Bukkit.getPluginManager().getPlugin("Magic");
        if (mp != null) {
            try {
                if (magicBlockDataMethod == null) {
                    magicBlockDataMethod = Class.forName("com.elmakers.mine.bukkit.block.UndoList").getDeclaredMethod("getBlockData", Location.class);
                    magicBlockDataMethod.setAccessible(true);
                }

                var result = magicBlockDataMethod.invoke(null, block.getLocation());

                if (result instanceof BlockData magicData) {
                    return magicData.isFake();
                } else {
                    return true;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "找不到 Magic 插件兼容所需类", e);
                return true;
            }
        } else {
            return true;
        }
    }
}
