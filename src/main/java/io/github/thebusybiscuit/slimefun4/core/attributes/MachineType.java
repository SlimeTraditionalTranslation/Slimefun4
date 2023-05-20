package io.github.thebusybiscuit.slimefun4.core.attributes;

import javax.annotation.Nonnull;

public enum MachineType {

    CAPACITOR("電容"),
    GENERATOR("發電機"),
    MACHINE("機器");

    private final String suffix;

    MachineType(@Nonnull String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return suffix;
    }

}
