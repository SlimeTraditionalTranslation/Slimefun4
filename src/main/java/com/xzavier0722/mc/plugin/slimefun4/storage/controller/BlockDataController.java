package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import city.norain.slimefun4.utils.InventoryUtil;
import com.xzavier0722.mc.plugin.slimefun4.storage.adapter.IDataSourceAdapter;
import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataScope;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.DataType;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.FieldKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.RecordSet;
import com.xzavier0722.mc.plugin.slimefun4.storage.common.ScopeKey;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.DelayedSavingLooperTask;
import com.xzavier0722.mc.plugin.slimefun4.storage.task.DelayedTask;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.DataUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.InvStorageUtils;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import io.github.bakedlibs.dough.collections.Pair;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BlockDataController extends ADataController {

    private final Map<LinkedKey, DelayedTask> delayedWriteTasks;
    private final Map<String, SlimefunChunkData> loadedChunk;
    private final Map<String, List<Pair<ItemStack, Integer>>> invSnapshots;
    private final ScopedLock lock;
    private boolean enableDelayedSaving = false;
    private int delayedSecond = 0;
    private BukkitTask looperTask;

    BlockDataController() {
        super(DataType.BLOCK_STORAGE);
        delayedWriteTasks = new HashMap<>();
        loadedChunk = new ConcurrentHashMap<>();
        invSnapshots = new ConcurrentHashMap<>();
        lock = new ScopedLock();
    }

    @Override
    public void init(IDataSourceAdapter<?> dataAdapter, int maxReadThread, int maxWriteThread) {
        super.init(dataAdapter, maxReadThread, maxWriteThread);
        loadLoadedChunks();
    }

    private void loadLoadedChunks() {
        for (var world : Bukkit.getWorlds()) {
            for (var chunk : world.getLoadedChunks()) {
                loadChunk(chunk, false);
            }
        }
    }

    public void initDelayedSaving(Plugin p, int delayedSecond, int forceSavePeriod) {
        checkDestroy();
        if (delayedSecond < 1 || forceSavePeriod < 1) {
            throw new IllegalArgumentException("Second must be greater than 0!");
        }
        enableDelayedSaving = true;
        this.delayedSecond = delayedSecond;
        looperTask = Bukkit.getScheduler().runTaskTimerAsynchronously(p, new DelayedSavingLooperTask(
                forceSavePeriod,
                () -> {
                    synchronized (delayedWriteTasks) {
                        return new HashMap<>(delayedWriteTasks);
                    }
                },
                key -> {
                    synchronized (delayedWriteTasks) {
                        delayedWriteTasks.keySet().remove(key);
                    }
                }
        ), 20, 20);
    }

    public boolean isDelayedSavingEnabled() {
        return enableDelayedSaving;
    }

    public void setDelayedSavingEnable(boolean isEnable) {
        enableDelayedSaving = isEnable;
    }

    @Nonnull
    public SlimefunBlockData createBlock(Location l, String sfId) {
        checkDestroy();
        var re = getChunkDataCache(l.getChunk(), true).createBlockData(l, sfId);
        if (Slimefun.getRegistry().getTickerBlocks().contains(sfId)) {
            Slimefun.getTickerTask().enableTicker(l);
        }
        return re;
    }

    void saveNewBlock(Location l, String sfId) {
        var lKey = LocationUtils.getLocKey(l);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);

        var data = new RecordSet();
        data.put(FieldKey.LOCATION, lKey);
        data.put(FieldKey.CHUNK, LocationUtils.getChunkKey(l.getChunk()));
        data.put(FieldKey.SLIMEFUN_ID, sfId);

        var scopeKey = new LocationKey(DataScope.NONE, l);
        removeDelayedBlockDataUpdates(scopeKey); // Shouldn't have.. But for safe..
        scheduleWriteTask(scopeKey, key, data, true);
    }

    public void removeBlock(Location l) {
        checkDestroy();
        var removed = getChunkDataCache(l.getChunk(), true).removeBlockData(l);
        if (removed == null) {
            return;
        }

        var menu = removed.getBlockMenu();
        if (menu != null) {
            InventoryUtil.closeInventory(menu.toInventory());
        }

        if (Slimefun.getRegistry().getTickerBlocks().contains(removed.getSfId())) {
            Slimefun.getTickerTask().disableTicker(l);
        }
    }

    void removeBlockDirectly(Location l) {
        checkDestroy();
        var scopeKey = new LocationKey(DataScope.NONE, l);
        removeDelayedBlockDataUpdates(scopeKey);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, LocationUtils.getLocKey(l));
        scheduleDeleteTask(scopeKey, key, true);
    }

    @Nullable
    @ParametersAreNonnullByDefault
    public SlimefunBlockData getBlockData(Location l) {
        checkDestroy();
        var chunk = l.getChunk();
        var chunkData = getChunkDataCache(chunk, false);
        var lKey = LocationUtils.getLocKey(l);
        if (chunkData != null) {
            var re = chunkData.getBlockCacheInternal(lKey);
            if (re != null || chunkData.hasBlockCache(lKey) || chunkData.isDataLoaded()) {
                return re;
            }
        }

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addCondition(FieldKey.LOCATION, lKey);
        key.addField(FieldKey.SLIMEFUN_ID);

        var result = getData(key);
        var re = result.isEmpty() ? null : new SlimefunBlockData(l, result.get(0).get(FieldKey.SLIMEFUN_ID));
        if (re != null) {
            chunkData = getChunkDataCache(chunk, true);
            chunkData.addBlockCacheInternal(re, false);
            re = chunkData.getBlockCacheInternal(lKey);
        }
        return re;
    }

    public void getBlockDataAsync(Location l, IAsyncReadCallback<SlimefunBlockData> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getBlockData(l)));
    }

    public SlimefunBlockData getBlockDataFromCache(Location l) {
        return getBlockDataFromCache(LocationUtils.getChunkKey(l.getChunk()), LocationUtils.getLocKey(l));
    }

    public void setBlockDataLocation(SlimefunBlockData blockData, Location target) {
        if (LocationUtils.isSameLoc(blockData.getLocation(), target)) {
            return;
        }

        if (Slimefun.getRegistry().getTickerBlocks().contains(blockData.getSfId())) {
            Slimefun.getTickerTask().disableTicker(blockData.getLocation());
            Slimefun.getTickerTask().enableTicker(target);
        }

        var chunk = blockData.getLocation().getChunk();
        var chunkData = getChunkDataCache(chunk, false);
        if (chunkData != null) {
            chunkData.removeBlockDataCacheInternal(blockData.getKey());
        }

        var newBlockData = new SlimefunBlockData(target, blockData);
        var key = new RecordKey(DataScope.BLOCK_RECORD);
        if (LocationUtils.isSameChunk(blockData.getLocation().getChunk(), target.getChunk())) {
            if (chunkData == null) {
                chunkData = getChunkDataCache(chunk, true);
            }
            key.addField(FieldKey.CHUNK);
        } else {
            chunkData = getChunkDataCache(target.getChunk(), true);
        }
        chunkData.addBlockCacheInternal(newBlockData, true);

        var menu = blockData.getBlockMenu();
        if (menu != null) {
            newBlockData.setBlockMenu(new BlockMenu(menu.getPreset(), target, blockData.getMenuContents()));
        }

        key.addField(FieldKey.LOCATION);
        key.addCondition(FieldKey.LOCATION, blockData.getKey());

        var data = new RecordSet();
        data.put(FieldKey.LOCATION, newBlockData.getKey());
        data.put(FieldKey.CHUNK, chunkData.getKey());
        data.put(FieldKey.SLIMEFUN_ID, blockData.getSfId());
        var scopeKey = new LocationKey(DataScope.NONE, blockData.getLocation());
        synchronized (delayedWriteTasks) {
            var it = delayedWriteTasks.entrySet().iterator();
            while (it.hasNext()) {
                var next = it.next();
                if (scopeKey.equals(next.getKey().getParent())) {
                    next.getValue().runUnsafely();
                    it.remove();
                }
            }
        }
        scheduleWriteTask(scopeKey, key, data, true);
    }

    private SlimefunBlockData getBlockDataFromCache(String cKey, String lKey) {
        checkDestroy();
        var chunkData = loadedChunk.get(cKey);
        return chunkData == null ? null : chunkData.getBlockCacheInternal(lKey);
    }

    public void loadChunk(Chunk chunk, boolean isNewChunk) {
        checkDestroy();
        var chunkData = getChunkDataCache(chunk, true);

        if (isNewChunk) {
            chunkData.setIsDataLoaded(true);
            return;
        }

        if (chunkData.isDataLoaded()) {
            return;
        }

        loadChunkData(chunkData);

        var key = new RecordKey(DataScope.BLOCK_RECORD);
        key.addField(FieldKey.LOCATION);
        key.addField(FieldKey.SLIMEFUN_ID);
        key.addCondition(FieldKey.CHUNK, chunkData.getKey());

        getData(key).forEach(block -> {
            var lKey = block.get(FieldKey.LOCATION);
            var sfId = block.get(FieldKey.SLIMEFUN_ID);
            var sfItem = SlimefunItem.getById(sfId);
            if (sfItem == null) {
                return;
            }

            var cache = getBlockDataFromCache(chunkData.getKey(), lKey);
            var blockData = cache == null ? new SlimefunBlockData(LocationUtils.toLocation(lKey), sfId) : cache;
            chunkData.addBlockCacheInternal(blockData, false);

            if (sfItem.loadDataByDefault()) {
                scheduleReadTask(() -> {
                    loadBlockData(blockData);
                    if (sfItem.isTicking()) {
                        Slimefun.getTickerTask().enableTicker(blockData.getLocation());
                    }
                });
            }
        });
    }

    private void loadChunkData(SlimefunChunkData chunkData) {
        if (chunkData.isDataLoaded()) {
            return;
        }
        var key = new RecordKey(DataScope.CHUNK_DATA);
        key.addField(FieldKey.DATA_KEY);
        key.addField(FieldKey.DATA_VALUE);
        key.addCondition(FieldKey.CHUNK, chunkData.getKey());

        lock.lock(key);
        try {
            if (chunkData.isDataLoaded()) {
                return;
            }

            getData(key).forEach(data -> chunkData.setCacheInternal(
                    data.get(FieldKey.DATA_KEY),
                    DataUtils.blockDataDebase64(data.get(FieldKey.DATA_VALUE)),
                    false)
            );
            chunkData.setIsDataLoaded(true);
        } finally {
            lock.unlock(key);
        }
    }

    public void loadBlockData(SlimefunBlockData blockData) {
        if (blockData.isDataLoaded()) {
            return;
        }
        var key = new RecordKey(DataScope.BLOCK_DATA);
        key.addCondition(FieldKey.LOCATION, blockData.getKey());
        key.addField(FieldKey.DATA_KEY);
        key.addField(FieldKey.DATA_VALUE);

        lock.lock(key);
        try {
            if (blockData.isDataLoaded()) {
                return;
            }

            getData(key).forEach(
                    recordSet -> blockData.setCacheInternal(
                            recordSet.get(FieldKey.DATA_KEY),
                            DataUtils.blockDataDebase64(recordSet.get(FieldKey.DATA_VALUE)),
                            false
                    )
            );

            var menuPreset = BlockMenuPreset.getPreset(blockData.getSfId());
            if (menuPreset != null) {
                var menuKey = new RecordKey(DataScope.BLOCK_INVENTORY);
                menuKey.addCondition(FieldKey.LOCATION, blockData.getKey());
                menuKey.addField(FieldKey.INVENTORY_SLOT);
                menuKey.addField(FieldKey.INVENTORY_ITEM);

                var inv = new ItemStack[54];
                getData(menuKey).forEach(record -> inv[record.getInt(FieldKey.INVENTORY_SLOT)] = record.getItemStack(FieldKey.INVENTORY_ITEM));
                blockData.setBlockMenu(new BlockMenu(menuPreset, blockData.getLocation(), inv));
            }

            blockData.setIsDataLoaded(true);
        } finally {
            lock.unlock(key);
        }
    }

    public void loadBlockDataAsync(SlimefunBlockData blockData, IAsyncReadCallback<SlimefunBlockData> callback) {
        scheduleReadTask(() -> {
            loadBlockData(blockData);
            invokeCallback(callback, blockData);
        });
    }

    public SlimefunChunkData getChunkData(Chunk chunk) {
        checkDestroy();
        loadChunk(chunk, false);
        return getChunkDataCache(chunk, false);
    }

    public void getChunkDataAsync(Chunk chunk, IAsyncReadCallback<SlimefunChunkData> callback) {
        scheduleReadTask(() -> invokeCallback(callback, getChunkData(chunk)));
    }

    public void saveAllBlockInventories() {
        var chunks = new HashSet<>(loadedChunk.values());
        chunks.forEach(chunk -> chunk.getAllCacheInternal().forEach(block -> {
            if (block.isPendingRemove() || !block.isDataLoaded()) {
                return;
            }
            var menu = block.getBlockMenu();
            if (menu == null || !menu.isDirty()) {
                return;
            }

            saveBlockInventory(block);
        }));
    }

    public void saveBlockInventory(SlimefunBlockData blockData) {
        var newInv = blockData.getMenuContents();
        List<Pair<ItemStack, Integer>> lastSave;
        if (newInv == null) {
            lastSave = invSnapshots.remove(blockData.getKey());
            if (lastSave == null) {
                return;
            }
        } else {
            lastSave = invSnapshots.put(blockData.getKey(), InvStorageUtils.getInvSnapshot(newInv));
        }

        var changed = InvStorageUtils.getChangedSlots(lastSave, newInv);
        if (changed.isEmpty()) {
            return;
        }

        changed.forEach(slot -> scheduleDelayedBlockInvUpdate(blockData, slot));
    }

    private void scheduleDelayedBlockInvUpdate(SlimefunBlockData blockData, int slot) {
        var scopeKey = new LocationKey(DataScope.NONE, blockData.getLocation());
        var reqKey = new RecordKey(DataScope.BLOCK_INVENTORY);
        reqKey.addCondition(FieldKey.LOCATION, blockData.getKey());
        reqKey.addCondition(FieldKey.INVENTORY_SLOT, slot + "");
        reqKey.addField(FieldKey.INVENTORY_ITEM);

        if (enableDelayedSaving) {
            scheduleDelayedUpdateTask(
                    new LinkedKey(scopeKey, reqKey),
                    () -> scheduleBlockInvUpdate(scopeKey, reqKey, blockData.getKey(), blockData.getMenuContents(), slot)
            );
        } else {
            scheduleBlockInvUpdate(scopeKey, reqKey, blockData.getKey(), blockData.getMenuContents(), slot);
        }
    }

    private void scheduleBlockInvUpdate(ScopeKey scopeKey, RecordKey reqKey, String lKey, ItemStack[] inv, int slot) {
        var item = inv != null && slot < inv.length ? inv[slot] : null;

        if (item == null) {
            scheduleDeleteTask(scopeKey, reqKey, true);
        } else {
            var data = new RecordSet();
            data.put(FieldKey.LOCATION, lKey);
            data.put(FieldKey.INVENTORY_SLOT, slot + "");
            data.put(FieldKey.INVENTORY_ITEM, item);
            scheduleWriteTask(scopeKey, reqKey, data, true);
        }
    }

    @Override
    public void shutdown() {
        saveAllBlockInventories();
        if (enableDelayedSaving) {
            looperTask.cancel();
            executeAllDelayedTasks();
        }
        super.shutdown();
    }

    void scheduleDelayedBlockDataUpdate(SlimefunBlockData blockData, String key) {
        var scopeKey = new LocationKey(DataScope.NONE, blockData.getLocation());
        var reqKey = new RecordKey(DataScope.BLOCK_DATA);
        reqKey.addCondition(FieldKey.LOCATION, blockData.getKey());
        reqKey.addCondition(FieldKey.DATA_KEY, key);
        if (enableDelayedSaving) {
            scheduleDelayedUpdateTask(
                    new LinkedKey(scopeKey, reqKey),
                    () -> scheduleBlockDataUpdate(scopeKey, reqKey, blockData.getKey(), key, blockData.getData(key))
            );
        } else {
            scheduleBlockDataUpdate(scopeKey, reqKey, blockData.getKey(), key, blockData.getData(key));
        }
    }

    private void removeDelayedBlockDataUpdates(ScopeKey scopeKey) {
        synchronized (delayedWriteTasks) {
            delayedWriteTasks.entrySet().removeIf(each -> scopeKey.equals(each.getKey().getParent()));
        }
    }

    private void scheduleBlockDataUpdate(ScopeKey scopeKey, RecordKey reqKey, String lKey, String key, String val) {
        if (val == null) {
            scheduleDeleteTask(scopeKey, reqKey, false);
        } else {
            var data = new RecordSet();
            reqKey.addField(FieldKey.DATA_VALUE);
            data.put(FieldKey.LOCATION, lKey);
            data.put(FieldKey.DATA_KEY, key);
            data.put(FieldKey.DATA_VALUE, DataUtils.blockDataBase64(val));
            scheduleWriteTask(scopeKey, reqKey, data, true);
        }
    }

    void scheduleDelayedChunkDataUpdate(SlimefunChunkData chunkData, String key) {
        var scopeKey = new ChunkKey(DataScope.NONE, chunkData.getChunk());
        var reqKey = new RecordKey(DataScope.CHUNK_DATA);
        reqKey.addCondition(FieldKey.CHUNK, chunkData.getKey());
        reqKey.addCondition(FieldKey.DATA_KEY, key);

        if (enableDelayedSaving) {
            scheduleDelayedUpdateTask(
                    new LinkedKey(scopeKey, reqKey),
                    () -> scheduleChunkDataUpdate(scopeKey, reqKey, chunkData.getKey(), key, chunkData.getData(key))
            );
        } else {
            scheduleChunkDataUpdate(scopeKey, reqKey, chunkData.getKey(), key, chunkData.getData(key));
        }
    }

    private void scheduleDelayedUpdateTask(LinkedKey key, Runnable run) {
        synchronized (delayedWriteTasks) {
            var task = delayedWriteTasks.get(key);
            if (task != null && !task.isExecuted()) {
                task.setRunAfter(delayedSecond, TimeUnit.SECONDS);
                return;
            }

            task = new DelayedTask(delayedSecond, TimeUnit.SECONDS, run);
            delayedWriteTasks.put(key, task);
        }
    }

    private void scheduleChunkDataUpdate(ScopeKey scopeKey, RecordKey reqKey, String cKey, String key, String val) {
        if (val == null) {
            scheduleDeleteTask(scopeKey, reqKey, false);
        } else {
            var data = new RecordSet();
            reqKey.addField(FieldKey.DATA_VALUE);
            data.put(FieldKey.CHUNK, cKey);
            data.put(FieldKey.DATA_KEY, key);
            data.put(FieldKey.DATA_VALUE, DataUtils.blockDataBase64(val));
            scheduleWriteTask(scopeKey, reqKey, data, false);
        }
    }

    private void executeAllDelayedTasks() {
        synchronized (delayedWriteTasks) {
            delayedWriteTasks.values().forEach(DelayedTask::runUnsafely);
        }
    }

    private SlimefunChunkData getChunkDataCache(Chunk chunk, boolean createOnNotExists) {
        return createOnNotExists
                ? loadedChunk.computeIfAbsent(LocationUtils.getChunkKey(chunk), k -> new SlimefunChunkData(chunk))
                : loadedChunk.get(LocationUtils.getChunkKey(chunk));
    }
}
