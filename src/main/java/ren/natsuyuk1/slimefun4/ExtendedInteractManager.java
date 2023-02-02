package ren.natsuyuk1.slimefun4;

import io.github.thebusybiscuit.slimefun4.api.events.AndroidFarmEvent;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ren.natsuyuk1.slimefun4.event.AndroidMoveEvent;
import ren.natsuyuk1.slimefun4.handler.IExtendedInteractHandler;
import ren.natsuyuk1.slimefun4.handler.bulitin.QuickShopHandler;
import ren.natsuyuk1.slimefun4.handler.bulitin.ResidenceHandler;
import ren.natsuyuk1.slimefun4.utils.AndroidUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ExtendedInteractManager implements Listener {
    private static Logger logger = Logger.getLogger("SlimefunInteractManger");
    private static List<IExtendedInteractHandler> handlers = new ArrayList<>();
    private static final ExtendedInteractManager manager = new ExtendedInteractManager();


    private ExtendedInteractManager() {
    }

    static {
        handlers.add(new QuickShopHandler());
        handlers.add(new ResidenceHandler());
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

    protected static void init(@Nonnull Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(manager, plugin);
    }

    public static void register(@Nonnull IExtendedInteractHandler handler) {
        Objects.requireNonNull(handler, "Interact handler cannot be null!");

        if (!handlers.contains(handler) && handler.checkEnvironment()) {
            handlers.add(handler);
            logger.log(Level.INFO, "已注册扩展处理器: " + handler.name());
        }
    }

    public static void shutdown() {
        handlers.forEach(IExtendedInteractHandler::cleanup);
        handlers.clear();
    }
}
