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
    private String name;
    private double earned;
    private double max;
    private double weightPercent;

    public Assignment(String name, double earned, double max, double weightPercent) {
        setName(name);
        setMax(max);
        setEarned(earned);        // must be after setMax
        setWeightPercent(weightPercent);
    }

    public String getName() { return name; }
    public double getEarned() { return earned; }
    public double getMax() { return max; }
    public double getWeightPercent() { return weightPercent; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name blank");
        this.name = name;
    }

    public void setEarned(double earned) {
        if (earned < 0 || earned > this.max) throw new IllegalArgumentException("earned out of range");
        this.earned = earned;
    }

    public void setMax(double max) {
        if (max <= 0) throw new IllegalArgumentException("max <= 0");
        // If lowering max below current earned, reject
        if (this.earned > 0 && this.earned > max) throw new IllegalArgumentException("max < current earned");
        this.max = max;
    }

    public void setWeightPercent(double weightPercent) {
        if (weightPercent <= 0) throw new IllegalArgumentException("weight <= 0");
        this.weightPercent = weightPercent;
    }
}
