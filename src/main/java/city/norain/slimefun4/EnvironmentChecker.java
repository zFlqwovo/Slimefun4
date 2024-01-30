package city.norain.slimefun4;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

class EnvironmentChecker {
    private static final List<String> UNSUPPORTED_PLUGINS =
        List.of("BedrockTechnology", "SlimefunFix", "SlimefunBugFixer", "Slimefunbookfix", "MiraiMC");

    static boolean checkIncompatiblePlugins(@Nonnull Logger logger) {
        List<String> plugins = UNSUPPORTED_PLUGINS.stream()
            .filter(name -> Bukkit.getServer().getPluginManager().isPluginEnabled(name))
            .toList();

        if (plugins.isEmpty()) {
            return false;
        }

        printBorder(logger);
        logger.log(Level.WARNING, "");
        logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "检测到不兼容的插件, 已自动禁用 Slimefun!");
        logger.log(Level.WARNING, "插件列表: ", String.join(", ", plugins));
        logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(Level.WARNING, "");
        printBorder(logger);

        return true;
    }

    static boolean checkHybridServer(@Nonnull Logger logger) {
        try {
            Class.forName("cpw.mods.modlauncher.Launcher");
            Class.forName("net.minecraftforge.server.console.TerminalHandler");

            printBorder(logger);
            logger.log(Level.WARNING, "");
            logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.log(Level.WARNING, "检测到正在使用混合端, Slimefun 将会被禁用!");
            logger.log(Level.WARNING, "混合端已被多个用户报告有使用问题, ");
            logger.log(Level.WARNING, "强制绕过检测将不受任何反馈支持. ");
            logger.log(Level.WARNING, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.log(Level.WARNING, "");
            printBorder(logger);

            return true;
        } catch (ClassNotFoundException ignored) {
            if (Bukkit.getPluginCommand("mohist") != null) {
                return true;
            }

            var serverVer = Bukkit.getVersion().toLowerCase();

            return serverVer.contains("arclight") || serverVer.contains("mohist");
        }
    }

    static void scheduleSlimeGlueCheck(@Nonnull Slimefun sf) {
        Bukkit.getScheduler()
            .runTaskLater(
                sf,
                () -> {
                    if (Bukkit.getPluginManager().getPlugin("SlimeGlue") == null) {
                        sf.getLogger().log(Level.WARNING, "检测到没有安装 SlimeGlue (粘液胶), 你将缺失对一些插件的额外保护检查!");
                        sf.getLogger().log(Level.WARNING, "下载: https://github.com/Xzavier0722/SlimeGlue");
                    }
                },
                300); // 15s
    }

    private static void printBorder(@Nonnull Logger logger) {
        logger.log(Level.WARNING, "#######################################################");
    }
}
