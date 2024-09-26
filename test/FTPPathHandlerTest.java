import org.junit.jupiter.api.Test;
import util.FTPPathHandler;

import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;

class FTPPathHandlerTest {

    @Test
    void testBasicFtpUrl() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/file.txt", "", true);

        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals(result.getUsername(), "anonymous");
        assertEquals(result.getPassword(), "");
    }

    @Test
    void testFtpUrlWithPort() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com:2121/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(2121, result.getPort());
    }

    @Test
    void testFtpUrlWithUsername() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user@example.com/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals("user", result.getUsername());
        // TODO: This should throw an exception
        assertNull(result.getPassword());
    }

    @Test
    void testFtpUrlWithUsernameAndPassword() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user:pass@example.com/file.txt", "", true);


        assertEquals("example.com", result.getHost());
        assertEquals(21, result.getPort());
        assertEquals("user", result.getUsername());
        assertEquals("pass", result.getPassword());
    }

    @Test
    void testFtpUrlWithLongerPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/path/to/file.txt", "", true);


        assertEquals("example.com", result.getHost());
    }

    @Test
    void testFtpUrlWithBackslashes() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com\\path\\to\\file.txt", "", true);

        assertEquals("example.com", result.getHost());
    }

    @Test
    void testFtpUrlWithRootPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com/", "", true);

        assertEquals("example.com", result.getHost());
    }

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

    @Test
    void testNormalizeFtpPath() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://example.com//path//to\\file.txt", "", true);
        assertEquals("/path/to/file.txt", result.getRemotePath());
    }

    @Test
    void testToDisplayPathFtp() throws MalformedURLException {
        FTPPathHandler.ParsedPath result = FTPPathHandler.parse("ftp://user:pass@example.com:2121/path/to/file.txt", "", true);
        assertEquals("FTP URL: ftp://user:pass@example.com:2121/path/to/file.txt", result.getRemotePath());
    }
}