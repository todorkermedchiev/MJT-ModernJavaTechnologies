package bg.sofia.uni.fmi.mjt.smartfridge;

import bg.sofia.uni.fmi.mjt.smartfridge.exception.FridgeCapacityExceededException;
import bg.sofia.uni.fmi.mjt.smartfridge.exception.InsufficientQuantityException;
import bg.sofia.uni.fmi.mjt.smartfridge.ingredient.DefaultIngredient;
import bg.sofia.uni.fmi.mjt.smartfridge.ingredient.Ingredient;
import bg.sofia.uni.fmi.mjt.smartfridge.recipe.Recipe;
import bg.sofia.uni.fmi.mjt.smartfridge.storable.Storable;
import bg.sofia.uni.fmi.mjt.smartfridge.storable.StorableByExpirationComparator;

import javax.swing.text.html.parser.Entity;
import java.util.*;

public class SmartFridge implements SmartFridgeAPI {
    Map<String, List<Storable>> items;
    //    Map<String, List<? super Storable>> items;
    private final int totalCapacity;
    private int itemsCount;

    public SmartFridge(int totalCapacity) {
        this.totalCapacity = totalCapacity;
        items = new HashMap<>();
    }

    @Override
    public <E extends Storable> void store(E item, int quantity) throws FridgeCapacityExceededException {
        validateObject(item);
        validateInt(quantity);

        if (totalCapacity - itemsCount < quantity) {
            throw new FridgeCapacityExceededException("The fridge is full - cannot store " + quantity + " items");
        }

        if (!items.containsKey(item.getName())) {
            items.put(item.getName(), new LinkedList<>());
        }

        List<Storable> currentList = items.get(item.getName());
//        List<? super Storable> currentList = items.get(item.getName());
        for (int i = 0; i < quantity; ++i) {
            currentList.add(item);
            ++itemsCount;
        }
        currentList.sort(new StorableByExpirationComparator());
//        items.put(item.getName(), currentList);
    }

    @Override
    public List<? extends Storable> retrieve(String itemName) {
        validateString(itemName);

        if (!items.containsKey(itemName)) {
            return new LinkedList<>();
        }

        itemsCount -= items.get(itemName).size();
        return items.remove(itemName);
    }

    @Override
    public List<? extends Storable> retrieve(String itemName, int quantity) throws InsufficientQuantityException {
        validateString(itemName);
        validateInt(quantity);

        if (!items.containsKey(itemName) || items.get(itemName).size() < quantity) {
            throw new InsufficientQuantityException("The stored quantity is insufficient");
        }

        List<Storable> stored = items.get(itemName);
        List<Storable> toReturn = new LinkedList<>();
        for (int i = 0; i < quantity; ++i) {
            toReturn.add(stored.remove(0));
        }
        itemsCount -= quantity;

        return toReturn;
    }

    @Override
    public int getQuantityOfItem(String itemName) {
        validateString(itemName);

        if (!items.containsKey(itemName)) {
            return 0;
        }

        return items.get(itemName).size();
    }

    @Override
    public Iterator<Ingredient<? extends Storable>> getMissingIngredientsFromRecipe(Recipe recipe) {
        validateObject(recipe);

        Set<Ingredient<? extends Storable>> missingIngredients = new HashSet<>();

        Set<Ingredient<? extends Storable>> neededIngredients = recipe.getIngredients();
        for (Ingredient<? extends Storable> neededIngredient : neededIngredients) {
            if (!items.containsKey(neededIngredient.item().getName())) {
                missingIngredients.add(neededIngredient);
                continue;
            }

            int availableQuantity = items.get(neededIngredient.item().getName()).size() -
                    getExpiredItemsCountByName(neededIngredient.item().getName());

            if (neededIngredient.quantity() > availableQuantity) {
                missingIngredients.add(new DefaultIngredient<>(neededIngredient.item(),
                        neededIngredient.quantity() - availableQuantity));
            }
        }

        return neededIngredients.iterator();
    }

    @Override
    public List<? extends Storable> removeExpired() {
        List<Storable> expired = new ArrayList<>();

        Set<String> keys = items.keySet();
        for (String key : keys) {
            List<? extends Storable> current = items.get(key);
            List<Storable> notExpired = new LinkedList<>();
            for (Storable item : current) {
                if (item.isExpired()) {
                    expired.add(item);
                } else {
                    notExpired.add(item);
                }
            }

            items.put(key, notExpired);
        }

        itemsCount -= expired.size();

        return expired;
    }

    private int getExpiredItemsCountByName(String itemName) {
        validateString(itemName);

        if (!items.containsKey(itemName)) {
            return 0;
        }

        int counter = 0;
        List<? extends Storable> current = items.get(itemName);
        for (Storable item : current) {
            if (item.isExpired()) {
                ++counter;
            }
        }
        return counter;
    }

    // Validators
    private void validateString(String string) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException("Invalid string");
        }
    }

    private void validateObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object can not be null");
        }
    }

    private void validateInt(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("The value can not be negative");
        }
    }
}
