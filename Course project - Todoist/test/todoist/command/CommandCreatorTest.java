package todoist.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandCreatorTest {
    @Test
    void testCreateCommandNullString() {
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.newCommand(null),
                "Expected IllegalArgumentException to be thrown when the command string is null");
    }

    @Test
    void testCreateCommandEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.newCommand(""),
                "Expected IllegalArgumentException to be thrown when the command string is empty");
    }

    @Test
    void testCreateCommandBlankString() {
        assertThrows(IllegalArgumentException.class, () -> CommandCreator.newCommand("  "),
                "Expected IllegalArgumentException to be thrown when the command string is blank");
    }

    @Test
    void testCreateCommandUnknownCommand() {
        Command command = CommandCreator.newCommand("unknownCommand");
        assertEquals(CommandType.UNKNOWN, command.type(), "Unecpected command type returned: expected unknown");
    }

    @Test
    void testCreateCommandWithoutArguments() {
        Command command = CommandCreator.newCommand("register");
        assertEquals(CommandType.REGISTER, command.type(),
                "Unexpected command type returned: expected register");
    }

    @Test
    void testCreateCommandWithOneArgument() {
        Command command = CommandCreator.newCommand("register --username=username");
        assertEquals(CommandType.REGISTER, command.type(),
                "Unexpected command type returned: expected register");
        assertEquals(1, command.arguments().length,
                "Unexpected command type returned: expected command with 1 argument.");
        assertEquals("username=username", command.arguments()[0],
                "Unexpected command type returned: expected \"username=username\" argument to be returned");
    }

    @Test
    void testCreateCommandWithMoreArguments() {
        Command command = CommandCreator.newCommand("register --arg1=val1 --arg2=val2 --arg3=val3");
        assertEquals(CommandType.REGISTER, command.type(),
                "Unexpected command type returned: expected register");
        assertEquals(3, command.arguments().length,
                "Unexpected command type returned: expected command with 3 arguments.");
        assertEquals("arg1=val1", command.arguments()[0],
                "Unexpected command type returned: expected \"arg1=val1\" argument to be returned");
        assertEquals("arg2=val2", command.arguments()[1],
                "Unexpected command type returned: expected \"arg2=val2\" argument to be returned");
        assertEquals("arg3=val3", command.arguments()[2],
                "Unexpected command type returned: expected \"arg3=val3\" argument to be returned");
    }
}
