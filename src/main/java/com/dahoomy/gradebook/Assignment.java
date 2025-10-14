package com.dahoomy.gradebook;

/**
 * A single graded item.
 * - name: label shown to you (e.g., "Quiz1")
 * - earned: points you scored
 * - max: total possible points
 * - weightPercent: contribution to the course grade (e.g., 10 means 10%)
 *
 * We use a Java "record" to keep it short and immutable.
 * The compact constructor below does sanity checks.
 */
public record Assignment(String name, double earned, double max, double weightPercent) {
    public Assignment {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name blank");
        if (max <= 0) throw new IllegalArgumentException("max <= 0");
        if (earned < 0 || earned > max) throw new IllegalArgumentException("earned out of range");
        if (weightPercent <= 0) throw new IllegalArgumentException("weight <= 0");
    }
}
