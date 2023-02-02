package ren.natsuyuk1.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SlimefunExtended {
    private static Logger logger = null;
    public static void register(@Nonnull Slimefun sf) {
        logger = sf.getLogger();

        if (Bukkit.getPluginManager().getPlugin("SlimeGlue") == null) {
            logger.log(Level.INFO, "检测到没有安装 SlimeGlue (粘液胶), 你将缺失对一些插件的额外保护检查!");
            logger.log(Level.INFO, "下载: https://github.com/Xzavier0722/SlimeGlue");
        }

        VaultHelper.register(sf);
    }

    public static void shutdown() {
        logger = null;
        VaultHelper.shutdown();
    }

    public static Logger getLogger() {
        return logger;
    }
}
