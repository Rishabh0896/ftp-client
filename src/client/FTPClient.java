package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
     * Constructs an client.FTPClient with the specified server details and credentials.
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
    public void connect() throws IOException {
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

    private int getDataPort() throws IOException {
        String response = sendCommand("PASV");
        System.out.println(response);
        if (!response.startsWith("227 ")) {
            throw new IOException("Failed to enter passive mode. Response: " + response);
        }

        // Find the port that we have to use for the data channel
        String[] address = response.replaceAll("\\D+", " ").split("\\s+");
        Arrays.stream(address).forEach(System.out::println);

        // Open a new Socket connection for the data channel
        return Integer.parseInt(address[address.length - 2]) * 256 + Integer.parseInt(address[address.length - 1]);
    }

    public void listFiles(String path) throws IOException {
        int dataPort = getDataPort();

        try (Socket dataSocket = new Socket(server, dataPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {
            String response = sendCommand("LIST " + path);
            System.out.println(response);
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // Read the closing response
            readResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void copyFile(String remotePath, String localPath, boolean isDownload) throws IOException {
        String operation = isDownload ? "Downloading" : "Uploading";
        System.out.println(operation + " remotePath: " + remotePath);
        System.out.println(operation + " localPath: " + localPath);

        int dataPort = getDataPort();
        String command = isDownload ? "RETR " : "STOR ";

        // Ensure local directory exists for download
        if (isDownload) {
            Files.createDirectories(Paths.get(localPath).getParent());
        }

        try (Socket dataSocket = new Socket(server, dataPort);
             InputStream input = isDownload ? dataSocket.getInputStream() : new FileInputStream(localPath);
             OutputStream output = isDownload ? new FileOutputStream(localPath) : dataSocket.getOutputStream()) {

            // Send the appropriate FTP command
            String response = sendCommand(command + remotePath);
            System.out.println("FTP response: " + response);

            if (!response.startsWith("150") && !response.startsWith("125")) {
                throw new IOException("Failed to initiate file transfer: " + response);
            }

            // Perform the file transfer
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.flush();
        } catch (IOException e) {
            System.err.println("Error during file content transfer: " + e.getMessage());
            throw e;
        } finally {
            String transferResponse = readResponse();
            System.out.println("Transfer completed: " + transferResponse);
        }
    }

    public void moveFile(String remotePath, String localPath, boolean isDownload) throws IOException {
        copyFile(remotePath, localPath, isDownload);
        if (isDownload) {
            deleteFile(remotePath, true);
        } else {
            deleteFile(localPath, false);
        }
    }

    public void deleteFile(String filePath, boolean is_remote) throws IOException {
        System.out.println("Deleting file: " + filePath + " remote: " + is_remote);
        if (is_remote) {
            String response = sendCommand("DELE " + filePath);
            System.out.println(response);
        } else {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("Local file deleted successfully: " + filePath);
            } else {
                throw new IOException("Local file not found: " + filePath);
            }
        }
    }

    public void disconnect() {
        try {
            sendCommand("QUIT");
        } catch (IOException e) {
            closeQuietly();
        }
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