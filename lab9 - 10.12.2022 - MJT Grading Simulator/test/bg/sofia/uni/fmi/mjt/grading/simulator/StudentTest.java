package bg.sofia.uni.fmi.mjt.grading.simulator;

import bg.sofia.uni.fmi.mjt.grading.simulator.grader.CodePostGrader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StudentTest {
    @Test
    void testCreateStudentInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Student(-4, null, null),
                "Faculty number must be positive number - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Student(1, null, null),
                "Name cannot be null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Student(1, "", null),
                "Name cannot be empty or blank - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Student(1, "name", null),
                "Grader cannot be null - expected IllegalArgumentException");
    }

    @Test
    void testCreateStudent() {
        int fn = 23445;
        String name = "gohso";
        CodePostGrader grader = new CodePostGrader(2);

        Student student = new Student(fn, name, grader);

        assertEquals(fn, student.getFn(), "Wrong faculty number");
        assertEquals(name, student.getName(), "Wrong name");
        assertEquals(grader, student.getGrader(), "Wrong grader");

        grader.finalizeGrading();
    }
}
