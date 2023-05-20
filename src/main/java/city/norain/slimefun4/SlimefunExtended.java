package city.norain.slimefun4;

import city.norain.slimefun4.listener.SlimefunMigrateListener;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.bukkit.Bukkit;

public final class SlimefunExtended {
    private static Logger logger = null;

    private static SlimefunMigrateListener migrateListener = new SlimefunMigrateListener();

    public static void register(@Nonnull Slimefun sf) {
        logger = sf.getLogger();
        if (checkHybridServer(sf)) {
            return;
        }

        scheduleSlimeGlueCheck(sf);
        VaultIntegration.register(sf);

        migrateListener.register(sf);
    }

    public static void shutdown() {
        logger = null;
        migrateListener = null;

        VaultIntegration.cleanup();
    }

    public static Logger getLogger() {
        return logger;
    }

    private static boolean checkHybridServer(@Nonnull Slimefun sf) {
        try {
            Class.forName("net/minecraftforge/common/MinecraftForge");
            logger.log(Level.WARNING, "偵測到正在使用混合伺服器端，Slimefun 將被停用！");
            Bukkit.getPluginManager().disablePlugin(sf);

            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void scheduleSlimeGlueCheck(Slimefun sf) {
        Bukkit.getScheduler().runTaskLater(sf, () -> {
            if (Bukkit.getPluginManager().getPlugin("SlimeGlue") == null) {
                logger.log(Level.WARNING, "偵測到沒有安裝 SlimeGlue（黏液膠），你將會缺少一些插件的額外保護檢查！");
                logger.log(Level.WARNING, "下載：https://github.com/Xzavier0722/SlimeGlue");
            }
        }, 300); // 15s
    }
}
