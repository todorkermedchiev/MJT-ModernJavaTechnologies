package bg.sofia.uni.fmi.mjt.cocktail.server.command;

import bg.sofia.uni.fmi.mjt.cocktail.server.Cocktail;
import bg.sofia.uni.fmi.mjt.cocktail.server.Ingredient;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.CocktailStorage;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.exceptions.CocktailAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.exceptions.CocktailNotFoundException;

import java.util.Collection;
import java.util.HashSet;

public class CommandExecutor {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects %s arguments.";
    private final CocktailStorage storage;

    public CommandExecutor(CocktailStorage storage) {
        this.storage = storage;
    }

    public String execute(Command command) {
        return switch (command.type()) {
            case CREATE -> create(command.arguments());
            case GET_ALL -> getAll();
            case GET_BY_NAME -> getByName(command.arguments());
            case GET_BY_INGREDIENT -> getByIngredient(command.arguments());
            case DISCONNECT -> disconnect();
            case UNKNOWN -> "Unknown command";
        };
    }

    private String create(String... arguments) {
        if (arguments.length < 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CommandType.CREATE.name, "more than 1");
        }

        String name = arguments[0];

        Cocktail newCocktail = new Cocktail(name, new HashSet<>());

        for (int i = 1; i < arguments.length; ++i) {
            String[] ingredient = arguments[i].split("=");
            newCocktail.ingredients().add(new Ingredient(ingredient[0], ingredient[1]));
        }

        try {
            storage.createCocktail(newCocktail);
        } catch (CocktailAlreadyExistsException e) {
            return String.format("Cocktail %s already exists", name);
        }

        return String.format("New cocktail %s successfully created!", name);
    }

    private String getAll() {
        Collection<Cocktail> cocktails = storage.getCocktails();

        StringBuilder response = new StringBuilder();

        for (Cocktail cocktail : cocktails) {
            response.append(cocktail.toString());
        }

        return response.toString();
    }

    private String getByName(String... arguments) {
        if (arguments.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CommandType.GET_BY_NAME.name, "1");
        }

        String name = arguments[0];

        try {
            return storage.getCocktail(name).toString();
        } catch (CocktailNotFoundException e) {
            return String.format("Cocktail with name \"%s\" not found", name);
        }
    }

    private String getByIngredient(String... arguments) {
        if (arguments.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CommandType.GET_BY_INGREDIENT.name, "1");
        }

        String ingredient = arguments[0];

        Collection<Cocktail> cocktails = storage.getCocktailsWithIngredient(ingredient);
        if (cocktails.isEmpty()) {
            return "Cocktail with ingredient \"" + ingredient + "\" not found";
        }

        StringBuilder response = new StringBuilder();
        for (Cocktail cocktail : cocktails) {
            response.append(cocktail.toString());
        }

        return response.toString();
    }

    private String disconnect() {
        return "Disconnected from the server!";
    }
}
