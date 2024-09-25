import command.FTPExecutor;
import util.FTPPathHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Main {

    private static final Map<String, BiConsumer<FTPExecutor, FTPPathHandler.ParsedPath>> OPERATIONS = new HashMap<>();

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

    public static void main(String[] args) {
        try {
            ParseArgs result = ParseArgs.parse(args);
            if (result.helpRequested) {
                System.out.println(HELP_STR);
                return;
            }
            FTPPathHandler.ParsedPath parsedPath = (result.param1.startsWith("ftp://") ?
                    FTPPathHandler.parse(result.param1, result.param2, true) :
                    FTPPathHandler.parse(result.param2, result.param1, false));
            FTPExecutor executor = new FTPExecutor(parsedPath.getHost(), parsedPath.getPort(),
                    parsedPath.getUsername(), parsedPath.getPassword());

            BiConsumer<FTPExecutor, FTPPathHandler.ParsedPath> operation = OPERATIONS.get(result.operation);
            if (operation == null) {
                throw new IllegalArgumentException("Unknown operation: " + result.operation);
            }

            operation.accept(executor, parsedPath);

        } catch (IllegalArgumentException | MalformedURLException e) {
            System.err.println("Error: " + e.getMessage());
            System.out.println(HELP_STR);
            System.exit(1);
        }
    }
}