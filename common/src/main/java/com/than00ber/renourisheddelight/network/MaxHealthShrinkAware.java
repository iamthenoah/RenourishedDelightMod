package com.than00ber.renourisheddelight.network;

public interface MaxHealthShrinkAware {

    void maxHealthHasShrunk();

    boolean consumeRecentMaxHealthShrink();
}
