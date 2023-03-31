package com.xzavier0722.mc.plugin.slimefun4.autocrafter;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.NamespacedKey;

public interface SmartNamespacedKey {
    public NamespacedKey countKey = new NamespacedKey(Slimefun.instance(), "ingredientCount");
}
