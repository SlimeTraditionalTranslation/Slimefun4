package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.LocationUtils;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.ParametersAreNullableByDefault;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class SlimefunBlockData extends ASlimefunDataContainer {
    private final Location location;
    private final String sfId;
    private volatile BlockMenu menu;
    private volatile boolean pendingRemove = false;

    @ParametersAreNonnullByDefault
    SlimefunBlockData(Location location, String sfId) {
        super(LocationUtils.getLocKey(location));
        this.location = location;
        this.sfId = sfId;
    }

    @ParametersAreNonnullByDefault
    SlimefunBlockData(Location location, SlimefunBlockData other) {
        super(LocationUtils.getLocKey(location), other);
        this.location = location;
        this.sfId = other.sfId;
    }

    @Nonnull
    public Location getLocation() {
        return location;
    }

    @Nonnull
    public String getSfId() {
        return sfId;
    }

    @ParametersAreNonnullByDefault
    public void setData(String key, String val) {
        checkData();
        setCacheInternal(key, val, true);
        Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedBlockDataUpdate(this, key);
    }

    @ParametersAreNonnullByDefault
    public void removeData(String key) {
        if (removeCacheInternal(key) != null || !isDataLoaded()) {
            Slimefun.getDatabaseManager().getBlockDataController().scheduleDelayedBlockDataUpdate(this, key);
        }
    }

    @ParametersAreNullableByDefault
    void setBlockMenu(BlockMenu blockMenu) {
        menu = blockMenu;
    }

    @Nullable
    public BlockMenu getBlockMenu() {
        return menu;
    }

    @Nullable
    public ItemStack[] getMenuContents() {
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

    public void setPendingRemove(boolean pendingRemove) {
        this.pendingRemove = pendingRemove;
    }

    public boolean isPendingRemove() {
        return pendingRemove;
    }

    @Override
    public String toString() {
        return "SlimefunBlockData [sfId=" + sfId + ", location=" + location + ", isPendingRemove=" + pendingRemove + "]";
    }
}
