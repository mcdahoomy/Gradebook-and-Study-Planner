package com.dahoomy.gradebook;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

/**
 * Gradebook (console) — now with:
 *  1) Edit/delete assignment commands + a summary table for each course
 *  2) Simple study planner (tasks with due dates) saved in the same CSV
 *
 * NEW COMMANDS (gradebook):
 *   edit-assign <code> <index> name|earned|max|weight <newValue>
 *   del-assign  <code> <index>
 *   set-weight  <code> <index> <newWeight>
 *   course      <code>     // pretty table for one course
 *
 * NEW COMMANDS (study planner):
 *   plan-add    <code> <title> <YYYY-MM-DD>         // add a task for a course
 *   plan-list   [code]                              // list tasks (optionally filter by course)
 *   plan-done   <code> <index>                      // mark a task done
 *   plan-del    <code> <index>                      // delete a task
 *
 * CSV format now has 3 row types:
 *   COURSE,code,name,credits
 *   ASSIGN,courseCode,name,earned,max,weight
 *   TASK,  courseCode,title,dueISO,status
 *
 * NOTE: Keep commas out of names/titles to keep parsing simple.
 */
public class Main {

    // Our "database": course code -> Course object
    private final Map<String, Course> courses = new HashMap<>();

    public static void main(String[] args) {
        new Main().run();
    }

    /**
     * Runs a simple REPL (read–eval–print loop).
     * We read a line, decide which command it is, and call a helper method.
     */
    private void run() {
        System.out.println("Gradebook CLI — type 'help' to begin.");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // Split into command + the rest (arguments)
                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase(Locale.ROOT);
                String args;
                if (parts.length > 1) {
                    args = parts[1];
                } else {
                    args = "";
                }

                switch (cmd) {
                    case "help" -> printHelp();

                    // gradebook basics
                    case "add-course"  -> addCourseCmd(args);
                    case "set-credits" -> setCreditsCmd(args);
                    case "list-courses"-> listCoursesCmd();
                    case "add-assign"  -> addAssignmentCmd(args);
                    case "list-assign" -> listAssignmentsCmd(args);
                    case "grade"       -> gradeCmd(args);
                    case "gpa"         -> gpaCmd();

                    // gradebook upgrades
                    case "edit-assign" -> editAssignmentCmd(args);
                    case "del-assign"  -> deleteAssignmentCmd(args);
                    case "set-weight"  -> setWeightCmd(args);
                    case "course"      -> courseSummaryCmd(args);

                    // study planner
                    case "plan-add"    -> planAddCmd(args);
                    case "plan-list"   -> planListCmd(args);
                    case "plan-done"   -> planDoneCmd(args);
                    case "plan-del"   -> planDelCmd(args);

                    // persistence
                    case "save" -> saveCmd(args);
                    case "load" -> loadCmd(args);

                    case "exit", "quit" -> { System.out.println("Bye!"); return; }
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            }
        }
    }

    /** Prints a short command reference. Keep this up to date as you add features. */
    private void printHelp() {
        System.out.println("""
                Commands:
                  add-course <code> <name>                                      — add a course (e.g., CMPS251 Programming Concepts)
                  set-credits <code> <credits>                                  — set course credits (e.g., 3)
                  list-courses                                                  — list all courses
                  add-assign <code> <name> <e>/<m> <w>                          — add assignment: earned/max weight%
                                                                                e.g., add-assign CMPS251 Quiz1 18/20 10
                  list-assign <code>                                            — list assignments of a course
                  edit-assign <code> <idx> name|earned|max|weight <newValue>    — edit assignment
                  del-assign <code> <idx>                                       — delete assignment
                  set-weight <code> <idx> <newWeight>                           — edit weight
                  course <code>                                                 — show course in a pretty table
                  grade <code>                                                  — show weighted grade for course
                  gpa                                                           — compute GPA across all courses with credits
                  plan-add <code> <title> <YYYY-MM-DD>                          — add a task for a course
                  plan-list [code]                                              — list tasks (optionally filter by course)
                  plan-done <code> <idx>                                        — mark a task done
                  plan-del <code> <idx                                          — delete a task
                  save <file.csv>                                               — saves info to file
                  load <file.csv>                                               — loads info from file
                  exit                                                          — quit
                """);
    }

    // ---------------------------
    // Course-related commands
    // ---------------------------

    /**
     * add-course <code> <name>
     * Example: add-course CMPS251 Programming_Concepts
     * Rule: code must be unique.
     */
    private void addCourseCmd(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Usage: add-course <code> <name>");
            return;
        }
        String code = parts[0];
        String name = parts[1];
        if (courses.containsKey(code)) {
            System.out.println("Course already exists.");
            return;
        }

        courses.put(code, new Course(code, name));
        System.out.println("Added: " + code + " — " + name);
    }

    /**
     * set-credits <code> <credits>
     * Stores how many credit hours a course is worth (needed for GPA).
     */
    private void setCreditsCmd(String args) {
        String[] parts = args.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Usage: set-credits <code> <credits>");
            return;
        }
        Course c = courses.get(parts[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        try {
            int credits = Integer.parseInt(parts[1]);
            if (credits <= 0) {
                System.out.println("Credits must be positive.");
                return;
            }
            c.setCredits(credits);
            System.out.println("Set " + c.getCode() + " credits to " + credits);
        } catch (NumberFormatException e) {
            System.out.println("Credits must be an integer.");
        }
    }

    /** Lists all courses with current credit hours and the total weight added so far. */
    private void listCoursesCmd() {
        if (courses.isEmpty()) {
            System.out.println("No courses yet.");
            return;
        }

        for (String code : courses.keySet().stream().sorted().toList()) {
            Course c = courses.get(code);
            System.out.printf(
                    "%s — %s [credits=%d, weight%% total=%.2f]\n",
                    c.getCode(), c.getName(), c.getCredits(), c.totalWeight());
        }
    }

    // ---------------------------
    // Assignment-related commands
    // ---------------------------

    /**
     * add-assign <code> <name> <earned>/<max> <weight%>
     * Example: add-assign CMPS251 Quiz1 18/20 10
     *
     * Notes:
     * - weight% is the portion of the course grade this item contributes.
     * - For a valid final grade, the sum of weights in a course must be 100%.
     * - We validate numbers and ranges (e.g., earned cannot exceed max).
     */
    private void addAssignmentCmd(String args) {
        String[] p = args.split("\\s+", 4);
        if (p.length < 4) {
            System.out.println("Usage: add-assign <code> <name> <earned>/<max> <weight%>");
            return;
        }

        String code = p[0];
        String name = p[1];
        String ratio = p[2];      // "earned/max" (18/20)
        String weightStr = p[3];  // "10" (for 10%)

        Course c = courses.get(code);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }

        // Parse earned/max
        if (!ratio.contains("/")) {
            System.out.println("Use <earned>/<max> like 18/20.");
            return;
        }
        String[] em = ratio.split("/");
        double earned, max;
        try {
            earned = Double.parseDouble(em[0]);
            max = Double.parseDouble(em[1]);
            if (max <= 0) {
                System.out.println("Max points must be > 0.");
                return;
            }
            if (earned < 0) {
                System.out.println("Earned must be between 0 and max.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid earned/max numbers.");
            return;
        }

        // Parse weight%
        double weight;
        try {
            weight = Double.parseDouble(weightStr);
            if (weight <= 0) {
                System.out.println("Weight% must be > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid weight%.");
            return;
        }

        // Prevent going over 100%
        double newTotal = c.totalWeight() + weight;
        if (newTotal > 100.00001) {
            System.out.printf("Adding this would exceed 100%% (current %.2f%%).%n", c.totalWeight());
            return;
        }

        c.addAssignment(new Assignment(name, earned, max, weight));
        System.out.printf("Added assignment '%s' to %s (%.2f/%.2f, weight %.2f%%).%n",
                name, c.getCode(), earned, max, weight);
        if (Math.abs(newTotal - 100.0) < 1e-6) {
            System.out.println("Heads up: weights now total 100% for this course.");
        } else {
            System.out.printf("weights total: %.2f%%%n", newTotal);
        }
    }

    /** Shows all assignments for a course in a quick, readable list. */
    private void listAssignmentsCmd(String args) {
        if (args.isBlank()) {
            System.out.println("Usage: list-assign <code>");
            return;
        }
        Course c = courses.get(args.split("\\s+")[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        if (c.assignments().isEmpty()) {
            System.out.println("No assignments yet.");
            return;
        }
        int i = 1;
        for (Assignment a : c.assignments()) {
            System.out.printf("%d) %s  earned/max=%.2f/%.2f  weight=%.2f%%%n",
                    i++, a.getName(), a.getEarned(), a.getMax(), a.getWeightPercent());
        }
        System.out.printf("Weight total: %.2f%%%n", c.totalWeight());
    }

    private void editAssignmentCmd(String args) {
        // edit-assign <code> <index> name|earned|max|weight <newValue>
        String[] p = args.split("\\s+", 4);
        if (p.length < 4) {
            System.out.println("Usage: edit-assign <code> <index> name|earned|max|weight <newValue>");
            return;
        }
        String code = p[0];
        Integer idx = tryParseInt(p[1]);
        String field = p[2].toLowerCase();
        String newVal = p[3];

        Course c = courses.get(code);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        if (idx == null || idx < 1 || idx > c.getAssignmentsCount()) {
            System.out.println("Index out of range.");
            return;
        }
        Assignment a = c.getAssignment(idx - 1);

        switch(field) {
            case "name" -> {
                if (newVal.isBlank()) {
                    System.out.println("Name cannot be blank.");
                    return;
                }
                a.setName(newVal);
                System.out.println("Updated name.");
            }
            case "earned" -> {
                Double v = tryParseDouble(newVal);
                if (v == null || v < 0 || v > a.getMax()) {
                    System.out.println("Invalid earned value.");
                    return;
                }
                a.setEarned(v);
                System.out.println("Updated earned.");
            }
            case "max" -> {
                Double v = tryParseDouble(newVal);
                if (v == null || v <= 0 || a.getEarned() > v) {
                    System.out.println("Invalid max value.");
                    return;
                }
                a.setMax(v);
                System.out.println("Updated max.");
            }
            case "weight" -> {
                Double v = tryParseDouble(newVal);
                if (v == null || v <= 0) {
                    System.out.println("Weight must be > 0.");
                    return;
                }
                // Check the new total will not exceed 100%
                double currentTotal = c.totalWeight() - a.getWeightPercent(); // remove old weight
                if (currentTotal + v > 100.00001) {
                    System.out.println("Changing weight would exceed 100% (current " + fmt2(currentTotal) + "%).");
                    return;
                }
                a.setWeightPercent(v);
                System.out.println("Updated weight. New total: " + fmt2(currentTotal + v) + "%");
            }
            default -> System.out.println("Unknown field. Use: name | earned | max | weight");
        }
    }

    private void deleteAssignmentCmd(String args) {
        // del-assign <code> <index>
        String[] p = args.split("\\s+");

    }

    /**
     * grade <code>
     * Prints the final percentage, letter, and points for a course.
     * Requires that the course's weights sum to 100%.
     */
    private void gradeCmd(String args) {
        if (args.isBlank()) {
            System.out.println("Usage: grade <code>");
            return;
        }
        Course c = courses.get(args.split("\\s+")[0]);
        if (c == null) {
            System.out.println("No such course.");
            return;
        }
        if (Math.abs(c.totalWeight() - 100.0) > 1e-6) {
            System.out.printf("Weights must total 100%% to compute final grade (current %.2f%%).%n", c.totalWeight());
            return;
        }

        double pct = c.weightedPercentage();  // e.g., 83.5
        System.out.printf("%s - %s%nFinal: %.2f%%%n", c.getCode(), c.getCode(), pct);
        System.out.printf("Letter: %s (4.0 scale %.2f)%n",
                GpaScale.letterFor(pct), GpaScale.pointsFor(pct));
    }

    /**
     * gpa
     * Computes GPA across all courses that:
     *   - have credits set (>0), and
     *   - have weights totaling 100% (so we know the final grade).
     */
    private void gpaCmd() {
        // GPA uses courses that have credits > 0 AND whose weights sum to 100%
        double totalPointsTimesCredits = 0.0;
        int totalCredits = 0;

        for (Course c : courses.values()) {
            if (c.getCredits() <= 0) continue;
            if (Math.abs(c.totalWeight() - 100.0) > 1e-6) continue;

            double pct = c.weightedPercentage();
            double pts = GpaScale.pointsFor(pct);
            totalPointsTimesCredits += pts * c.getCredits();
            totalCredits += c.getCredits();
        }

        if (totalCredits == 0) {
            System.out.println("No GPA yet. Ensure courses have credits and weights sum to 100%.");
            return;
        }

        double gpa = totalPointsTimesCredits / totalCredits;
        System.out.printf("GPA across %d credits: %.3f%n", totalCredits, gpa);
    }

    private void saveCmd(String args) {
        if (args.isBlank()) {
            System.out.println("Usage: save <file.csv>");
            return;
        }
        String fileName = args.trim();

        try {
            List<String> lines = new ArrayList<>();
            lines.add("# Gradebook CSV v1");

            // Write all courses first (sorted by code)
            List<String> codes = new ArrayList<>(courses.keySet());
            Collections.sort(codes);
            for (String code : codes) {
                Course c = courses.get(code);
                // COURSE,code,name,credits
                lines.add("COURSE," + c.getCode() + "," + c.getName() + "," + c.getCredits());
            }

            // Then all assignment, grouped by course (also sorted)
            for (String code : codes) {
                Course c = courses.get(code);
                for (int i = 0; i < c.getAssignmentsCount(); i++) {
                    Assignment a = c.getAssignment(i);
                    // ASSIGN,courseCode,name,earned,max,weight
                    lines.add("ASSIGN," + c.getCode() + "," + a.getName() + ","
                              + trimZeros(a.getEarned()) + "," + trimZeros(a.getMax()) + ","
                              + trimZeros(a.getWeightPercent()));
                }
            }

            Files.write(Path.of(fileName), lines, StandardCharsets.UTF_8);
            System.out.println("Saved to: " + fileName);
        } catch (Exception e) {
            System.out.println("Failed to save: " + e.getMessage());
        }
    }

    // load data from a CSV file
    private void loadCmd(String args) {
        if (args.isBlank()) {
            System.out.println("Usage: load <file.csv>");
            return;
        }
        String fileName = args.trim();

        try {
            List<String> lines = Files.readAllLines(Path.of(fileName), StandardCharsets.UTF_8);
            // Temporary holders while we rebuild
            Map<String, Course> newCourses = new HashMap<>();
            List<String[]> pendingAssigns = new ArrayList<>();

            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("#")) continue; // allow comments/header

                String[] parts = line.split(",", -1); // keep empty fields if any
                String kind = parts[0].trim().toUpperCase();

                if (kind.equals("COURSE")) {
                    // Expect: COURSE,code,name,credits
                    if (parts.length != 4) {
                        System.out.println("Skipping bad COURSE row: " + line);
                        continue;
                    }
                    String code = parts[1].trim();
                    String name = parts[2].trim();
                    Integer credits = tryParseInt(parts[3].trim());
                    if (code.isEmpty() || name.isEmpty() || credits == null || credits < 0) {
                        System.out.println("Skipping invalid COURSE row: " + line);
                        continue;
                    }
                    newCourses.put(code, new Course(code, name));
                    newCourses.get(code).setCredits(credits);
                }
                else if (kind.equals("ASSIGN")) {
                    // Expect: ASSIGN,courseCode,name,earned,max,weight
                    if (parts.length != 6) {
                        System.out.println("Skipping bad ASSIGN row: " + line);
                        continue;
                    }
                    pendingAssigns.add(parts);
                }
                else {
                    System.out.println("Skipping unknown row: " + line);
                }
            }

            // Now apply all assignments
            for (String[] p : pendingAssigns) {
                String courseCode = p[1].trim();
                String name = p[2].trim();
                Double earned = tryParseDouble(p[3].trim());
                Double max = tryParseDouble(p[4].trim());
                Double weight = tryParseDouble(p[5].trim());

                Course c = newCourses.get(courseCode);
                if (c == null) {
                    System.out.println("Skipping ASSIGN for missing course " + courseCode + ": " + name);
                    continue;
                }
                if (earned == null || max == null || weight == null ||
                        max <= 0 || earned < 0 || earned > max || weight <= 0) {
                    System.out.println("Skipping invalid ASSIGN: " + name);
                    continue;
                }
                // This will throw if invalid, which is fine: we catch and report.
                try {
                    c.addAssignment(new Assignment(name, earned, max, weight));
                } catch (IllegalArgumentException ex) {
                    System.out.println("Skipping ASSIGN (invalid values): " + name + " (" + ex.getMessage() + ")");
                }
            }

            // Replace current data
            courses.clear();
            courses.putAll(newCourses);
            System.out.println("Loaded from: " + fileName + "  (courses: " + courses.size() + ")");
        } catch (Exception e) {
            System.out.println("Failed to load: " + e.getMessage());
        }
    }

    // ---------------------------
    // Small helpers
    // ---------------------------

    private Integer tryParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }
    private Double tryParseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private String fmt2(double d) { return String.format(Locale.ROOT, "%.2f", d); }

    /** Format doubles like 10 or 10.5 (no trailing .0s in CSV). */
    private String trimZeros(double d) {
        String s = String.format(java.util.Locale.ROOT, "%.6f", d);
        // remove trailing zeros and dot
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            if (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s.isEmpty() ? "0" : s;
    }
}
