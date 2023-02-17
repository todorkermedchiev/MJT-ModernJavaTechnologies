import bg.sofia.uni.fmi.mjt.cocktail.server.Server;
import bg.sofia.uni.fmi.mjt.cocktail.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.DefaultCocktailStorage;

public class ServerStarter {
    private static final int SERVER_PORT = 9999;

    public static void main(String... args) throws InterruptedException {
        Server server = new Server(SERVER_PORT, new CommandExecutor(new DefaultCocktailStorage()));
        server.start();

//        Thread.sleep(120_000);

        server.stop();
    }
}
