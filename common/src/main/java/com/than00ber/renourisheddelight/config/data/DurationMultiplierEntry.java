package com.than00ber.renourisheddelight.config.data;

import com.than00ber.renourisheddelight.food.ConsumableFoodInstance;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    public DurationMultiplierEntry copy() {
        return new DurationMultiplierEntry(attribute, multiplier);
    }

    public static @Nullable DurationMultiplierEntry get(List<DurationMultiplierEntry> entries, String attributeId) {
        Holder<Attribute> attribute = ConsumableFoodInstance.resolveAttribute(attributeId);

        if (attribute != null) {
            for (DurationMultiplierEntry entry : entries) {
                Holder<Attribute> candidate = ConsumableFoodInstance.resolveAttribute(entry.attribute);

                if (candidate != null && candidate.value() == attribute.value()) {
                    return entry;
                }
            }
        }
        return null;
    }
}
