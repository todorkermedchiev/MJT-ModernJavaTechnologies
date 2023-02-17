package bg.sofia.uni.fmi.mjt.grading.simulator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssistantTest {
    @Test
    void testCreatingAssistantInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> new Assistant(null, null),
                "The name is null - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Assistant("", null),
                "The name is empty - expected IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> new Assistant("name", null),
                "The grader is null - expected IllegalArgumentException");
    }
}
