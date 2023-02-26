package todoist.server;

import todoist.command.Command;
import todoist.command.CommandCreator;
import todoist.command.CommandExecutor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server extends Thread {
    private static final int BUFFER_SIZE = 2048;
    private static final String HOST = "localhost";

    private final CommandExecutor executor;

    private final int port;
    private final AtomicBoolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    private final Map<SocketChannel, Integer> clientId;

    public Server(int port, CommandExecutor executor) {
        this.executor = executor;
        this.port = port;
        this.clientId = new HashMap<>();
        this.isServerWorking = new AtomicBoolean();
    }

    public void startServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();

            serverSocketChannel.bind(new InetSocketAddress(HOST, port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking.set(true);

            int clientIdCounter = 0;

            while (isServerWorking.get()) {
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

                        System.out.println("Client #" + clientIdCounter + " connected.");
                        clientId.put(clientSocketChannel, clientIdCounter++);

                    } else if (key.isReadable()) {
                        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
                        String clientInput = getClientInput(clientSocketChannel);

                        if (clientInput == null) {
                            continue;
                        }

                        Command command = CommandCreator.newCommand(clientInput);
                        String output = executor.execute(clientId.get(clientSocketChannel), command);

                        writeClientOutput(clientSocketChannel, output);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopServer() {
        this.isServerWorking.set(false);

        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private String getClientInput(SocketChannel clientSocketChannel) throws IOException {
        buffer.clear();

        int readBytes = clientSocketChannel.read(buffer);
        if (readBytes < 0) {
            clientSocketChannel.close();
            return null;
        }

        buffer.flip();

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

    @Override
    public void run() {
        startServer();
    }
}
