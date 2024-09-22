import java.io.IOException;

public class Main {

    private static final String HOST = "ftp.4700.network";

    private static final int CONTROL_PORT = 21;

    private static final String USER = "gupta.risha";

    private static final String PASSWORD = "f60048066e1d153c5d1ecd1032a26369766ed8b32e1ef4b0466d553cbe8a77ef";

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

    public static void main(String[] args) {
        String operation = "";
        String param1 = "";
        String param2 = "";
        FTPClient client;
        try {
            ParseArgs result = ParseArgs.parse(args);
            if (result.helpRequested) {
                System.out.println(HELP_STR);
                System.exit(0);
            } else {
                operation = result.operation;
                param1 = result.param1;
                param2 = result.param2;
                // Use the result to execute the FTP operation
                System.out.println("Operation: " + result.operation);
                System.out.println("Param1: " + result.param1);
                System.out.println("Param2: " + result.param2);
                System.out.println("Verbose: " + result.verbose);
            }

        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println(HELP_STR);
            System.exit(1);
        }

        client = new FTPClient(HOST, CONTROL_PORT, USER, PASSWORD);
        client.connect();

        // Process the command that was passed

        try {
            executeCommand(client, operation, param1, param2);
            client.disconnect();
        } catch (IOException e) {
            client.closeQuietly();
        }
    }

    public static void executeCommand(FTPClient client, String operation, String param1, String param2) throws IOException {
        switch (operation) {
            case "mkdir":
                // RUN MKD
                client.createDirectory(param1);
                break;
            case "rmdir":
                // RUN RMD
                client.deleteDirectory(param1);
                break;
        }
    }
}