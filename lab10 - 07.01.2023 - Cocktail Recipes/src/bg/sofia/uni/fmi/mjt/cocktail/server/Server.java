package bg.sofia.uni.fmi.mjt.cocktail.server;

import bg.sofia.uni.fmi.mjt.cocktail.server.command.Command;
import bg.sofia.uni.fmi.mjt.cocktail.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.cocktail.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cocktail.server.command.CommandType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor executor;

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public Server(int port, CommandExecutor executor) {
        this.port = port;
        this.executor = executor;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();

            serverSocketChannel.bind(new InetSocketAddress(HOST, port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            int connectedClients = 0;

            while (isServerWorking) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        ServerSocketChannel keyServerSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientSocketChannel = keyServerSocketChannel.accept();

                        clientSocketChannel.configureBlocking(false);
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);

                        ++connectedClients;
                    } else if (key.isReadable()) {
                        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                        String clientInput = getClientInput(clientSocketChannel);
//                        System.out.println(clientInput); // ???

                        if (clientInput == null) {
                            continue;
                        }

                        Command command = CommandCreator.newCommand(clientInput);
                        String output = executor.execute(command);

                        writeClientOutput(clientSocketChannel, output);

                        if (command.type().equals(CommandType.DISCONNECT)) {
                            --connectedClients;
                            if (connectedClients <= 0) {
                                stop();
                            }
                        }
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private String getClientInput(SocketChannel clientSocketChannel) throws IOException {
        buffer.clear(); // switch to writing mode

        int readBytes = clientSocketChannel.read(buffer);
        if (readBytes < 0) {
            clientSocketChannel.close();
            return null;
        }

        buffer.flip(); // switch to reading mode

        byte[] inputBytes = new byte[buffer.remaining()];
        buffer.get(inputBytes);

        return new String(inputBytes);
    }

    private void writeClientOutput(SocketChannel clientSocketChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientSocketChannel.write(buffer);
    }
}
