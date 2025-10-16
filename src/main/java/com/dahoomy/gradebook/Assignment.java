package com.dahoomy.gradebook;

/**
 * A single graded item.
 * - name: label shown to you (e.g., "Quiz1")
 * - earned: points you scored
 * - max: total possible points
 * - weightPercent: contribution to the course grade (e.g., 10 means 10%)
 * <p>
 * We use a Java "record" to keep it short and immutable.
 * The compact constructor below does sanity checks.
 */
public class Assignment {
    private final String name;
    private final double earned;
    private final double max;
    private final double weightPercent;

    public Assignment(String name, double earned, double max, double weightPercent) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name blank");
        }
        if (max <= 0) {
            throw new IllegalArgumentException("max <= 0");
        }
        if (earned < 0 || earned > max) {
            throw new IllegalArgumentException("earned out of range");
        }
        if (weightPercent <= 0) {
            throw new IllegalArgumentException("weight <= 0");
        }
        this.name = name;
        this.earned = earned;
        this.max = max;
        this.weightPercent = weightPercent;
    }

    public String getName() { return name; }
    public double getEarned() { return earned; }
    public double getMax() { return max; }
    public double getWeightPercent() { return weightPercent; }
}
