package city.norain.slimefun4.listener;

import com.xzavier0722.mc.plugin.slimefun4.storage.migrator.BlockStorageMigrator;
import com.xzavier0722.mc.plugin.slimefun4.storage.migrator.PlayerProfileMigrator;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import javax.annotation.Nonnull;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SlimefunMigrateListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        var p = e.getPlayer();

        if ((PlayerProfileMigrator.isOldDataExists() || BlockStorageMigrator.isOldDataExists()) && p.hasPermission("slimefun.command.migrate")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c偵測到舊型檔案式儲存資料，請使用 /sf migrate 轉移到新的資料庫中！"));
        }
    }

    public void register(@Nonnull Slimefun plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
