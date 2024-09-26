/**
 * The ParseArgs class is responsible for parsing and storing command-line arguments
 * for an application that performs operations like copy (cp) and move (mv).
 * It supports verbose mode and help requests.
 */
public class ParseArgs {
    /** The operation to be performed (e.g., "cp" for copy, "mv" for move) */
    public final String operation;

    /** The first parameter for the operation (e.g., source file/directory) */
    public final String param1;

    /** The second parameter for the operation (e.g., destination file/directory) */
    public final String param2;

    /** Flag indicating whether verbose output is requested */
    public final boolean verbose;

    /** Flag indicating whether help information is requested */
    public final boolean helpRequested;

    /**
     * Constructs a ParseArgs object with the specified parameters.
     *
     * @param operation     The operation to be performed
     * @param param1        The first parameter for the operation
     * @param param2        The second parameter for the operation
     * @param verbose       Flag for verbose output
     * @param helpRequested Flag indicating a help request
     */
    public ParseArgs(String operation, String param1, String param2, boolean verbose, boolean helpRequested) {
        this.operation = operation;
        this.param1 = param1;
        this.param2 = param2;
        this.verbose = verbose;
        this.helpRequested = helpRequested;
    }

    /**
     * Parses the given command-line arguments and returns a ParseArgs object.
     *
     * @param args The command-line arguments to parse
     * @return A ParseArgs object representing the parsed arguments
     * @throws IllegalArgumentException If the arguments are invalid or insufficient
     */
    public static ParseArgs parse(String[] args) throws IllegalArgumentException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        // Check for help request
        if (args[0].equals("-h") || args[0].equals("--help")) {
            return new ParseArgs(null, null, null, false, true);
        }

        boolean verbose = false;
        int startIndex = 0;

        // Check for verbose flag
        if (args[0].equals("-v") || args[0].equals("--verbose")) {
            verbose = true;
            startIndex = 1;
        }

        // Ensure sufficient arguments are provided
        if (args.length - startIndex < 2) {
            throw new IllegalArgumentException("Insufficient arguments");
        }

        String operation = args[startIndex];
        String param1 = args[startIndex + 1];
        String param2 = null;

        // Handle operations that require two parameters (cp and mv)
        if (operation.equals("cp") || operation.equals("mv")) {
            if (args.length - startIndex < 3) {
                throw new IllegalArgumentException("Insufficient arguments for " + operation);
            }
            param2 = args[startIndex + 2];
        }

        return new ParseArgs(operation, param1, param2, verbose, false);
    }
}