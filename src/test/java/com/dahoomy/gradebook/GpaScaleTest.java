package com.dahoomy.gradebook;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Small checks for GPA scale mapping.
 * Adjust thresholds in GpaScale later if your university differs.
 */
public class GpaScaleTest {

    @Test
    void letterAndPoints_examples() {
        // Quick spot checks around boundaries
        assertEquals("A",  GpaScale.letterFor(95));
        assertEquals(4.0,  GpaScale.pointsFor(95), 1e-9);

        assertEquals("B+", GpaScale.letterFor(85));
        assertEquals(3.5,  GpaScale.pointsFor(85), 1e-9);

        assertEquals("B",  GpaScale.letterFor(80));
        assertEquals(3.0,  GpaScale.pointsFor(80), 1e-9);

        assertEquals("C+",  GpaScale.letterFor(75));
        assertEquals(2.5,  GpaScale.pointsFor(75), 1e-9);

        assertEquals("C",  GpaScale.letterFor(70));
        assertEquals(2.0,  GpaScale.pointsFor(70), 1e-9);

        assertEquals("D+",  GpaScale.letterFor(65));
        assertEquals(1.5,  GpaScale.pointsFor(65), 1e-9);

        assertEquals("D",  GpaScale.letterFor(60));
        assertEquals(1.0,  GpaScale.pointsFor(60), 1e-9);

        assertEquals("F",  GpaScale.letterFor(59.9));
        assertEquals(0.0,  GpaScale.pointsFor(59.9), 1e-9);
    }
}
