package com.dahoomy.gradebook;

public record Assignment(String name, double earned, double max, double weightPercent) {
    public Assignment {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name blank");
        if (max <= 0) throw new IllegalArgumentException("max <= 0");
        if (earned < 0 || earned > max) throw new IllegalArgumentException("earned out of range");
        if (weightPercent <= 0) throw new IllegalArgumentException("weight <= 0");
    }
}
