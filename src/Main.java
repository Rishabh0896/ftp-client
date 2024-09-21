public class Main {

    public static final String HELP_STR = """
            usage: 4700ftp [-h] [--verbose] operation params [params ...]
            
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
        try {
            ParseArgs result = ParseArgs.parse(args);
            if (result.helpRequested) {
                System.out.println(HELP_STR);
            } else {
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
    }

}