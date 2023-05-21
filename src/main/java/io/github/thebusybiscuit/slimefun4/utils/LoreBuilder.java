package io.github.thebusybiscuit.slimefun4.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.annotation.Nonnull;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineTier;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactivity;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;

/**
 * This utility class provides a few handy methods and constants to build the lore of any
 * {@link SlimefunItemStack}. It is mostly used directly inside the class {@link SlimefunItems}.
 *
 * @author TheBusyBiscuit
 * 
 * @see SlimefunItems
 *
 */
public final class LoreBuilder {

    public static final String HAZMAT_SUIT_REQUIRED = "&8\u21E8 &4需要穿上防護套裝！";
    public static final String RIGHT_CLICK_TO_USE = "&e右鍵點擊&7 使用";
    public static final String RIGHT_CLICK_TO_OPEN = "&e右鍵點擊&7 打開";
    public static final String CROUCH_TO_USE = "&e蹲下&7 使用";
    private static final DecimalFormat hungerFormat = new DecimalFormat("#.0", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private LoreBuilder() {}

    public static @Nonnull String radioactive(@Nonnull Radioactivity radioactivity) {
        return radioactivity.getLore();
    }

    public static @Nonnull String machine(@Nonnull MachineTier tier, @Nonnull MachineType type) {
        return tier + " " + type;
    }

    public static @Nonnull String speed(float speed) {
        return "&8\u21E8 &b\u26A1 &7速度：&b" + speed + 'x';
    }

    public static @Nonnull String powerBuffer(int power) {
        return power(power, " 緩衝");
    }

    public static @Nonnull String powerPerSecond(int power) {
        return power(power, "/s");
    }

    public static @Nonnull String power(int power, @Nonnull String suffix) {
        return "&8\u21E8 &e\u26A1 &7" + power + " J" + suffix;
    }

    public static @Nonnull String powerCharged(int charge, int capacity) {
        return "&8\u21E8 &e\u26A1 &7" + charge + " / " + capacity + " J";
    }

    public static @Nonnull String material(String material) {
        return "&8\u21E8 &7材料：&b" + material;
    }

    public static @Nonnull String hunger(double value) {
        return "&7&o恢复 &b&o" + hungerFormat.format(value) + " &7&o飽食度";
    }

    public static @Nonnull String range(int blocks) {
        return "&7範圍：&c" + blocks + " 格方塊";
    }

    public static @Nonnull String usesLeft(int usesLeft) {
        return "&7剩餘次數 &e" + usesLeft + " 次";
    }

    public static @Nonnull String thrust(float thrust) {
        return "&8\u21E8 &7推力：&c" + thrust;
    }

    public static @Nonnull String accuracy(int accuracy) {
        String color;

        if (accuracy >= 80) {
            color = "&c";
        } else if (accuracy >= 75) {
            color = "&a";
        } else if (accuracy >= 70) {
            color = "&e";
        } else if (accuracy >= 60) {
            color = "&6";
        } else {
            color = "&c";
        }

        return "&8\u21E8 &7穩定性：" + color + accuracy + "%";
    }

    public static @Nonnull String accuracy(float accuracy) {
        return "&8\u21E8 &7穩定性：&c" + accuracy + "%";
    }

}
