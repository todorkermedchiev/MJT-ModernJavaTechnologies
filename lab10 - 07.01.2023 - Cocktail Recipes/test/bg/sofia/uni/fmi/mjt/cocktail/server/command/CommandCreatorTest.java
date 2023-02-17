package bg.sofia.uni.fmi.mjt.cocktail.server.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CommandCreatorTest {
    @Test
    public void testCommandCreationWithNoArguments() {
        String command = "test";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(CommandType.UNKNOWN, cmd.type(), "unexpected command returned for command 'test'");
        assertNotNull(cmd.arguments(), "command arguments should not be null");
        assertEquals(0, cmd.arguments().length, "unexpected command arguments count");
    }

    @Test
    public void testCommandCreationWithOneArgument() {
        String command = "create arg=1";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(CommandType.CREATE, cmd.type(), "unexpected command returned for command 'create arg=1'");
        assertNotNull(cmd.arguments(), "command arguments should not be null");
        assertEquals(1, cmd.arguments().length, "unexpected command arguments count");
        assertEquals(command.split(" ")[1], cmd.arguments()[0], "unexpected argument returned for command 'test abcd'");
    }

    @Test
    public void testCommandCreationInvalidCommand() {
        String command = "get some";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(CommandType.UNKNOWN, cmd.type(), "unexpected command type returned");
        assertNotNull(cmd.arguments(), "command arguments should not be null");
        assertEquals(0, cmd.arguments().length, "unexpected command arguments count");
    }
}
