package com.than00ber.renourisheddelight.config.data;

public final class DurationMultiplierEntry {

    public String attribute;
    public double multiplier;

    @SuppressWarnings("unused")
    public DurationMultiplierEntry() {
        // needed for persisted config
    }

    public DurationMultiplierEntry(String attribute, double multiplier) {
        this.attribute = attribute;
        this.multiplier = multiplier;
    }
}
