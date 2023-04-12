package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import io.github.bakedlibs.dough.collections.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InvStorageUtils {

    public static Set<Integer> getChangedSlots(List<Pair<ItemStack, Integer>> snapshot, ItemStack[] currContent) {
        var re = new HashSet<Integer>();
        for (var i = 0; i < currContent.length; i++) {
            var each = snapshot.get(i);
            var curr = currContent[i];
            if (curr == null) {
                if (each.getFirstValue() != null) {
                    re.add(i);
                }
                continue;
            }

            if (!curr.equals(each.getFirstValue()) || curr.getAmount() != each.getSecondValue()) {
                re.add(i);
            }
        }

        return re;
    }

    public static List<Pair<ItemStack, Integer>> getInvSnapshot(ItemStack[] invContents) {
        var re = new ArrayList<Pair<ItemStack, Integer>>();
        for (var each : invContents) {
            re.add(new Pair<>(each, each == null ? 0 : each.getAmount()));
        }

        return re;
    }
}
