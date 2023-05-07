package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataUtils {
    public static String itemStack2String(ItemStack itemStack) {
        var stream = new ByteArrayOutputStream();
        try (var bs = new BukkitObjectOutputStream(stream)) {
            bs.writeObject(itemStack);
            return Base64Coder.encodeLines(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static ItemStack string2ItemStack(String base64Str) {
        if (base64Str == null || base64Str.isEmpty() || base64Str.isBlank()) {
            return null;
        }

        var stream = new ByteArrayInputStream(Base64Coder.decodeLines(base64Str));
        try (var bs = new BukkitObjectInputStream(stream)) {
            return (ItemStack) bs.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String base64(String text) {
        return Slimefun.getDatabaseManager().isBase64Enabled() ? Base64Coder.encodeString(text) : text;
    }

    public static String debase64(String base64Str) {
        return Slimefun.getDatabaseManager().isBase64Enabled() ? Base64Coder.decodeString(base64Str) : base64Str;
    }
}
