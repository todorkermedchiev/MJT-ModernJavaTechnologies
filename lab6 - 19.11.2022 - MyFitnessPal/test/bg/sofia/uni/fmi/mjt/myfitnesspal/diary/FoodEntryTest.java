package bg.sofia.uni.fmi.mjt.myfitnesspal.diary;

import bg.sofia.uni.fmi.mjt.myfitnesspal.nutrition.NutritionInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FoodEntryTest {

    @Test
    void testFoodEntryNullFood() {
        NutritionInfo info = new NutritionInfo(20, 40, 40);
        assertThrows(IllegalArgumentException.class, () -> new FoodEntry(null, 1, info),
                "Food is null - Expected IllegalArgumentException");
    }

    @Test
    void testFoodEntryEmptyFood() {
        NutritionInfo info = new NutritionInfo(20, 40, 40);
        assertThrows(IllegalArgumentException.class, () -> new FoodEntry("", 1, info),
                "Food is empty - Expected IllegalArgumentException");
    }

    @Test
    void testFoodEntryBlankFood() {
        NutritionInfo info = new NutritionInfo(20, 40, 40);
        assertThrows(IllegalArgumentException.class, () -> new FoodEntry("  ", 1, info),
                "Food is blank - Expected IllegalArgumentException");
    }

    @Test
    void testFoodEntryNegativeServingSize() {
        NutritionInfo info = new NutritionInfo(20, 40, 40);
        assertThrows(IllegalArgumentException.class, () -> new FoodEntry("name", -2.2, info),
                "ServingSize is negative - expected IllegalArgumentException");
    }

    @Test
    void testFoodEntryNullNutritionInfo() {
        assertThrows(IllegalArgumentException.class, () -> new FoodEntry("name", 2.2, null),
                "nutritionInfo is null - Expected IllegalArgumentException");
    }
}
