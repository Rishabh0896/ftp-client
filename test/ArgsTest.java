import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ArgsTest Class
 *
 * This class contains unit tests for the ParseArgs utility class.
 * It tests various scenarios of command-line argument parsing for the FTP client,
 * including different commands, flags, and error conditions.
 */
class ArgsTest {

    /**
     * Tests parsing of the help flag.
     * Verifies that when "-h" is provided, helpRequested is set to true
     * and other fields are set to their default values.
     */
    @Test
    void testParseArgsWithHelp() {
        String[] args = {"-h"};
        ParseArgs result = ParseArgs.parse(args);
        assertTrue(result.helpRequested);
        assertNull(result.operation);
        assertNull(result.param1);
        assertNull(result.param2);
        assertFalse(result.verbose);
    }

    /**
     * Tests parsing with insufficient arguments.
     * Verifies that an IllegalArgumentException is thrown when only
     * a command is provided without required parameters.
     */
    @Test
    void testParseArgsWithInsufficientArgs() {
        String[] args = {"ls"};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }

    /**
     * Tests parsing of the "ls" command.
     * Verifies correct parsing of the command and its parameter.
     */
    @Test
    void testParseArgsWithLsCommand() {
        String[] args = {"ls", "ftp://example.com"};
        ParseArgs result = ParseArgs.parse(args);
        assertEquals("ls", result.operation);
        assertEquals("ftp://example.com", result.param1);
        assertNull(result.param2);
        assertFalse(result.verbose);
        assertFalse(result.helpRequested);
    }

    /**
     * Tests parsing of the "cp" command with insufficient arguments.
     * Verifies that an IllegalArgumentException is thrown when "cp"
     * is not followed by two parameters.
     */
    @Test
    void testParseArgsWithCpCommandAndInsufficientArgs() {
        String[] args = {"cp", "file1.txt"};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }

    /**
     * Tests parsing of the "cp" command with correct arguments.
     * Verifies correct parsing of the command and its two parameters.
     */
    @Test
    void testParseArgsWithCpCommand() {
        String[] args = {"cp", "file1.txt", "ftp://example.com/file2.txt"};
        ParseArgs result = ParseArgs.parse(args);
        assertEquals("cp", result.operation);
        assertEquals("file1.txt", result.param1);
        assertEquals("ftp://example.com/file2.txt", result.param2);
        assertFalse(result.verbose);
        assertFalse(result.helpRequested);
    }

    /**
     * Tests parsing of the "mv" command.
     * Verifies correct parsing of the command and its two parameters.
     */
    @Test
    void testParseArgsWithMvCommand() {
        String[] args = {"mv", "file1.txt", "ftp://example.com/file2.txt"};
        ParseArgs result = ParseArgs.parse(args);
        assertEquals("mv", result.operation);
        assertEquals("file1.txt", result.param1);
        assertEquals("ftp://example.com/file2.txt", result.param2);
        assertFalse(result.verbose);
        assertFalse(result.helpRequested);
    }

    /**
     * Tests parsing with the verbose flag.
     * Verifies that when "-v" is provided, the verbose flag is set to true
     * and other arguments are correctly parsed.
     */
    @Test
    void testParseArgsWithVerboseFlag() {
        String[] args = {"-v", "ls", "ftp://example.com"};
        ParseArgs result = ParseArgs.parse(args);
        assertEquals("ls", result.operation);
        assertEquals("ftp://example.com", result.param1);
        assertNull(result.param2);
        assertTrue(result.verbose);
        assertFalse(result.helpRequested);
    }

    /**
     * Tests parsing with empty arguments.
     * Verifies that an IllegalArgumentException is thrown when no arguments are provided.
     */
    @Test
    void testParseArgsWithEmptyArgs() {
        String[] args = {};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }
}