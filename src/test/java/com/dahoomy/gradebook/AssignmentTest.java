package com.dahoomy.gradebook;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Assignment validation.
 * We check that bad inputs throw clear exceptions.
 */
public class AssignmentTest {

    @Test
    void constructor_acceptsValidValue() {
        Assignment a = new Assignment("Quiz1", 18, 20, 10);
        assertEquals("Quiz1", a.getName());
        assertEquals(18, a.getEarned(), 1e-9);
        assertEquals(20, a.getMax(), 1e-9);
        assertEquals(10, a.getWeightPercent(), 1e-9);
    }

    @Test
    void constructor_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Assignment("  ", 10, 10, 5));
    }

    @Test
    void constructor_rejectsMaxNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new Assignment("X", 5, 0, 5));
    }

    @Test
    void constructor_rejectsEarnedOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> new Assignment("X", -1, 10, 5));
        assertThrows(IllegalArgumentException.class, () -> new Assignment("X", 11, 10, 5));
    }

    @Test
    void constructor_rejectsWeightNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new Assignment("X", 5, 10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Assignment("X", 5, 10, -3));
    }
}
