package bg.sofia.uni.fmi.mjt.grading.simulator;

import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.Assignment;
import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.AssignmentType;
import bg.sofia.uni.fmi.mjt.grading.simulator.grader.StudentGradingAPI;

import java.util.Random;

public class Student implements Runnable {
    private static final int MAX_WAITING_TIME = 1_000;
    private final int fn;
    private final String name;
    private final StudentGradingAPI studentGradingAPI;

    public Student(int fn, String name, StudentGradingAPI studentGradingAPI) {
        if (fn < 0) {
            throw new IllegalArgumentException("Faculty number must be positive integer");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null, empty or blank");
        }
        if (studentGradingAPI == null) {
            throw new IllegalArgumentException("StudentGradingAPI cannot be null");
        }

        this.fn = fn;
        this.name = name;
        this.studentGradingAPI = studentGradingAPI;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(new Random().nextInt(MAX_WAITING_TIME));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assignment assignment = new Assignment(fn, name, AssignmentType.randomType());
        studentGradingAPI.submitAssignment(assignment);
    }

    public int getFn() {
        return fn;
    }

    public String getName() {
        return name;
    }

    public StudentGradingAPI getGrader() {
        return studentGradingAPI;
    }
}