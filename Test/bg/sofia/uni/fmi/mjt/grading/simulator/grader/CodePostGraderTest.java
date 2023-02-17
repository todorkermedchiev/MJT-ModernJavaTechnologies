package bg.sofia.uni.fmi.mjt.grading.simulator.grader;

import bg.sofia.uni.fmi.mjt.grading.simulator.Assistant;
import bg.sofia.uni.fmi.mjt.grading.simulator.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodePostGraderTest {
    @Test
    void testCodePostGrader() throws InterruptedException {
        CodePostGrader grader = new CodePostGrader(5);

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
