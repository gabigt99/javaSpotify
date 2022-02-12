package bg.sofia.uni.fmi.mjt.spotify.server.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCreatorTest {

    @Test
    public void testCommandCreationWithNoArguments() {
        String command = "disconnect";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(command, cmd.command(), "Unexpected command returned for command 'disconnect'");
        assertNotNull(cmd.arguments(), "Command arguments should not be null");
        assertEquals(0, cmd.arguments().length, "Unexpected command arguments count");
    }

    @Test
    public void testCommandCreationWithOneArgument() {
        String command = "create-playlist rock";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(command.split(" ")[0], cmd.command(), "Unexpected command returned for command 'create-playlist rock'");
        assertNotNull(cmd.arguments(), "Command arguments should not be null");
        assertEquals(1, cmd.arguments().length, "Unexpected command arguments count");
        assertEquals(command.split(" ")[1], cmd.arguments()[0], "Unexpected argument returned for command 'create-playlist rock'");
    }

    @Test
    public void testCommandCreationWithOneArgumentInQuotes() {
        String command = "create-playlist \"rock forever\"";
        Command cmd = CommandCreator.newCommand(command);

        assertEquals(command.split(" ")[0], cmd.command(), "Unexpected command returned for command 'create-playlist \"rock forever\"'");
        assertNotNull(cmd.arguments(), "Command arguments should not be null");
        assertEquals(1, cmd.arguments().length, "Unexpected command arguments count");
        assertEquals("rock forever", cmd.arguments()[0], "Multi-word argument is not respected");
    }
}