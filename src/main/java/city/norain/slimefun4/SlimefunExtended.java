package city.norain.slimefun4;

import city.norain.slimefun4.listener.SlimefunMigrateListener;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import lombok.Getter;

public final class SlimefunExtended {
    private static SlimefunMigrateListener migrateListener = new SlimefunMigrateListener();

    public static boolean checkEnvironment(@Nonnull Slimefun sf) {
        if (EnvironmentChecker.checkHybridServer(sf.getLogger())) {
            return false;
        }

        return !EnvironmentChecker.checkIncompatiblePlugins(sf.getLogger());
    }

    public static void register(@Nonnull Slimefun sf) {
        EnvironmentChecker.scheduleSlimeGlueCheck(sf);

        VaultIntegration.register(sf);

        migrateListener.register(sf);
    }

    public static void shutdown() {
        migrateListener = null;

        VaultIntegration.cleanup();
    }
}
