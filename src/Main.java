import command.FTPExecutor;
import util.FTPPathHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Main class for the FTP client application.
 * This class handles command-line argument parsing, operation execution,
 * and serves as the entry point for the FTP client.
 */
public class Main {

    /**
     * Map of supported FTP operations and their corresponding execution logic.
     */
    private static final Map<String, BiConsumer<FTPExecutor, FTPPathHandler.ParsedPath>> OPERATIONS = new HashMap<>();

    /**
     * Help string containing usage information and available operations.
     */
    public static final String HELP_STR = """
            usage: ./4700ftp [-h] [--verbose] operation params [params ...]
            
            FTP client for listing, copying, moving, and deleting files and directories on remote FTP servers.
            
            positional arguments:
            operation      The operation to execute. Valid operations are 'ls', 'rm', 'rmdir',
                          'mkdir', 'cp', and 'mv'
            params         Parameters for the given operation. Will be one or two paths and/or URLs.
            
            optional arguments:
            -h, --help     show this help message and exit
            --verbose, -v  Print all messages to and from the FTP server
            
            # Available Operations
            
            This FTP client supports the following operations:
            
            ls <URL>                 Print out the directory listing from the FTP server at the given URL
            mkdir <URL>              Create a new directory on the FTP server at the given URL
            rm <URL>                 Delete the file on the FTP server at the given URL
            rmdir <URL>              Delete the directory on the FTP server at the given URL
            cp <ARG1> <ARG2>         Copy the file given by ARG1 to the file given by
                                      ARG2. If ARG1 is a local file, then ARG2 must be a URL, and vice-versa.
            mv <ARG1> <ARG2>         Move the file given by ARG1 to the file given by
                                      ARG2. If ARG1 is a local file, then ARG2 must be a URL, and vice-versa.""";

    static {
        OPERATIONS.put("ls", (executor, path) -> executor.executeCommand(client -> client.listFiles(path.getRemotePath())));
        OPERATIONS.put("mkdir", (executor, path) -> executor.executeCommand(client -> client.createDirectory(path.getRemotePath())));
        OPERATIONS.put("rmdir", (executor, path) -> executor.executeCommand(client -> client.deleteDirectory(path.getRemotePath())));
        OPERATIONS.put("rm", (executor, path) -> executor.executeCommand(client -> client.deleteFile(path.getRemotePath(), true)));
        OPERATIONS.put("cp", (executor, path) -> executor.executeCommand(client -> client.copyFile(path.getRemotePath(), path.getLocalPath(), path.isDownload())));
        OPERATIONS.put("mv", (executor, path) -> executor.executeCommand(client -> client.moveFile(path.getRemotePath(), path.getLocalPath(), path.isDownload())));
    }

    /**
     * Main method serving as the entry point for the FTP client application.
     * Parses command-line arguments, sets up the FTP connection, and executes the requested operation.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        try {
            // Parse command-line arguments
            ParseArgs result = ParseArgs.parse(args);

            // Display help message if requested
            if (result.helpRequested) {
                System.out.println(HELP_STR);
                return;
            }

            // Parse the FTP path
            FTPPathHandler.ParsedPath parsedPath = (result.param1.startsWith("ftp://") ?
                    FTPPathHandler.parse(result.param1, result.param2, true) :
                    FTPPathHandler.parse(result.param2, result.param1, false));

            // Create an FTP executor with the parsed connection details
            FTPExecutor executor = new FTPExecutor(parsedPath.getHost(), parsedPath.getPort(),
                    parsedPath.getUsername(), parsedPath.getPassword());

            // Retrieve the operation to execute
            BiConsumer<FTPExecutor, FTPPathHandler.ParsedPath> operation = OPERATIONS.get(result.operation);
            if (operation == null) {
                throw new IllegalArgumentException("Unknown operation: " + result.operation);
            }

            // Execute the requested operation
            operation.accept(executor, parsedPath);

        } catch (IllegalArgumentException | MalformedURLException e) {
            // Handle errors by displaying the error message and help information
            System.err.println("Error: " + e.getMessage());
            System.out.println(HELP_STR);
            System.exit(1);
        }
    }
}