package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.AsyncWriteTask;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerBackpack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerProfileDataController {
    private static volatile PlayerProfileDataController instance;

    private final PlayerBackpackCache backpackCache;
    private final Map<String, PlayerProfile> profileCache;
    private final Map<RecordKey, AsyncWriteTask> scheduledWriteTasks;
    private volatile IDataSourceAdapter<?> dataAdapter;
    private ExecutorService readExecutor;
    private ExecutorService writeExecutor;
    private ExecutorService callbackExecutor;
    private volatile boolean destroyed = false;

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
        profileCache = new ConcurrentHashMap<>();
        scheduledWriteTasks = new ConcurrentHashMap<>();
    }

    public void init(IDataSourceAdapter<?> dataAdapter, int maxReadThread, int maxWriteThread) {
        checkDestroy();
        this.dataAdapter = dataAdapter;
        readExecutor = Executors.newFixedThreadPool(maxReadThread);
        writeExecutor = Executors.newFixedThreadPool(maxWriteThread);
        callbackExecutor = Executors.newCachedThreadPool();
    }

    public PlayerProfile getProfile(OfflinePlayer p) {
        checkDestroy();
        var uuid = p.getUniqueId().toString();
        var re = profileCache.get(uuid);
        if (re != null) {
            return re;
        }

        var key = new RecordKey(DataScope.PLAYER_PROFILE);
        key.addField(FieldKey.PLAYER_BACKPACK_NUM);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);

        var result = dataAdapter.getData(key);
        if (result.isEmpty()) {
            return createProfile(p);
        }

        var bNum = result.get(0).getInt(FieldKey.BACKPACK_NUMBER);

        var researches = new HashSet<Research>();
        getUnlockedResearchKeys(uuid).forEach(rKey -> {
            Research.getResearch(rKey).ifPresent(researches::add);
        });

        re = new PlayerProfile(p, bNum, researches);
        profileCache.put(uuid, re);
        return re;
    }

    public void getProfileAsync(OfflinePlayer p, IAsyncReadCallback<PlayerProfile> callback) {
        checkDestroy();
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
        checkDestroy();
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

        var idStr= bResult.get(0).get(FieldKey.BACKPACK_ID);
        var size = Integer.parseInt(bResult.get(0).get(FieldKey.BACKPACK_SIZE));

        re = new PlayerBackpack(
                owner,
                UUID.fromString(idStr),
                num,
                size,
                getBackpackInv(idStr, size)
        );
        backpackCache.put(re);
        return re;
    }

    public PlayerBackpack getBackpack(String uuid) {
        var re = backpackCache.get(uuid);
        if (re != null) {
            return re;
        }

        var key = new RecordKey(DataScope.BACKPACK_PROFILE);
        key.addField(FieldKey.BACKPACK_ID);
        key.addField(FieldKey.BACKPACK_SIZE);
        key.addField(FieldKey.BACKPACK_NUMBER);
        key.addField(FieldKey.PLAYER_UUID);
        key.addCondition(FieldKey.BACKPACK_ID, uuid);

        var resultSet = dataAdapter.getData(key);
        if (resultSet.isEmpty()) {
            return null;
        }

        var result = resultSet.get(0);
        var idStr= result.get(FieldKey.BACKPACK_ID);
        var size = result.getInt(FieldKey.BACKPACK_SIZE);

        re = new PlayerBackpack(
                Bukkit.getOfflinePlayer(UUID.fromString(result.get(FieldKey.PLAYER_UUID))),
                UUID.fromString(idStr),
                result.getInt(FieldKey.BACKPACK_NUMBER),
                size,
                getBackpackInv(idStr, size)
        );
        backpackCache.put(re);
        return re;

    }

    private ItemStack[] getBackpackInv(String uuid, int size) {
        var key = new RecordKey(DataScope.BACKPACK_INVENTORY);
        key.addField(FieldKey.INVENTORY_SLOT);
        key.addField(FieldKey.INVENTORY_ITEM);
        key.addCondition(FieldKey.BACKPACK_ID, uuid);

        var invResult = dataAdapter.getData(key);
        var re = new ItemStack[size];
        invResult.forEach(each -> re[each.getInt(FieldKey.INVENTORY_SLOT)] = each.getItemStack(FieldKey.INVENTORY_ITEM));

        return re;
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
        checkDestroy();
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

    public void getBackpackAsync(String uuid, IAsyncReadCallback<PlayerBackpack> callback) {
        checkDestroy();
        readExecutor.submit(() -> {
            var re = getBackpack(uuid);
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

    private PlayerProfile createProfile(OfflinePlayer p) {
        checkDestroy();
        var uuid = p.getUniqueId().toString();
        var cache = profileCache.get(uuid);
        if (cache != null) {
            return cache;
        }

        var re = new PlayerProfile(p, 0);
        profileCache.put(uuid, re);

        var key = new RecordKey(DataScope.PLAYER_PROFILE);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);
        scheduleWriteTask(key, getRecordSet(re));
        return re;
    }

    public void shutdown() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        readExecutor.shutdownNow();
        callbackExecutor.shutdownNow();
        try {
            var pendingTask = scheduledWriteTasks.size();
            while (pendingTask > 0) {
                Slimefun.logger().log(Level.SEVERE, "数据保存中，请稍候... 剩余 " + pendingTask + " 个任务");
                Thread.sleep(3000);
                pendingTask = scheduledWriteTasks.size();
            }
        } catch (InterruptedException e) {
            Slimefun.logger().log(Level.SEVERE, "Exception thrown while saving data: ", e);
        }
        writeExecutor.shutdown();
        dataAdapter = null;
        backpackCache.clean();
        profileCache.clear();
    }

    public void setResearch(String uuid, NamespacedKey researchKey, boolean unlocked) {
        var key = new RecordKey(DataScope.PLAYER_RESEARCH);
        key.addCondition(FieldKey.PLAYER_UUID, uuid);
        key.addCondition(FieldKey.RESEARCH_ID, researchKey.toString());
        if (unlocked) {
            var data = new RecordSet();
            data.put(FieldKey.PLAYER_UUID, uuid);
            data.put(FieldKey.RESEARCH_ID, researchKey.toString());
            dataAdapter.setData(key, data);
        } else {
            scheduleWriteTask(key);
        }
    }

    public PlayerBackpack createBackpack(OfflinePlayer p, int num, int size) {
        var re = new PlayerBackpack(p, UUID.randomUUID(), num, size, null);
        var key = new RecordKey(DataScope.BACKPACK_PROFILE);
        key.addCondition(FieldKey.BACKPACK_ID, re.getUniqueId().toString());
        scheduleWriteTask(key, getRecordSet(re));
        return re;
    }

    public void saveBackpackSize(PlayerBackpack bp) {
        var key = new RecordKey(DataScope.BACKPACK_PROFILE);
        key.addCondition(FieldKey.BACKPACK_ID, bp.getUniqueId().toString());
        key.addField(FieldKey.BACKPACK_SIZE);
        scheduleWriteTask(key, getRecordSet(bp));
    }

    public void saveProfileBackpackCount(PlayerProfile profile) {
        var key = new RecordKey(DataScope.PLAYER_PROFILE);
        key.addField(FieldKey.PLAYER_BACKPACK_NUM);
        key.addCondition(FieldKey.PLAYER_UUID, profile.getUUID().toString());
        scheduleWriteTask(key, getRecordSet(profile));
    }

    public void saveBackpackInventory(PlayerBackpack bp, Set<Integer> slots) {
        var id = bp.getUniqueId().toString();
        var inv = bp.getInventory();
        slots.forEach(slot -> {
            var key = new RecordKey(DataScope.BACKPACK_INVENTORY);
            key.addCondition(FieldKey.BACKPACK_ID, id);
            key.addCondition(FieldKey.INVENTORY_SLOT, slot + "");
            key.addField(FieldKey.INVENTORY_ITEM);
            var is = inv.getItem(slot);
            if (is == null) {
                scheduleWriteTask(key);
            } else {
                var data = new RecordSet();
                data.put(FieldKey.BACKPACK_ID, id);
                data.put(FieldKey.INVENTORY_SLOT, slot + "");
                data.put(FieldKey.INVENTORY_ITEM, is);
                scheduleWriteTask(key, data);
            }
        });
    }

    private void checkDestroy() {
        if (destroyed) {
            throw new IllegalStateException("Controller cannot be accessed after destroyed.");
        }
    }

    private static RecordSet getRecordSet(PlayerBackpack bp) {
        var re = new RecordSet();
        re.put(FieldKey.PLAYER_UUID, bp.getOwner().getUniqueId().toString());
        re.put(FieldKey.BACKPACK_ID, bp.getUniqueId().toString());
        re.put(FieldKey.BACKPACK_NUMBER, bp.getId() + "");
        re.put(FieldKey.BACKPACK_SIZE, bp.getSize() + "");
        return re;
    }

    private static RecordSet getRecordSet(PlayerProfile profile) {
        var re = new RecordSet();
        re.put(FieldKey.PLAYER_UUID, profile.getUUID().toString());
        re.put(FieldKey.PLAYER_NAME, profile.getOwner().getName());
        re.put(FieldKey.PLAYER_BACKPACK_NUM, profile.getBackpackCount() + "");
        return re;
    }

    public void scheduleWriteTask(RecordKey key) {
        scheduleWriteTask(key, () -> dataAdapter.deleteData(key));
    }

    private void scheduleWriteTask(RecordKey key, RecordSet data) {
        scheduleWriteTask(key, () -> dataAdapter.setData(key, data));
    }

    private void scheduleWriteTask(RecordKey key, Runnable task) {
        var writeTask = new AsyncWriteTask() {
            @Override
            protected boolean execute() {
                var lastScheduledTask = scheduledWriteTasks.get(key);
                if (lastScheduledTask != null && lastScheduledTask != this) {
                    return false;
                }
                task.run();
                return true;
            }

            @Override
            protected void onSuccess() {
                scheduledWriteTasks.remove(key);
            }

            @Override
            protected void onError(Throwable e) {
                Slimefun.logger().log(Level.SEVERE, "Exception thrown while executing write task: ");
                e.printStackTrace();
            }
        };
        scheduledWriteTasks.put(key, writeTask);
        writeExecutor.submit(writeTask);
    }
}
