package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PlayerProfileDataController {
    private static volatile PlayerProfileDataController instance;

    private final PlayerBackpackCache backpackCache;
    private final Map<String, PlayerProfile> playerProfileCache;
    private IDataSourceAdapter<?> dataAdapter;
    private ExecutorService readExecutor;
    private ExecutorService writeExecutor;
    private ExecutorService callbackExecutor;

    public static PlayerProfileDataController getInstance() {
        if (instance == null) {
            synchronized (PlayerProfileDataController.class) {
                if (instance == null) {
                    instance = new PlayerProfileDataController();
                }
            }
        }

        return instance;
    }

    private PlayerProfileDataController() {
        backpackCache = new PlayerBackpackCache();
        playerProfileCache = new HashMap<>();
    }

    public void init(IDataSourceAdapter<?> dataAdapter, int maxReadThread, int maxWriteThread) {
        this.dataAdapter = dataAdapter;
        readExecutor = Executors.newFixedThreadPool(maxReadThread);
        writeExecutor = Executors.newFixedThreadPool(maxWriteThread);
        callbackExecutor = Executors.newCachedThreadPool();
    }

    public PlayerProfile getProfile(OfflinePlayer p) {
        var uuid = p.getUniqueId().toString();
        var re = playerProfileCache.get(uuid);
        if (re != null) {
            return re;
        }

        var key = new RecordKey(DataScope.PLAYER_PROFILE);
        key.addField(FieldKey.PLAYER_BACKPACK_NUM);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);

        var result = dataAdapter.getData(key);
        if (result.isEmpty()) {
            re = new PlayerProfile(p, 0);
            // TODO: save to storage
            return re;
        }

        var bNum = result.get(0).getInt(FieldKey.BACKPACK_NUMBER);

        var researches = new HashSet<Research>();
        getUnlockedResearchKeys(uuid).forEach(rKey -> {
            Research.getResearch(rKey).ifPresent(researches::add);
        });

        return new PlayerProfile(p, bNum, researches);
    }

    public void getProfileAsync(OfflinePlayer p, IAsyncReadCallback<PlayerProfile> callback) {
        readExecutor.submit(() -> {
            var re = getProfile(p);
            Runnable cb;
            if (re == null) {
                cb = callback::onResultNotFound;
            } else {
                cb = () -> callback.onResult(re);
            }

            if (callback.runOnMainThread()) {
                Slimefun.runSync(cb);
            } else {
                callbackExecutor.submit(cb);
            }
        });
    }

    public PlayerBackpack getBackpack(OfflinePlayer owner, int num) {
        var uuid = owner.getUniqueId().toString();
        var re = backpackCache.get(uuid, num);
        if (re != null) {
            return re;
        }

        var key = new RecordKey(DataScope.BACKPACK_PROFILE);
        key.addField(FieldKey.BACKPACK_ID);
        key.addField(FieldKey.BACKPACK_SIZE);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);
        key.addCondition(FieldKey.BACKPACK_NUMBER, num + "");

        var bResult = dataAdapter.getData(key);
        if (bResult.isEmpty()) {
            return null;
        }

        var id = bResult.get(0).getInt(FieldKey.BACKPACK_ID);
        key = new RecordKey(DataScope.BACKPACK_INVENTORY);
        key.addField(FieldKey.INVENTORY_SLOT);
        key.addField(FieldKey.INVENTORY_ITEM);
        key.addCondition(FieldKey.BACKPACK_ID, id + "");

        var invResult = dataAdapter.getData(key);

        var size = bResult.get(0).getInt(FieldKey.BACKPACK_SIZE);
        var items = new ItemStack[size];
        invResult.forEach(each -> items[each.getInt(FieldKey.INVENTORY_SLOT)] = each.getItemStack(FieldKey.INVENTORY_ITEM));

        return new PlayerBackpack(owner, id, size, items);
    }

    private Set<NamespacedKey> getUnlockedResearchKeys(String uuid) {
        var key = new RecordKey(DataScope.PLAYER_RESEARCH);
        key.addField(FieldKey.RESEARCH_ID);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);

        var result = dataAdapter.getData(key);
        if (result.isEmpty()) {
            return Collections.emptySet();
        }

        return result.stream().map(
                record -> NamespacedKey.fromString(record.get(FieldKey.RESEARCH_ID))
        ).collect(Collectors.toSet());
    }

    public void getBackpackAsync(OfflinePlayer owner, int num, IAsyncReadCallback<PlayerBackpack> callback) {
        readExecutor.submit(() -> {
            var re = getBackpack(owner, num);
            Runnable cb;
            if (re == null) {
                cb = callback::onResultNotFound;
            } else {
                cb = () -> callback.onResult(re);
            }

            if (callback.runOnMainThread()) {
                Slimefun.runSync(cb);
            } else {
                callbackExecutor.submit(cb);
            }
        });
    }
}
