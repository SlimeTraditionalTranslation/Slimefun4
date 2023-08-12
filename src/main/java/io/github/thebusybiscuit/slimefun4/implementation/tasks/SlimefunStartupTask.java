package io.github.thebusybiscuit.slimefun4.implementation.tasks;

import com.xzavier0722.mc.plugin.slimefun4.storage.listener.ChunkListener;
import com.xzavier0722.mc.plugin.slimefun4.storage.listener.WorldListener;
import com.xzavier0722.mc.plugin.slimefun4.storage.controller.ChunkDataLoadMode;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.listeners.TeleporterListener;
import io.github.thebusybiscuit.slimefun4.implementation.setup.PostSetup;
import org.bukkit.Bukkit;

import javax.annotation.Nonnull;

/**
 * This Task initializes all items, some listeners and various other stuff.
 * This has been moved to its own class to make timings log easier to read, so
 * they say "SlimefunStartupTask" instead of "Slimefun:lambda:123456789".
 * 
 * @author TheBusyBiscuit
 *
 */
public class SlimefunStartupTask implements Runnable {

    private final Slimefun plugin;
    private final Runnable runnable;

    /**
     * This initializes our {@link SlimefunStartupTask} for the given {@link Slimefun}.
     * 
     * @param plugin
     *            The main instance of our {@link Slimefun}
     * @param runnable
     *            A {@link Runnable} containing additional operations that need to be run
     */
    public SlimefunStartupTask(@Nonnull Slimefun plugin, @Nonnull Runnable runnable) {
        this.plugin = plugin;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();

        // Load all items
        PostSetup.loadItems();

        // Load all worlds
        Slimefun.getWorldSettingsService().load(Bukkit.getWorlds());

        // Load/Unload Worlds, only after all plugins have started up. Fixes #2862
        plugin.getServer().getPluginManager().registerEvents(new ChunkListener(), plugin);
        if (Slimefun.getDatabaseManager().getChunkDataLoadMode() == ChunkDataLoadMode.LOAD_ON_STARTUP) {
            plugin.getServer().getPluginManager().registerEvents(new WorldListener(), plugin);
        }

        // Only load this Listener if the corresponding items are enabled
        if (isEnabled("ELEVATOR_PLATE", "GPS_ACTIVATION_DEVICE_SHARED", "GPS_ACTIVATION_DEVICE_PERSONAL")) {
            new TeleporterListener(plugin);
        }
    }

    private boolean isEnabled(String... itemIds) {
        for (String id : itemIds) {
            SlimefunItem item = SlimefunItem.getById(id);

            if (item != null && !item.isDisabled()) {
                return true;
            }
        }

        return false;
    }

}
