package com.xzavier0722.mc.plugin.slimefun4.storage.util;

import com.xzavier0722.mc.plugin.slimefun4.storage.callback.IAsyncReadCallback;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;

/**
 * Utils to access the cached block data.
 * It is safe to use when the target block is in a loaded chunk (such as in block events).
 * By default, please use
 * {@link com.xzavier0722.mc.plugin.slimefun4.storage.controller.BlockDataController#getBlockData}
 */
public class StorageCacheUtils {
    private static final Set<SlimefunBlockData> loadingData = new HashSet<>();

    @ParametersAreNonnullByDefault
    public static boolean hasBlock(Location l) {
        return getBlock(l) != null;
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static SlimefunBlockData getBlock(Location l) {
        return Slimefun.getDatabaseManager().getBlockDataController().getBlockDataFromCache(l);
    }

    @ParametersAreNonnullByDefault
    public static boolean isBlock(Location l, String id) {
        var blockData = getBlock(l);
        return blockData != null && id.equals(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static SlimefunItem getSfItem(Location l) {
        var blockData = getBlock(l);
        return blockData == null ? null : SlimefunItem.getById(blockData.getSfId());
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static String getData(Location loc, String key) {
        var blockData = getBlock(loc);
        return blockData == null ? null : blockData.getData(key);
    }

    @ParametersAreNonnullByDefault
    public static void setData(Location loc, String key, String val) {
        var block = getBlock(loc);

        if (block == null) {
            Slimefun.logger().log(Level.WARNING, "The specifiy location {0} doesn't have block data!", LocationUtils.locationToString(loc));
        } else {
            block.setData(key, val);
        }
    }

    @ParametersAreNonnullByDefault
    public static void removeData(Location loc, String key) {
        getBlock(loc).removeData(key);
    }

    @ParametersAreNonnullByDefault
    @Nullable
    public static BlockMenu getMenu(Location loc) {
        var blockData = getBlock(loc);
        if (blockData == null) {
            return null;
        }

        if (!blockData.isDataLoaded()) {
            requestLoad(blockData);
            return null;
        }

        return blockData.getBlockMenu();
    }

    public static void requestLoad(SlimefunBlockData blockData) {
        if (blockData.isDataLoaded()) {
            return;
        }

        if (loadingData.contains(blockData)) {
            return;
        }

        synchronized (loadingData) {
            if (loadingData.contains(blockData)) {
                return;
            }
            loadingData.add(blockData);
        }

        Slimefun.getDatabaseManager().getBlockDataController().loadBlockDataAsync(
                blockData,
                new IAsyncReadCallback<>() {
                    @Override
                    public void onResult(SlimefunBlockData result) {
                        loadingData.remove(blockData);
                    }
                }
        );
    }

    public static void executeAfterLoad(SlimefunBlockData data, Runnable execute, boolean runOnMainThread) {
        if (data.isDataLoaded()) {
            execute.run();
            return;
        }

        Slimefun.getDatabaseManager().getBlockDataController().loadBlockDataAsync(
                data,
                new IAsyncReadCallback<>() {
                    @Override
                    public boolean runOnMainThread() {
                        return runOnMainThread;
                    }

                    @Override
                    public void onResult(SlimefunBlockData result) {
                        execute.run();
                    }
                }
        );
    }
}
