package com.than00ber.renourisheddelight.food;

public enum EatingOutcome {
    // takes a free slot
    CONSUME,
    // tops up the slot already holding the same item
    REPLENISH,
    // swaps out the one active food sharing a restriction with it
    REPLACE_RESTRICTED,
    // every slot is full, swaps out the food closest to running out
    REPLACE_LOW,
    // shares a restriction with an active food that cannot be swapped out
    RESTRICTED,
    // every slot is full and replacing is disabled
    TOO_FULL;

    public boolean isAllowed() {
        return this != RESTRICTED && this != TOO_FULL;
    }
}
