package io.github.thebusybiscuit.slimefun4.core.attributes;

import javax.annotation.Nonnull;

public enum MachineTier {

    BASIC("&e基礎"),
    AVERAGE("&6普通"),
    MEDIUM("&a中等"),
    GOOD("&2優秀"),
    ADVANCED("&6高級"),
    END_GAME("&4終極");

    private final String prefix;

    MachineTier(@Nonnull String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return prefix;
    }

}
