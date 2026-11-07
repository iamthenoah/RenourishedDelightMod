package com.than00ber.renourisheddelight.client.overlay;

import com.than00ber.renourisheddelight.config.ClientConfiguration;
import dev.architectury.platform.Platform;

import java.util.stream.Stream;

public final class HealthBarCompat {

    private static Boolean overridden;

    public static boolean isHealthBarOverridden() {
        if (overridden == null) {
            overridden = Stream // any mod that overrides the health bar disables goes here
                    .of("colorfulhearts", "partialhearts", "overflowingbars")
                    .anyMatch(Platform::isModLoaded);
        }
        return overridden;
    }

    public static boolean compactHealthBar() {
        return !isHealthBarOverridden() && ClientConfiguration.getInstance().compactHealthBar;
    }

    public static boolean clipOddMaxHealthHeart() {
        return !isHealthBarOverridden() && ClientConfiguration.getInstance().clipOddMaxHealthHeart;
    }
}
