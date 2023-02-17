package todoist.client;

import todoist.command.CommandType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private static final int SERVER_PORT = 9999;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2048;

    private static final String LOGS_FILE_PATH = "resources/log.txt";

    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("<- ");
                String message = scanner.nextLine();

                BUFFER.clear();
                BUFFER.put(message.getBytes());
                BUFFER.flip();
                socketChannel.write(BUFFER);

                BUFFER.clear();
                socketChannel.read(BUFFER);
                BUFFER.flip();

                byte[] byteArray = new byte[BUFFER.remaining()];
                BUFFER.get(byteArray);
                String reply = new String(byteArray, StandardCharsets.UTF_8);

                System.out.println("-> " + reply);
                if (message.equalsIgnoreCase(CommandType.DISCONNECT.name)) {
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("There is a problem with the network communication. " +
                    "Try again later or contact administrator by providing the logs in " +
                    LOGS_FILE_PATH);

            try (var fileWriter = new FileWriter(LOGS_FILE_PATH, true);
                 var bufferedWriter = new BufferedWriter(fileWriter);
                 var printWriter = new PrintWriter(bufferedWriter, true)) {

                printWriter.println(LocalDateTime.now());
                Arrays.stream(e.getStackTrace()).forEach(printWriter::println);
                printWriter.println("");

            } catch (IOException exception) {
                System.out.println("Could not save the log for the exception");
                throw new UncheckedIOException(exception); // e.printStackTrace();
            }
        }
    }
}
