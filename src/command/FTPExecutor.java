package command;

import client.FTPClient;

import java.io.IOException;

/**
 * The FTPExecutor class is responsible for executing FTP commands using an FTPClient.
 * It manages the connection lifecycle and provides methods to execute commands with and without log capture.
 */
public class FTPExecutor {
    private final FTPClient client;

    /**
     * Constructs an FTPExecutor with the specified server details and credentials.
     *
     * @param server   The hostname or IP address of the FTP server
     * @param port     The port number on which the FTP server is listening
     * @param username The username for authentication
     * @param password The password for authentication
     */
    public FTPExecutor(String server, int port, String username, String password) {
        this.client = new FTPClient(server, port, username, password);
    }

    /**
     * Executes an FTP command by connecting to the server, running the command, and then disconnecting.
     * Any IOException that occurs during the process is caught and printed to the error stream.
     *
     * @param command The FTPCommand to execute
     */
    public void executeCommand(FTPCommand command) {
        try {
            client.connect();
            command.execute(client);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.disconnect();
        }
    }

    /**
     * Executes an FTP command and captures the logs of the operation.
     * This method connects to the server, runs the command, disconnects, and then returns the log of the operation.
     * This is majorly used for the record replay tests and is not used for any other interaction in the client code.
     *
     * @param command The FTPCommand to execute
     * @return A String containing the logs of the FTP operation
     */
    public String executeCommandCaptureLogs(FTPCommand command) {
        try {
            client.connect();
            command.execute(client);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.disconnect();
        }
        return client.getResponseBuffer();
    }
}
