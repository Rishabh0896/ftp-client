package command;

import client.FTPClient;

import java.io.IOException;

/**
 * The FTPCommand interface represents a command to be executed on an FTP server.
 * It defines a single method that encapsulates the execution logic for a specific FTP operation.
 * This interface is designed to be implemented by various concrete command classes,
 * each representing a different FTP operation (e.g., upload, download, list files).
 */
public interface FTPCommand {

    /**
     * Executes the FTP command using the provided FTPClient.
     * This method should contain the logic for performing a specific FTP operation.
     * Implementations of this method should use the methods provided by the FTPClient
     * to interact with the FTP server and perform the desired operation.
     *
     * @param client The FTPClient instance to use for executing the command.
     *               This client should already be connected to the FTP server.
     * @throws IOException If an I/O error occurs during the execution of the command.
     *                     This could be due to network issues, server errors, or other IO-related problems.
     */
    void execute(FTPClient client) throws IOException;
}