package client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A simple FTP Client that interacts with the FTP Server used for CS5700.
 * This client implements basic FTP operations including connecting, file transfer,
 * directory management, and disconnecting.
 */
public class FTPClient {

    private Socket controlSocket;
    private BufferedReader controlReader;
    private PrintWriter controlWriter;
    private final String server;
    private final int controlPort;
    private final String username;
    private final String password;
    private StringBuffer responseBuffer;

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
     * Establishes a connection to the FTP Server and sets up the connection.
     * This method performs the following steps:
     * 1. Opens a control socket connection
     * 2. Logs in with the provided credentials
     * 3. Sets up the connection parameters (binary mode, stream mode, file-oriented)
     *
     * @throws IOException If there's an error during connection or setup
     */
    public void connect() throws IOException {
        controlSocket = new Socket(server, controlPort);
        controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
        controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
        this.responseBuffer = new StringBuffer();

        String response = readResponse();
        if (!response.startsWith("220 ")) {
            throw new IOException("FTP server not ready. Response: " + response);
        }

        // Log in to the server
        login();

        // Set up the connection parameters
        sendCommand("TYPE I"); // Set to 8-bit binary data mode
        sendCommand("MODE S"); // Set to stream mode
        sendCommand("STRU F"); // Set to file-oriented mode

    }

    /**
     * Logs in to the server with the provided credentials.
     *
     * @throws IOException If login fails or there's an error in communication
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
     * Sends a command to the FTP server and returns the response.
     *
     * @param command The FTP command to send
     * @return The server's response to the command
     * @throws IOException If there's an error in sending the command or reading the response
     */
    public String sendCommand(String command) throws IOException {
        controlWriter.println(command);
        return readResponse();
    }

    /**
     * Reads a response from the FTP server.
     *
     * @return The response sent by the server
     * @throws IOException If there's an error reading from the control connection
     */
    String readResponse() throws IOException {
        String response = controlReader.readLine();
        responseBuffer.append(response).append("\r\n");
        return response;
    }

    /**
     * Creates a new directory on the FTP server.
     *
     * @param directory The name or path of the directory to create
     * @throws IOException If the directory creation fails
     */
    public void createDirectory(String directory) throws IOException {
        String response = sendCommand("MKD " + directory);
        if (!response.startsWith("257 ")) {
            throw new IOException("Failed to create directory. Response: " + response);
        }
    }

    /**
     * Deletes a directory on the FTP server.
     *
     * @param directory The name or path of the directory to delete
     * @throws IOException If the directory deletion fails
     */
    public void deleteDirectory(String directory) throws IOException {
        String response = sendCommand("RMD " + directory);
        if (!response.startsWith("250 ")) {
            throw new IOException("Failed to remove directory. Response: " + response);
        }
    }

    /**
     * Retrieves the data port for passive mode data transfer.
     *
     * @return The port number to use for the data connection
     * @throws IOException If entering passive mode fails
     */
    private int getDataPort() throws IOException {
        String response = sendCommand("PASV");
        if (!response.startsWith("227 ")) {
            throw new IOException("Failed to enter passive mode. Response: " + response);
        }

        // Parse the port number from the server's response
        String[] address = response.replaceAll("\\D+", " ").split("\\s+");
        return Integer.parseInt(address[address.length - 2]) * 256 + Integer.parseInt(address[address.length - 1]);
    }

    /**
     * Lists files in the specified directory on the FTP server.
     *
     * @param path The directory path to list
     * @throws IOException If there's an error during the listing process
     */
    public void listFiles(String path) throws IOException {
        int dataPort = getDataPort();

        try (Socket dataSocket = new Socket(server, dataPort);
             BufferedReader reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()))) {
            String response = sendCommand("LIST " + path);
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                responseBuffer.append(line).append("\r\n");
            }
            // Read the closing response
            readResponse();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Copies a file between the local system and the FTP server.
     *
     * @param remotePath The path of the file on the FTP server
     * @param localPath  The path of the file on the local system
     * @param isDownload True if downloading from server to local, false if uploading
     * @throws IOException If there's an error during the file transfer
     */
    public void copyFile(String remotePath, String localPath, boolean isDownload) throws IOException {

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
            if (isDownload) {
                readResponse();
            }
        }
    }

    /**
     * Moves a file between the local system and the FTP server.
     * This operation is a copy followed by delete of the source file.
     *
     * @param remotePath The path of the file on the FTP server
     * @param localPath  The path of the file on the local system
     * @param isDownload True if moving from server to local, false if moving from local to server
     * @throws IOException If there's an error during the move operation
     */
    public void moveFile(String remotePath, String localPath, boolean isDownload) throws IOException {
        copyFile(remotePath, localPath, isDownload);
        if (isDownload) {
            deleteFile(remotePath, true);
        } else {
            deleteFile(localPath, false);
        }
    }

    /**
     * Deletes a file either on the FTP server or the local system.
     *
     * @param filePath  The path of the file to delete
     * @param isRemote  True if deleting on the server, false if deleting locally
     * @throws IOException If the file deletion fails
     */
    public void deleteFile(String filePath, boolean isRemote) throws IOException {
        if (isRemote) {
            String response = sendCommand("DELE " + filePath);
        } else {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            } else {
                throw new IOException("Local file not found: " + filePath);
            }
        }
    }

    /**
     * Disconnects from the FTP server.
     * This method sends the QUIT command and closes all connections.
     */
    public void disconnect() {
        try {
            sendCommand("QUIT");
        } catch (IOException e) {
            closeQuietly();
        } finally {
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

    /**
     * Retrieves the accumulated response buffer.
     *
     * @return A string containing all server responses received during the session
     */
    public String getResponseBuffer() {
        return responseBuffer.toString();
    }
}