package ren.natsuyuk1.slimefun4;

import io.github.bakedlibs.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.ExplosiveToolBreakBlocksEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ren.natsuyuk1.slimefun4.event.AndroidMoveEvent;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;
import ren.natsuyuk1.slimefun4.handler.bulitin.MagicHandler;
import ren.natsuyuk1.slimefun4.handler.bulitin.QuickShopHandler;
import ren.natsuyuk1.slimefun4.handler.bulitin.ResidenceHandler;
import ren.natsuyuk1.slimefun4.utils.AndroidUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public final class ExtendedInteractManager implements Listener {
    private static List<IExtendedInteractHandler> handlers = new ArrayList<>();
    private static final ExtendedInteractManager manager = new ExtendedInteractManager();


    private ExtendedInteractManager() {
    }

    static {
        register(new MagicHandler());
        register(new QuickShopHandler());
        register(new ResidenceHandler());
    }

    @EventHandler
    public void onAndroidFarm(AndroidFarmEvent e) {
        handlers.forEach(handler -> handler.onAndroidFarm(e, AndroidUtil.getAndroidOwner(BlockStorage.getBlockInfoAsJson(e.getAndroid().getBlock()))));
    }

    @EventHandler
    public void onAndroidMine(AndroidMineEvent e) {
        handlers.forEach(handler -> handler.onAndroidMine(e, AndroidUtil.getAndroidOwner(BlockStorage.getBlockInfoAsJson(e.getAndroid().getBlock()))));
    }

    @EventHandler
    public void onAndroidMove(AndroidMoveEvent e) {
        handlers.forEach(handler -> handler.onAndroidMove(e, AndroidUtil.getAndroidOwner(BlockStorage.getBlockInfoAsJson(e.getAndroid().getBlock()))));
    }

    @EventHandler
    public void onExplosiveToolBreakBlock(ExplosiveToolBreakBlocksEvent e) {
        handlers.forEach(handler -> handler.onExplosiveToolBreakBlocks(e));
    }

    protected static void init(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(manager, plugin);
    }

    public static void register(@Nonnull IExtendedInteractHandler handler) {
        Objects.requireNonNull(handler, "Interact handler cannot be null!");
        loadHandler(handler);
    }

    public static boolean checkInteraction(@Nonnull OfflinePlayer player, @Nonnull Block block, @Nonnull Interaction interaction) {
        for (IExtendedInteractHandler handler : handlers) {
            if (!handler.checkInteraction(player, block, interaction)) {
                return false;
            }
        }

        return true;
    }

    private static void loadHandler(IExtendedInteractHandler handler) {
        if (!handlers.contains(handler) && handler.checkEnvironment()) {
            try {
                handler.initEnvironment();
                handlers.add(handler);
                SlimefunExtended.getLogger().log(Level.INFO, "已注册保护检查模块: " + handler.name());
            } catch (Exception e) {
                if (!(e instanceof ReflectiveOperationException)) {
                    SlimefunExtended.getLogger().log(Level.WARNING, "加载保护检查模块 " + handler.name() + " 失败", e);
                } else {
                    SlimefunExtended.getLogger().log(Level.WARNING, "加载保护检查模块 " + handler.name() + " 失败");
                }
            }
        }
    }

    public static void shutdown() {
        handlers.forEach(IExtendedInteractHandler::cleanup);
        handlers.clear();
    }
}
