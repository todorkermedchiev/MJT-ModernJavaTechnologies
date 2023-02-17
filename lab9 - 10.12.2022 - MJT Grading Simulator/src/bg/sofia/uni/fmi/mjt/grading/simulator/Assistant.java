package bg.sofia.uni.fmi.mjt.grading.simulator;

import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.Assignment;
import bg.sofia.uni.fmi.mjt.grading.simulator.grader.AdminGradingAPI;

public class Assistant extends Thread {
    private final AdminGradingAPI grader;
    private int reviewsCount;

    public Assistant(String name, AdminGradingAPI grader) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null, empty or blank");
        }
        if (grader == null) {
            throw new IllegalArgumentException("StudentGradingAPI cannot be null");
        }

        setName(name);
        this.grader = grader;
        this.reviewsCount = 0;
    }

    private void reviewAssignment() {
        Assignment toBeReviewed = grader.getAssignment();

        while (toBeReviewed != null) {
            try {
                Thread.sleep(toBeReviewed.type().getGradingTime());
                ++reviewsCount;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            toBeReviewed = grader.getAssignment();
        }
    }

    @Override
    public void run() {
        reviewAssignment();
    }

    public int getNumberOfGradedAssignments() {
        return reviewsCount;
    }

}