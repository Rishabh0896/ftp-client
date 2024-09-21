public class ParseArgs {
    public final String operation;
    public final String param1;
    public final String param2;
    public final boolean verbose;
    public final boolean helpRequested;

    public ParseArgs(String operation, String param1, String param2, boolean verbose, boolean helpRequested) {
        this.operation = operation;
        this.param1 = param1;
        this.param2 = param2;
        this.verbose = verbose;
        this.helpRequested = helpRequested;
    }

    public static ParseArgs parse(String[] args) throws IllegalArgumentException {
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments provided");
        }

        if (args[0].equals("-h") || args[0].equals("--help")) {
            return new ParseArgs(null, null, null, false, true);
        }

        boolean verbose = false;
        int startIndex = 0;

        if (args[0].equals("-v") || args[0].equals("--verbose")) {
            verbose = true;
            startIndex = 1;
        }

        if (args.length - startIndex < 2) {
            throw new IllegalArgumentException("Insufficient arguments");
        }

        String operation = args[startIndex];
        String param1 = args[startIndex + 1];
        String param2 = null;

        if (operation.equals("cp") || operation.equals("mv")) {
            if (args.length - startIndex < 3) {
                throw new IllegalArgumentException("Insufficient arguments for " + operation);
            }
            param2 = args[startIndex + 2];
        }

        return new ParseArgs(operation, param1, param2, verbose, false);
    }
}
