package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import com.xzavier0722.mc.plugin.slimefun4.storage.migrator.PlayerProfileMigrator;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class MigrateCommand extends SubCommand {
    MigrateCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "migrate", true);
    }

    @Nonnull
    @Override
    protected String getDescription() {
        return "commands.migrate.description";
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender.isOp() || sender instanceof ConsoleCommandSender) {
            if (PlayerProfileMigrator.getMigrateStatus()) {
                Slimefun.getLocalization().sendMessage(sender, "commands.migrate.in-progress", true);
                return;
            }

            Bukkit.getScheduler().runTask(Slimefun.instance(), () -> {
                try {
                    var status = PlayerProfileMigrator.migrateOldData();
                    switch (status) {
                        case SUCCESS ->
                                Slimefun.getLocalization().sendMessage(sender, "commands.migrate.success", true);
                        case FAILED -> Slimefun.getLocalization().sendMessage(sender, "commands.migrate.failed", true);
                        case MIGRATED ->
                                Slimefun.getLocalization().sendMessage(sender, "commands.migrate.already-migrated", true);
                    }
                } catch (Exception e) {
                    Slimefun.getLocalization().sendMessage(sender, "commands.migrate.failed", true);
                    plugin.getLogger().log(Level.WARNING, "迁移数据时出现意外", e);
                }
            });
        } else {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
        }
    }
}
