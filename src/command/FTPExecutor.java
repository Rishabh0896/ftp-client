package command;

import client.FTPClient;

import java.io.IOException;

public class FTPExecutor {
    private final String server;
    private final int port;
    private final String username;
    private final String password;

    public FTPExecutor(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void executeCommand(FTPCommand command) {
        FTPClient client = new FTPClient(server, port, username, password);
        try {
            client.connect();
            command.execute(client);
        } catch (IOException e) {
            // TODO: Figure out what to do here
            e.printStackTrace();
        } finally {
            client.disconnect();
        }
    }
}
