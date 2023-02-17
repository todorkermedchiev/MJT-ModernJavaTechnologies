package todoist.server;

import todoist.command.CommandExecutor;
import todoist.storage.Storage;
import todoist.storage.serializer.StorageSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Scanner;

public class ServerManager {
    private static final int SERVER_PORT = 9999;
    private static final String STOP_COMMAND = "stop";

    public static void main(String... args) {
        try {
            StorageSerializer serializer = new StorageSerializer();
            Storage storage = serializer.readDataFromFile();
            CommandExecutor executor = new CommandExecutor(storage);

            Server server = new Server(SERVER_PORT, executor);
            server.start();

            while (true) {
                Scanner sc = new Scanner(System.in);
                String input = sc.nextLine();

                if (input.equals(STOP_COMMAND)) {
                    server.stopServer();
                    serializer.saveDataToFile(storage);
                    break;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading and writing to file", e);
        }
    }
}
