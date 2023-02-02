package ren.natsuyuk1.slimefun4.utils;

import io.github.thebusybiscuit.slimefun4.utils.JsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class AndroidUtil {
    public static OfflinePlayer getAndroidOwner(String json) {
        if (json != null) {
            var element = JsonUtils.parseString(json);
            if (!element.isJsonNull()) {
                var object = element.getAsJsonObject();
                return Bukkit.getOfflinePlayer(UUID.fromString(object.get("owner").getAsString()));
            }
        }
        return null;
    }
}
