package city.norain.slimefun4;

import city.norain.slimefun4.listener.SlimefunMigrateListener;
import city.norain.slimefun4.utils.LogUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import com.zaxxer.hikari.util.DriverDataSource;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import lombok.Getter;

public final class SlimefunExtended {
    private static SlimefunMigrateListener migrateListener = new SlimefunMigrateListener();

    @Getter
    private static boolean databaseDebugMode = false;

    private static void checkDebug() {
        if ("true".equals(System.getProperty("slimefun.database.debug"))) {
            databaseDebugMode = true;
            LogUtil.setSlf4jLogLevel(HikariPool.class, org.apache.logging.log4j.Level.TRACE);
            LogUtil.setSlf4jLogLevel(HikariConfig.class, org.apache.logging.log4j.Level.DEBUG);
            LogUtil.setSlf4jLogLevel(HikariDataSource.class, org.apache.logging.log4j.Level.TRACE);
            LogUtil.setSlf4jLogLevel(DriverDataSource.class, org.apache.logging.log4j.Level.DEBUG);
        } else {
            LogUtil.setSlf4jLogLevel(HikariPool.class, org.apache.logging.log4j.Level.WARN);
            LogUtil.setSlf4jLogLevel(HikariConfig.class, org.apache.logging.log4j.Level.WARN);
            LogUtil.setSlf4jLogLevel(HikariDataSource.class, org.apache.logging.log4j.Level.WARN);
            LogUtil.setSlf4jLogLevel(DriverDataSource.class, org.apache.logging.log4j.Level.WARN);
        }
    }

    public static boolean checkEnvironment(@Nonnull Slimefun sf) {
        if (EnvironmentChecker.checkHybridServer()) {
            sf.getLogger().log(Level.WARNING, "#######################################################");
            sf.getLogger().log(Level.WARNING, "");
            sf.getLogger().log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            sf.getLogger().log(Level.WARNING, "检测到正在使用混合端, Slimefun 将会被禁用!");
            sf.getLogger().log(Level.WARNING, "混合端已被多个用户报告有使用问题,");
            sf.getLogger().log(Level.WARNING, "强制绕过检测将不受任何反馈支持.");
            sf.getLogger().log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            sf.getLogger().log(Level.WARNING, "");
            sf.getLogger().log(Level.WARNING, "#######################################################");
            return false;
        }

        if (Slimefun.getConfigManager().isBypassEnvironmentCheck()) {
            sf.getLogger().log(Level.WARNING, "#######################################################");
            sf.getLogger().log(Level.WARNING, "");
            sf.getLogger().log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            sf.getLogger().log(Level.WARNING, "检测到你禁用了环境兼容性检查!");
            sf.getLogger().log(Level.WARNING, "未通过兼容性检查将无法受到反馈支持.");
            sf.getLogger().log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            sf.getLogger().log(Level.WARNING, "");
            sf.getLogger().log(Level.WARNING, "#######################################################");
            return true;
        } else {
            return !EnvironmentChecker.checkIncompatiblePlugins(sf.getLogger());
        }
    }

    public static void register(@Nonnull Slimefun sf) {
        EnvironmentChecker.scheduleSlimeGlueCheck(sf);

        checkDebug();

        VaultIntegration.register(sf);

        migrateListener.register(sf);
    }

    public static void shutdown() {
        migrateListener = null;

        VaultIntegration.cleanup();

        databaseDebugMode = false;
    }
}
