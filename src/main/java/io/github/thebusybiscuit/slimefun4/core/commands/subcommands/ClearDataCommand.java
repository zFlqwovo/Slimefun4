package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController;
import io.github.thebusybiscuit.slimefun4.api.geo.GEOResource;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.core.config.SlimefunDatabaseManager;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class ClearDataCommand extends SubCommand {
    @ParametersAreNonnullByDefault
    public ClearDataCommand(Slimefun plugin, SlimefunCommand cmd) {
        super(plugin, cmd, "cleardata", false);
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (sender.hasPermission("slimefun.command.cleardata") || sender instanceof ConsoleCommandSender) {
            if (args.length == 3) {
                String arg1 = args[1];
                String arg2 = args[2];
                List<World> worlds = new ArrayList<>();
                List<String> availableClearTypes = List.of("block", "oil");
                List<String> clearTypes = new ArrayList<>();
                SlimefunDatabaseManager database = Slimefun.getDatabaseManager();
                BlockDataController controller = database.getBlockDataController();
                if (arg1.equals("*")) {
                    worlds.addAll(Bukkit.getWorlds());
                } else {
                    World toAdd = Bukkit.getWorld(arg1);
                    if (toAdd == null) {
                        sender.sendMessage(ChatColor.RED + "未找到世界", arg1);
                        return;
                    }
                }

                if (arg2.equals("*")) {
                    clearTypes.addAll(availableClearTypes);
                } else if (availableClearTypes.contains(arg2)) {
                    clearTypes.add(arg2);
                }

                for (World world : worlds) {
                    for (String cleartype : clearTypes) {
                        if (cleartype.equals("block")) {
                            controller.removeAllDataInWorldAsync(
                                    world,
                                    () -> Slimefun.runSync(() -> sender.sendMessage(ChatColor.GREEN + "已清除"
                                            + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + "的方块数据")));
                        } else if (cleartype.equals("oil")) {
                            GEOResource oil = null;
                            for (GEOResource resource :
                                    Slimefun.getRegistry().getGEOResources().values()) {
                                if (resource.getKey()
                                        .toString()
                                        .equals(new NamespacedKey(Slimefun.instance(), "oil").toString())) {
                                    oil = resource;
                                }
                            }
                            controller.removeDataInWorldAsync(
                                    world,
                                    oil.getKey().toString().replace(":", "-"),
                                    () -> Slimefun.runSync(() -> sender.sendMessage(ChatColor.GREEN + "已清除"
                                            + ChatColor.YELLOW + world.getName() + ChatColor.GREEN + "的石油数据")));
                        }
                    }
                }
            }
            Slimefun.getLocalization()
                    .sendMessage(
                            sender,
                            "messages.usage",
                            true,
                            msg -> msg.replace("%usage%", "/sf cleardata <World> <Type>"));
        } else {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
        }
    }
}
