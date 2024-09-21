import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArgsTest {

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

    @Test
    void testParseArgsWithInsufficientArgs() {
        String[] args = {"ls"};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }

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

    @Test
    void testParseArgsWithCpCommandAndInsufficientArgs() {
        String[] args = {"cp", "file1.txt"};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }

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

    @Test
    void testParseArgsWithEmptyArgs() {
        String[] args = {};
        assertThrows(IllegalArgumentException.class, () -> ParseArgs.parse(args));
    }
}