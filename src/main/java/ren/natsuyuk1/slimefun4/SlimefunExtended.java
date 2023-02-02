package ren.natsuyuk1.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SlimefunExtended {
    private static Logger logger = null;
    public static void register(@Nonnull Slimefun sf) {
        logger = sf.getLogger();
        logger.log(Level.INFO, "加载扩展保护组件...");
        ExtendedInteractManager.init(sf);
        VaultHelper.register(sf);
    }

    public static void shutdown() {
        logger = null;
        ExtendedInteractManager.shutdown();
        VaultHelper.shutdown();
    }

    public static Logger getLogger() {
        return logger;
    }
}
