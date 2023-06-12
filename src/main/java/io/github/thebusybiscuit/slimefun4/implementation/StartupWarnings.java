package io.github.thebusybiscuit.slimefun4.implementation;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.ParametersAreNonnullByDefault;

import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;

/**
 * This class stores some startup warnings we occasionally need to print.
 * If you setup your server the recommended way, you are never going to see
 * any of these messages.
 * 
 * @author TheBusyBiscuit
 *
 */
final class StartupWarnings {

    private static final String BORDER = "****************************************************";
    private static final String PREFIX = "* ";

    private StartupWarnings() {}

    @ParametersAreNonnullByDefault
    static void discourageCSCoreLib(Logger logger) {
        logger.log(Level.SEVERE, BORDER);
        logger.log(Level.SEVERE, PREFIX + "看起來你仍在使用 CS-CoreLib。");
        logger.log(Level.SEVERE, PREFIX);
        logger.log(Level.SEVERE, PREFIX + "從 2021 年 1 月 30 起，");
        logger.log(Level.SEVERE, PREFIX + "Slimefun 不再需要安裝 CS-CoreLib");
        logger.log(Level.SEVERE, PREFIX + "你必須移除 CS-CoreLib 來執行 Slimefun。");
        logger.log(Level.SEVERE, BORDER);
    }

    @ParametersAreNonnullByDefault
    static void invalidMinecraftVersion(Logger logger, int majorVersion, String slimefunVersion) {
        logger.log(Level.SEVERE, BORDER);
        logger.log(Level.SEVERE, PREFIX + "Slimefun 並未被正確安裝！");
        logger.log(Level.SEVERE, PREFIX + "你正在使用不支援的 Minecraft 版本！");
        logger.log(Level.SEVERE, PREFIX);
        logger.log(Level.SEVERE, PREFIX + "你正在使用 Minecraft 1.{0}.x", majorVersion);
        logger.log(Level.SEVERE, PREFIX + "但 Slimefun {0} 只支援", slimefunVersion);
        logger.log(Level.SEVERE, PREFIX + "Minecraft {0}", String.join(" / ", Slimefun.getSupportedVersions()));
        logger.log(Level.SEVERE, BORDER);
    }

    @ParametersAreNonnullByDefault
    static void invalidServerSoftware(Logger logger) {
        logger.log(Level.SEVERE, BORDER);
        logger.log(Level.SEVERE, PREFIX + "Slimefun 並未被正確安裝！");
        logger.log(Level.SEVERE, PREFIX + "CraftBukkit 已不再受支援！");
        logger.log(Level.SEVERE, PREFIX);
        logger.log(Level.SEVERE, PREFIX + "Slimefun 需要你使用 Spigot、Paper 或");
        logger.log(Level.SEVERE, PREFIX + "任何 Spigot 或 Paper 的受支援分支。");
        logger.log(Level.SEVERE, PREFIX + "（我們推薦 Paper）");
        logger.log(Level.SEVERE, BORDER);
    }

    @ParametersAreNonnullByDefault
    static void oldJavaVersion(Logger logger, int recommendedJavaVersion) {
        int javaVersion = NumberUtils.getJavaVersion();

        logger.log(Level.WARNING, BORDER);
        logger.log(Level.WARNING, PREFIX + "你的 Java 版本（Java {0}）已過時。", javaVersion);
        logger.log(Level.WARNING, PREFIX);
        logger.log(Level.WARNING, PREFIX + "我們推薦你升級到 Java {0}。", recommendedJavaVersion);
        logger.log(Level.WARNING, PREFIX + "在新版本 Minecraft 中將需要使用 Java {0}", recommendedJavaVersion);
        logger.log(Level.WARNING, PREFIX + "我們希望能盡快利用");
        logger.log(Level.WARNING, PREFIX + "它所帶來的新功能。");
        logger.log(Level.WARNING, PREFIX + "在可預見的未來，Slimefun 也將需要 Java {0}", recommendedJavaVersion);
        logger.log(Level.WARNING, PREFIX + "所以請更新！");
        logger.log(Level.WARNING, BORDER);
    }

}
