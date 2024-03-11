package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import lombok.Getter;
import lombok.Setter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.UniversalMenu;
import org.bukkit.inventory.ItemStack;

public class SlimefunUniversalData extends ASlimefunDataContainer {
    @Getter
    private final String sfId;
    @Setter
    @Getter
    private volatile UniversalMenu menu;
    @Setter
    @Getter
    private volatile boolean pendingRemove = false;

    @ParametersAreNonnullByDefault
    SlimefunUniversalData(UUID uuid, String sfId) {
        super(uuid.toString());
        this.sfId = sfId;
    }

    @ParametersAreNonnullByDefault
    SlimefunUniversalData(UUID uuid, SlimefunUniversalData other) {
        super(uuid.toString(), other);
        this.sfId = other.sfId;
    }

    @ParametersAreNonnullByDefault
    public void setData(String key, String val) {
        checkData();
        setCacheInternal(key, val, true);
        Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedUniversalDataUpdate(this, key);
    }

    @ParametersAreNonnullByDefault
    public void removeData(String key) {
        if (removeCacheInternal(key) != null || !isDataLoaded()) {
            Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedUniversalDataUpdate(this, key);
        }
    }

    @Nullable public ItemStack[] getMenuContents() {
        if (menu == null) {
            return null;
        }
        var re = new ItemStack[54];
        var presetSlots = menu.getPreset().getPresetSlots();
        var inv = menu.toInventory().getContents();
        for (var i = 0; i < inv.length; i++) {
            if (presetSlots.contains(i)) {
                continue;
            }
            re[i] = inv[i];
        }

        return re;
    }

    @Override
    public String toString() {
        return "SlimefunUniversalData [sfId="
            + sfId
            + ", isPendingRemove="
            + pendingRemove
            + "]";
    }
}
