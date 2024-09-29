import org.junit.jupiter.api.Test;
import util.FTPPathHandler;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;

/**
 * FTPPathHandlerTest Class
 *
 * This class contains unit tests for the FTPPathHandler utility class.
 * It tests various scenarios of FTP URL parsing, including different URL formats,
 * edge cases, and error conditions.
 */
class FTPPathHandlerTest {

    /**
     * Tests parsing of a basic FTP URL.
     * Verifies correct extraction of host, port, username, and password.
     */
    @Test
    void testBasicFtpUrl() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/file.txt", "", true);

        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals(result.getUsername(), "anonymous");
        assertEquals(result.getPassword(), "");
    }

    /**
     * Tests parsing of an FTP URL with a non-standard port.
     * Verifies correct extraction of host and port.
     */
    @Test
    void testFtpUrlWithPort() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com:2121/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(2121, result.getPort());
    }

    /**
     * Tests parsing of an FTP URL with a username but no password.
     * Verifies correct extraction of host, port, and username.
     * Note: Currently expects null password, which may need revision.
     */
    @Test
    void testFtpUrlWithUsername() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user@example.com/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals("user", result.getUsername());
        // TODO: This should throw an exception
        assertNull(result.getPassword());
    }

    /**
     * Tests parsing of an FTP URL with both username and password.
     * Verifies correct extraction of all components.
     */
    @Test
    void testFtpUrlWithUsernameAndPassword() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user:pass@example.com/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals("user", result.getUsername());
        assertEquals("pass", result.getPassword());
    }

    /**
     * Tests parsing of an FTP URL with a longer path.
     * Verifies correct extraction of host.
     */
    @Test
    void testFtpUrlWithLongerPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/path/to/file.txt", "", true);


        assertEquals("example.com", result.getHost());
    }

    /**
     * Tests parsing of an FTP URL with backslashes in the path.
     * Verifies correct extraction of host.
     */
    @Test
    void testFtpUrlWithBackslashes() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com\\path\\to\\file.txt", "", true);

        assertEquals("example.com", result.getHost());
    }

    /**
     * Tests parsing of an FTP URL with only a root path.
     * Verifies correct extraction of host.
     */
    @Test
    void testFtpUrlWithRootPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/", "", true);

        assertEquals("example.com", result.getHost());
    }

    /**
     * Tests various invalid FTP URL formats.
     * Verifies that MalformedURLException is thrown for each invalid URL.
     */
    @Test
    void testInvalidFtpUrl() {
        String[] invalid_urls = {
                "example.com/file.txt", // Missing protocol
                "ftp://:invalid-url", // Invalid format
                "http://example.com/file.txt", // Invalid Protocol
                "ftp:///path/file.txt", // Missing Host
                "ftp://example.com:abc/file.txt", // Non-numeric port
                "ftp://user@name:password@example.com/file.txt", // Invalid character in username
        };
        for (String url : invalid_urls) {
            assertThrows(MalformedURLException.class, () ->
                    FTPPathHandler.parse(url, "", true)
            );
        }
    }

    /**
     * Tests normalization of FTP paths.
     * Verifies that paths with multiple slashes or backslashes are correctly normalized.
     */
    @Test
    void testNormalizeFtpPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com//path//to\\file.txt", "", true);
        assertEquals("/path/to/file.txt", result.getRemotePath());
    }

    /**
     * Tests the display format of a parsed FTP URL.
     * Verifies that the URL is correctly formatted for display, including all components.
     */
    @Test
    void testToDisplayPathFtp() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user:pass@example.com:2121/path/to/file.txt", "", true);
        assertEquals("FTP URL: ftp://user:pass@example.com:2121/path/to/file.txt", result.getRemotePath());
    }
}