package com.than00ber.renourisheddelight.food;

public enum EatingOutcome {
    // takes a free slot
    CONSUME,
    // tops up the slot already holding the same item
    REPLENISH,
    // swaps out the one active food sharing a category with it
    REPLACE_CATEGORY,
    // every slot is full, swaps out the food closest to running out
    REPLACE_LOW,
    // shares a category with an active food that cannot be swapped out
    CATEGORY_CONFLICT,
    // every slot is full and replacing is disabled
    TOO_FULL;

    public boolean isAllowed() {
        return this != CATEGORY_CONFLICT && this != TOO_FULL;
    }
}
