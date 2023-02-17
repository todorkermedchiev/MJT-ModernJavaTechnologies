package bg.sofia.uni.fmi.mjt.cocktail.server.command;

import bg.sofia.uni.fmi.mjt.cocktail.server.Cocktail;
import bg.sofia.uni.fmi.mjt.cocktail.server.Ingredient;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.CocktailStorage;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.exceptions.CocktailNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {
    CommandExecutor executor;
    CocktailStorage storage;

    @BeforeEach
    void initialize() {
        storage = mock(CocktailStorage.class);
        executor = new CommandExecutor(storage);
    }

    @Test
    void testCreate() {
        Command command = CommandCreator.newCommand("Create something ingredient=100");
        String response = executor.execute(command);
        String expected = "New cocktail something successfully created!";

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testCreateLessArguments() {
        Command command = CommandCreator.newCommand("Create something");
        String response = executor.execute(command);
        String expected = "Invalid count of arguments: \"create\" expects more than 1 arguments.";

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testCreateMoreThanOneCocktail() {
        Command createFirst = CommandCreator.newCommand("create cocktail1 ingredient1=100ml ingredient2=200ml");
        Command createSecond = CommandCreator.newCommand("create cocktail2 ingredinet2=100ml ingredinet3=200ml");

        Cocktail first = new Cocktail("cocktail1", Set.of(new Ingredient("ingredient1", "100ml"),
                new Ingredient("ingredient2", "200ml")));
        Cocktail second = new Cocktail("cocktail2", Set.of(new Ingredient("ingredient2", "100ml"),
                new Ingredient("ingredient3", "200ml")));

        when(storage.getCocktails()).thenReturn(List.of(first, second));

        String response = executor.execute(CommandCreator.newCommand("get all"));
        String expected = first.toString() + second.toString();

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testGetAll() {
        Command createCommand = CommandCreator.newCommand("Create something ingredient=100");
//        executor.execute(createCommand);

        Cocktail cocktail = new Cocktail("something", Set.of(new Ingredient("ingredient", "100")));
        when(storage.getCocktails()).thenReturn(List.of(cocktail));

        Command getAllCommand = CommandCreator.newCommand("get all");
        String response = executor.execute(getAllCommand);
        String expected = cocktail.toString();

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testGetByName() throws CocktailNotFoundException {
        Command createCommand = CommandCreator.newCommand("Create something ingredient=100");
        executor.execute(createCommand);

        Cocktail cocktail = new Cocktail("something", Set.of(new Ingredient("ingredient", "100")));
        when(storage.getCocktail("something")).thenReturn(cocktail);

        Command getByNameCommand = CommandCreator.newCommand("get by-name something");
        String response = executor.execute(getByNameCommand);
        String expected = cocktail.toString();

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testGetByIngredient() {
        Command createCommand = CommandCreator.newCommand("Create something ingredient=100");
        executor.execute(createCommand);

        Cocktail cocktail = new Cocktail("something", Set.of(new Ingredient("ingredient", "100")));
        when(storage.getCocktailsWithIngredient("ingredient")).thenReturn(List.of(cocktail));

        Command getByIngredientCommand = CommandCreator.newCommand("get by-ingredient ingredient");
        String response = executor.execute(getByIngredientCommand);
        String expected = cocktail.toString();

        assertEquals(expected, response, "unexpected response returned");
    }

    @Test
    void testDisconnect() {
        Command command = CommandCreator.newCommand("disconnect");
        String response = executor.execute(command);
        String expected = "Disconnected from the server!";

        assertEquals(expected, response, "unexpected response returned");
    }
}
