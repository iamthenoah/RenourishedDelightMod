package com.than00ber.renourisheddelight.food;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public enum EatingOutcome {
    SUCCESS(true, null),
    TOO_MANY(false, "message.eating_too_many"),
    NOT_BALANCED(false, "message.eating_not_balanced");

    final boolean success;
    final @Nullable String key;

    EatingOutcome(boolean success, @Nullable String key) {
        this.success = success;
        this.key = key;
    }

    public boolean isSuccess() {
        return success;
    }

    public Component message() {
        return Component.translatable(key != null ? key : "");
    }
}
