package util;

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FTPPathHandler {
    private static final Pattern FTP_URL_PATTERN = Pattern.compile(
            "ftp://(?:([^:@/\\\\]+)(?::([^@/\\\\]+))?@)?([^:/\\\\]+)(?::(\\d+))?([/\\\\].*)?");

    private static final String DEFAULT_USERNAME = "anonymous";

    private static final String DEFAULT_PASSWORD = "";

    private static final int DEFAULT_CONTROL_PORT = 21;

    public static class ParsedPath {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final String remotePath;
        private final String localPath;
        private final boolean isDownload;

        public ParsedPath(String host, int port, String username, String password, String remotePath, String localPath, boolean isDownload) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.remotePath = remotePath;
            this.localPath = localPath;
            this.isDownload = isDownload;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getRemotePath() {
            return remotePath;
        }

        public String getLocalPath() {
            return localPath;
        }

        public boolean isDownload() {
            return isDownload;
        }
    }

    public static ParsedPath parse(String ftpUrl, String localPath, boolean isDownload) throws MalformedURLException {
        Matcher matcher = FTP_URL_PATTERN.matcher(ftpUrl);
        if (!matcher.matches()) {
            throw new MalformedURLException("Invalid FTP URL format");
        }

        String username = matcher.group(1) != null ? matcher.group(1) : DEFAULT_USERNAME;
        String password = matcher.group(2) != null ? matcher.group(2) : DEFAULT_PASSWORD;
        String host = matcher.group(3);
        int port = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : DEFAULT_CONTROL_PORT;
        String remotePath = matcher.group(5) != null ? matcher.group(5) : "/";

        // Normalize the path
        remotePath = normalizePath(remotePath);

        return new ParsedPath(host, port, username, password, remotePath, localPath, isDownload);
    }

    private static String normalizePath(String path) {
        path = path.replace('\\', '/');
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }
}