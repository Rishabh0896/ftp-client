package util;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The FTPPathHandler class provides utility methods for parsing and handling FTP URLs.
 * It extracts components such as host, port, username, password, and path from FTP URLs,
 * and provides a structured representation of this information.
 */
public class FTPPathHandler {
    /**
     * Regular expression pattern for parsing FTP URLs.
     * Format: ftp://[username[:password]@]host[:port][/path]
     */
    private static final Pattern FTP_URL_PATTERN = Pattern.compile(
            "ftp://(?:([^:@/\\\\]+)(?::([^@/\\\\]+))?@)?([^:/\\\\]+)(?::(\\d+))?([/\\\\].*)?");

    /** Default username for anonymous FTP access */
    private static final String DEFAULT_USERNAME = "anonymous";

    /** Default password for anonymous FTP access */
    private static final String DEFAULT_PASSWORD = "";

    /** Default control port for FTP connections */
    private static final int DEFAULT_CONTROL_PORT = 21;

    /**
     * Inner class representing a parsed FTP path with all its components.
     */
    public static class ParsedPath {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final String remotePath;
        private final String localPath;
        private final boolean isDownload;

        /**
         * Constructs a ParsedPath object with the specified components.
         *
         * @param host       The FTP server hostname
         * @param port       The FTP server port
         * @param username   The username for FTP authentication
         * @param password   The password for FTP authentication
         * @param remotePath The path on the remote FTP server
         * @param localPath  The corresponding local file system path
         * @param isDownload True if this represents a download operation, false for upload
         */
        public ParsedPath(String host, int port, String username, String password, String remotePath, String localPath, boolean isDownload) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.remotePath = remotePath;
            this.localPath = localPath;
            this.isDownload = isDownload;
        }

        // Getter methods with JavaDoc comments
        /** @return The FTP server hostname */
        public String getHost() { return host; }

        /** @return The FTP server port */
        public int getPort() { return port; }

        /** @return The username for FTP authentication */
        public String getUsername() { return username; }

        /** @return The password for FTP authentication */
        public String getPassword() { return password; }

        /** @return The path on the remote FTP server */
        public String getRemotePath() { return remotePath; }

        /** @return The corresponding local file system path */
        public String getLocalPath() { return localPath; }

        /** @return True if this represents a download operation, false for upload */
        public boolean isDownload() { return isDownload; }
    }

    /**
     * Parses an FTP URL and local path into a structured ParsedPath object.
     *
     * @param ftpUrl    The FTP URL to parse
     * @param localPath The corresponding local file system path
     * @param isDownload True if this represents a download operation, false for upload
     * @return A ParsedPath object containing the extracted components
     * @throws MalformedURLException If the provided FTP URL is invalid
     */
    public static ParsedPath parse(String ftpUrl, String localPath, boolean isDownload) throws MalformedURLException {
        Matcher matcher = FTP_URL_PATTERN.matcher(ftpUrl);
        if (!matcher.matches()) {
            throw new MalformedURLException("Invalid FTP URL format");
        }

        // Extract components from the matched groups
        String username = matcher.group(1) != null ? matcher.group(1) : DEFAULT_USERNAME;
        String password = matcher.group(2) != null ? matcher.group(2) : DEFAULT_PASSWORD;
        String host = matcher.group(3);
        int port = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : DEFAULT_CONTROL_PORT;
        String remotePath = matcher.group(5) != null ? matcher.group(5) : "/";

        // Normalize the remote path
        remotePath = normalizePath(remotePath);

        return new ParsedPath(host, port, username, password, remotePath, localPath, isDownload);
    }

    /**
     * Normalizes a file path by replacing backslashes with forward slashes
     * and ensuring the path starts with a forward slash.
     *
     * @param path The path to normalize
     * @return The normalized path
     */
    private static String normalizePath(String path) {
        path = path.replace('\\', '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }
}