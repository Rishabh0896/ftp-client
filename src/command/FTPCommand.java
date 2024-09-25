package command;

import client.FTPClient;

import java.io.IOException;

public interface FTPCommand {
    void execute(FTPClient client) throws IOException;
}
