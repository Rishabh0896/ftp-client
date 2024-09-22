import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A simple FTP Client that interacts with the FTP Server used for CS4700/CS5700
 */
public class FTPClient {

    private Socket controlSocket;
    private BufferedReader controlReader;
    private PrintWriter controlWriter;
    private final String server;
    private final int controlPort;
    private final String username;
    private final String password;

    /**
     * Constructs an FTPClient with the specified server details and credentials.
     *
     * @param server   The hostname or IP address of the FTP server
     * @param port     The port number on which the FTP server is listening
     * @param username The username for authentication
     * @param password The password for authentication
     */
    public FTPClient(String server, int port, String username, String password) {
        this.server = server;
        this.controlPort = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Establishes a connection to the FTP Server and setups up the connection
     */
    public void connect() {
        try {
            controlSocket = new Socket(server, controlPort);
            controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            System.out.println("Connection Successful!");

            String response = readResponse();
            if (!response.startsWith("220 ")) {
                throw new IOException("FTP server not ready. Response: " + response);
            }

            /* Logs in the server with the credentials provided */
            login();

            /* Set up the connection, the server expects this to happen before any files are viewed/transferred */

            /* Set the connection to 8bit binary data mode */
            sendCommand("TYPE I");

            /* Set the connection to stream mode */
            sendCommand("MODE S");

            /* Set the connection to file oriented mode */
            sendCommand("STRU F");

        } catch (IOException e) {
            System.out.println("IO EXCEPTION: " + e);
            e.printStackTrace();
            closeQuietly();
        } catch (Exception e) {
            e.printStackTrace();
            closeQuietly();
        }

    }

    /**
     * Helper method that logs in to the server with the credentials (user,password) provided
     *
     * @throws IOException propagates the IOException thrown by the writer/reader
     */
    private void login() throws IOException {
        String response = sendCommand("USER " + username);
        if (response.startsWith("331 ")) {
            response = sendCommand("PASS " + password);
        } else if (!response.startsWith("230 ")) {
            throw new IOException("Login failed. Response: " + response);
        }
    }

    /**
     * Writes the command on the controlWriter which then sends it to the server
     *
     * @param command Command to send
     * @return response from the server after it receives the command
     * @throws IOException propagates the IOException thrown by the writer/reader
     */
    String sendCommand(String command) throws IOException {
        controlWriter.println(command);
        controlWriter.flush();
        return readResponse();
    }

    /**
     * Reads the response from the FTP server.
     * The BufferedReader readline method takes care of the "\r\n" end of message characters.
     *
     * @return response sent by the server
     * @throws IOException propagates the IOException thrown by the writer/reader
     */
    String readResponse() throws IOException {
        String response = controlReader.readLine();
        System.out.println("Server: " + response);
        return response;
    }

    public void createDirectory(String directory) throws IOException {
        String response = sendCommand("MKD " + directory);
        if (!response.startsWith("257 ")) {
            throw new IOException("Failed to create directory. Response: " + response);
        }
    }

    public void deleteDirectory(String directory) throws IOException {
        String response = sendCommand("RMD " + directory);
        if (!response.startsWith("250 ")) {
            throw new IOException("Failed to remove directory. Response: " + response);
        }
    }

    public void disconnect() throws IOException {
        sendCommand("QUIT");
        closeQuietly();
    }

    /**
     * Closes the control socket and associated streams quietly,
     * without throwing exceptions.
     */
    public void closeQuietly() {
        if (controlReader != null) {
            try {
                controlReader.close();
            } catch (IOException e) {
                System.err.println("Error closing control reader: " + e.getMessage());
            }
        }
        if (controlWriter != null) {
            controlWriter.close();
        }
        if (controlSocket != null) {
            try {
                controlSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing control socket: " + e.getMessage());
            }
        }
    }
}