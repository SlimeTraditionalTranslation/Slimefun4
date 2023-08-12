package city.norain.slimefun4;

import city.norain.slimefun4.listener.SlimefunMigrateListener;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public final class SlimefunExtended {
    private static Logger logger = null;

    private static SlimefunMigrateListener migrateListener = new SlimefunMigrateListener();

    public static boolean checkEnvironment(@Nonnull Slimefun sf) {
        logger = sf.getLogger();

        if (EnvironmentChecker.checkHybridServer(sf, logger)) {
            return false;
        }

        return !EnvironmentChecker.checkIncompatiblePlugins(sf, logger);
    }

    public static void register(@Nonnull Slimefun sf) {
        logger = sf.getLogger();

        EnvironmentChecker.scheduleSlimeGlueCheck(sf, logger);

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
}
