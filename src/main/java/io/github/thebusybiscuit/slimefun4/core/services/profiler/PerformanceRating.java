package io.github.thebusybiscuit.slimefun4.core.services.profiler;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * This enum is used to quantify Slimefun's performance impact. This way we can assign a
 * "grade" to each timings report and also use this for metrics collection.
 *
 * @author TheBusyBiscuit
 *
 * @see SlimefunProfiler
 *
 */
public enum PerformanceRating implements Predicate<Float> {

    // Thresholds might change in the future!

    UNKNOWN(ChatColor.WHITE, -1, "未知"),

    GOOD(ChatColor.DARK_GREEN, 10, "良好"),
    FINE(ChatColor.DARK_GREEN, 20, "普通"),
    OKAY(ChatColor.GREEN, 30, "尚可"),
    MODERATE(ChatColor.YELLOW, 55, "中等"),
    SEVERE(ChatColor.RED, 85, "嚴重"),
    HURTFUL(ChatColor.DARK_RED, 500, "有害"),
    BAD(ChatColor.DARK_RED, Float.MAX_VALUE, "糟糕");

    private final ChatColor color;
    private final float threshold;

    private final String displayname;

    PerformanceRating(@Nonnull ChatColor color, float threshold, String displayname) {
        Validate.notNull(color, "Color cannot be null");
        this.color = color;
        this.threshold = threshold;
        this.displayname = displayname;
    }

    @Override
    public boolean test(@Nullable Float value) {
        if (value == null) {
            // null will only test true for UNKNOWN
            return threshold < 0;
        }

        return value <= threshold;
    }

    @Nonnull
    public ChatColor getColor() {
        return color;
    }

    @Nonnull
    public String getDisplayname() {
        return displayname;
    }

}