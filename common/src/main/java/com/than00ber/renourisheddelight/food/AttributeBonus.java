package com.than00ber.renourisheddelight.food;

public final class AttributeBonus {

    public String attribute;
    public String operation;
    public double amount;
    public int duration;

    @SuppressWarnings("unused")
    public AttributeBonus() {
        // needed for persisted config
    }

    public AttributeBonus(String attribute, String operation, double amount, int duration) {
        this.attribute = attribute;
        this.operation = operation;
        this.amount = amount;
        this.duration = duration;
    }
    
    public AttributeBonus copy() {
        return new AttributeBonus(attribute, operation, amount, duration);
    }
}