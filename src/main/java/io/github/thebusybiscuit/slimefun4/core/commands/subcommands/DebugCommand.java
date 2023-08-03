package io.github.thebusybiscuit.slimefun4.core.commands.subcommands;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import io.github.thebusybiscuit.slimefun4.core.commands.SlimefunCommand;
import io.github.thebusybiscuit.slimefun4.core.commands.SubCommand;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The debug command will allow server owners to get information for us developers.
 * We can put debug messages in the code and they can trigger it for us to see what exactly is going on.
 *
 * @author WalshyDev
 */
public class DebugCommand extends SubCommand {

    protected DebugCommand(@Nonnull Slimefun plugin, @Nonnull SlimefunCommand cmd) {
        super(plugin, cmd, "debug", true);
    }

    @Override
    protected @Nonnull String getDescription() {
        return "commands.debug.description";
    }

    @Override
    public void onExecute(@Nonnull CommandSender sender, @Nonnull String[] args) {
        if (!sender.hasPermission("slimefun.command.debug")) {
            Slimefun.getLocalization().sendMessage(sender, "messages.no-permission", true);
            return;
        }

        if (args.length == 1) {
            String currentCase = String.join(", ", Debug.getTestCase());
            if (!currentCase.isEmpty()) {
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.current", true, msg -> msg.replace("%test_case%", currentCase));
            } else {
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.none-running", true);
            }
            return;
        }

        String test = args[1];

        switch (test.toLowerCase()) {
            case "disable", "off" -> {
                Debug.disableTestCase();
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.disabled");
            }
            case "get_all_blocks" -> {
                if (sender instanceof Player p) {
                    var data = Slimefun.getDatabaseManager().getBlockDataController().getAllBlockData(p.getWorld());
                    p.sendMessage("All block data in current world count: " + data.size());
                    p.sendMessage("Raw block datas: " + data.stream().map(bd -> bd.getSfId() + ";" + LocationUtils.locationToString(bd.getLocation())).collect(Collectors.joining(", ")));
                }
            }
            default -> {
                Debug.addTestCase(test);
                Slimefun.getLocalization().sendMessage(sender, "commands.debug.running", msg -> msg.replace("%test%", test));
            }
        }
    }
}
