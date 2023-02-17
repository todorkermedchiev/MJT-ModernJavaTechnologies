package bg.sofia.uni.fmi.mjt.grading.simulator.grader;

import bg.sofia.uni.fmi.mjt.grading.simulator.Assistant;
import bg.sofia.uni.fmi.mjt.grading.simulator.Student;
import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.Assignment;
import bg.sofia.uni.fmi.mjt.grading.simulator.assignment.AssignmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CodePostGraderTest {
    private CodePostGrader grader;

    @BeforeEach
    void initializeGrader() {
        grader = new CodePostGrader(5);
    }

    @Test
    void testCreateGraderNegativeAssistantsCount() {
        assertThrows(IllegalArgumentException.class, () -> new CodePostGrader(-5),
                "The number of assistants must be positive - expected IllegalArgumentException");

        grader.finalizeGrading();
    }

    @Test
    void testSubmitAssignmentNullAssignment() {
        assertThrows(IllegalArgumentException.class, () -> grader.submitAssignment(null),
                "The submitted assignment cannot be null - expected IllegalArgumentException");

        grader.finalizeGrading();
    }

    @Test
    void testSubmitAssignmentValidAssignment() throws InterruptedException {
        Assignment expected = new Assignment(2, "gosho", AssignmentType.LAB);

        synchronized (grader) {
            grader.submitAssignment(expected);

            assertEquals(expected, grader.getAssignment(), "Assignment not properly submitted");
        }

        grader.finalizeGrading();
    }

    @Test
    void testGetSubmittedAssignmentsCount() throws InterruptedException {
        grader.submitAssignment(new Assignment(1, "gosho", AssignmentType.LAB));
        grader.submitAssignment(new Assignment(2, "kiro", AssignmentType.HOMEWORK));
        grader.submitAssignment(new Assignment(3, "maria", AssignmentType.PLAYGROUND));
        grader.submitAssignment(new Assignment(4, "ivan", AssignmentType.PROJECT));
        grader.submitAssignment(new Assignment(5, "petar", AssignmentType.LAB));

        assertEquals(5, grader.getSubmittedAssignmentsCount(), "There must be 5 submitted assignments," +
                "but they are " + grader.getSubmittedAssignmentsCount());

        grader.finalizeGrading();
    }

    @Test
    void testAllAssignmentsAreSubmitted() throws InterruptedException {
        final int STUDENTS_COUNT = 30;
        Thread[] students = new Thread[STUDENTS_COUNT];
        for (int i = 0; i < STUDENTS_COUNT; ++i) {
            students[i] = new Thread(new Student(63000 + i, "Student " + i, grader));
            students[i].start();
        }

        Thread.sleep(1000);
        grader.finalizeGrading();

        assertEquals(STUDENTS_COUNT, grader.getSubmittedAssignmentsCount(),
                "Every student must submit 1 assignment - expected " + STUDENTS_COUNT +
                " assignments, but they are " + grader.getSubmittedAssignmentsCount());
    }

    @Test
    void testAllSubmittedAssignmentsAreReviewed() throws InterruptedException {
        Thread[] students = new Thread[30];
        for (int i = 0; i < 30; ++i) {
            students[i] = new Thread(new Student(63000 + i, "Student " + i, grader));
            students[i].start();
        }

        Thread.sleep(500);
        grader.finalizeGrading();
        Thread.sleep(500);

        int submittedAssignmentsCount = grader.getSubmittedAssignmentsCount();

        int reviewedAssignmentsCount = 0;
        List<Assistant> assistants = grader.getAssistants();
        for (Assistant assistant : assistants) {
            reviewedAssignmentsCount += assistant.getNumberOfGradedAssignments();
        }

        assertEquals(submittedAssignmentsCount, reviewedAssignmentsCount, submittedAssignmentsCount +
                " assignments must be reviewed, but " + reviewedAssignmentsCount + " was reviewed");
    }
}
