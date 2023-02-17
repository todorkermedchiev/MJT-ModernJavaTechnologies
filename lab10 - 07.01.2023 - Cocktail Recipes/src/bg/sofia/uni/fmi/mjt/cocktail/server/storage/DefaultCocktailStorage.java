package bg.sofia.uni.fmi.mjt.cocktail.server.storage;

import bg.sofia.uni.fmi.mjt.cocktail.server.Cocktail;
import bg.sofia.uni.fmi.mjt.cocktail.server.Ingredient;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.exceptions.CocktailAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.cocktail.server.storage.exceptions.CocktailNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultCocktailStorage implements CocktailStorage {

//    private final Set<Cocktail> cocktails;
    private final Map<String, Cocktail> cocktails;

    public DefaultCocktailStorage() {
//        cocktails = new HashSet<>();
        cocktails = new HashMap<>();
    }

    /**
     * Creates a new cocktail recipe
     *
     * @param cocktail cocktail
     * @throws CocktailAlreadyExistsException if the same cocktail already exists
     */
    @Override
    public void createCocktail(Cocktail cocktail) throws CocktailAlreadyExistsException {
        validateNullObject(cocktail);
//        if (cocktails.contains(cocktail)) {
//            throw new CocktailAlreadyExistsException("Cocktail " + cocktail.name() + " already exists");
//        }
//
//        cocktails.add(cocktail);
        if (cocktails.containsKey(cocktail.name().toLowerCase())) {
            throw new CocktailAlreadyExistsException("Cocktail " + cocktail.name() + " already exists");
        }

        cocktails.put(cocktail.name().toLowerCase(), cocktail);
    }

    /**
     * Retrieves all cocktail recipes
     *
     * @return all cocktail recipes from the storage, in undefined order.
     * If there are no cocktails in the storage, returns an empty collection.
     */
    @Override
    public Collection<Cocktail> getCocktails() {
//        return Collections.unmodifiableSet(cocktails);
        return Collections.unmodifiableCollection(cocktails.values());
    }

    /**
     * Retrieves all cocktail recipes with given ingredient
     *
     * @param ingredientName name of the ingredient (case-insensitive)
     * @return all cocktail recipes with given ingredient from the storage, in undefined order.
     * If there are no cocktails in the storage with the given ingredient, returns an empty collection.
     */
    @Override
    public Collection<Cocktail> getCocktailsWithIngredient(String ingredientName) {
        validateString(ingredientName);
//        return cocktails.stream()
//                .filter(cocktail -> cocktail.ingredients().contains(new Ingredient(ingredientName, "")))
//                .toList();
        return cocktails.values().stream()
                .filter(cocktail -> cocktail.ingredients().contains(new Ingredient(ingredientName, "")))
                .toList();
    }

    /**
     * Retrieves a cocktail recipe with the given name
     *
     * @param name cocktail name (case-insensitive)
     * @return cocktail with the given name
     * @throws CocktailNotFoundException if a cocktail with the given name does not exist in the storage
     */
    @Override
    public Cocktail getCocktail(String name) throws CocktailNotFoundException {
        validateString(name);
        if (!cocktails.containsKey(name.toLowerCase())) {
            throw new CocktailNotFoundException("Cocktail " + name + " not found");
        }

        return cocktails.get(name.toLowerCase());
    }

    private void validateNullObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
    }

    private void validateString(String str) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException("String cannot be null or blank");
        }
    }
}
