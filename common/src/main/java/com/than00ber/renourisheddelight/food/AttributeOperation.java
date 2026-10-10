package com.than00ber.renourisheddelight.food;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

// configs and presets use the 1.21 operation names so they stay portable between versions
public final class AttributeOperation {

    public static final String ADD_VALUE = "add_value";
    public static final String ADD_MULTIPLIED_BASE = "add_multiplied_base";
    public static final String ADD_MULTIPLIED_TOTAL = "add_multiplied_total";
    public static final List<String> NAMES = List.of(ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL);

    public static AttributeModifier.Operation parse(@Nullable String name) {
        String value = name != null ? name.trim().toLowerCase(Locale.ROOT) : "";
        return switch (value) {
            case ADD_MULTIPLIED_BASE -> AttributeModifier.Operation.MULTIPLY_BASE;
            case ADD_MULTIPLIED_TOTAL -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
    }

    public static boolean isValid(@Nullable String name) {
        return name != null && NAMES.contains(name.trim().toLowerCase(Locale.ROOT));
    }
}
