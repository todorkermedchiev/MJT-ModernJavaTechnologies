package bg.sofia.uni.fmi.mjt.myfitnesspal.diary;

import bg.sofia.uni.fmi.mjt.myfitnesspal.exception.UnknownFoodException;
import bg.sofia.uni.fmi.mjt.myfitnesspal.nutrition.NutritionInfo;
import bg.sofia.uni.fmi.mjt.myfitnesspal.nutrition.NutritionInfoAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DailyFoodDiaryTest {

    @Mock
    private NutritionInfoAPI nutritionInfoAPIMock;

    @InjectMocks
    private DailyFoodDiary diary;

    @Test
    void testAddFoodWhenMealIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.addFood(null, "name", 0.1),
                    "Meal is null - Expected IllegalArgumentException, but it was not thrown");
    }

    @Test
    void testAddFoodWhenNameIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.addFood(Meal.BREAKFAST, null, 0.1),
                "Name is null - Expected IllegalArgumentException, but it was not thrown");
    }

    @Test
    void testAddFoodWhenNameIsEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.addFood(Meal.BREAKFAST, "", 0.1),
                "Name is empty - Expected IllegalArgumentException, but it was not thrown");
    }

    @Test
    void testAddFoodWhenNameIsBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.addFood(Meal.BREAKFAST, " ", 0.1),
                "Name is blank - Expected IllegalArgumentException, but it was not thrown");
    }

    @Test
    void testAddFoodWhenServingSizeIsNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.addFood(Meal.BREAKFAST, "name", -2.2),
                "ServingSize is negative - Expected IllegalArgumentException, but it was not thrown");
    }

    @Test
    void testAddFoodWhenNoNutritionInfoIsAvailableThrowsException() throws UnknownFoodException {
        when(nutritionInfoAPIMock.getNutritionInfo("unknown food"))
                .thenThrow(new UnknownFoodException("No nutrition info is available for the food with the specified name"));

        assertThrows(UnknownFoodException.class, () -> diary.addFood(Meal.BREAKFAST, "unknown food", 0.1),
                "Unknown food - Expected UnknownFoodException");
    }

    @Test
    void testAddFoodWithCorrectFood() throws UnknownFoodException {
        when(nutritionInfoAPIMock.getNutritionInfo("bread"))
                .thenReturn(new NutritionInfo(60, 20, 20));

        NutritionInfo nutritionInfo = nutritionInfoAPIMock.getNutritionInfo("bread");

        assertEquals(new FoodEntry("bread", 1, nutritionInfo),
                diary.addFood(Meal.BREAKFAST, "bread", 1));
    }

    @Test
    void testGetAllFoodEntriesEmptyCollection() {
        Collection<FoodEntry> expected = new ArrayList<>();
        assertIterableEquals(expected, diary.getAllFoodEntries(),
                "Expected empty collection");
    }

    @Test
    void testGetAllFoodEntriesExistingFoodEntries() throws UnknownFoodException {
        Set<FoodEntry> expected = new HashSet<>();
        FoodEntry food1 = new FoodEntry("Food1", 1.1, new NutritionInfo(20, 30, 50));
        FoodEntry food2 = new FoodEntry("Food2", 2.2, new NutritionInfo(30, 20, 50));
        FoodEntry food3 = new FoodEntry("Food3", 3.3, new NutritionInfo(50, 30, 20));
        expected.add(food1);
        expected.add(food2);
        expected.add(food3);

        when(nutritionInfoAPIMock.getNutritionInfo("Food1"))
                .thenReturn(new NutritionInfo(20, 30, 50));
        when(nutritionInfoAPIMock.getNutritionInfo("Food2"))
                .thenReturn(new NutritionInfo(30, 20, 50));
        when(nutritionInfoAPIMock.getNutritionInfo("Food3"))
                .thenReturn(new NutritionInfo(50, 30, 20));

        diary.addFood(Meal.BREAKFAST, "Food1", 1.1);
        diary.addFood(Meal.LUNCH, "Food2", 2.2);
        diary.addFood(Meal.DINNER, "Food3", 3.3);

        Collection<FoodEntry> actual = diary.getAllFoodEntries();
        assertTrue(expected.containsAll(actual) && actual.containsAll(expected),
                "Expected collection with 3 elements - Food1, Food2 and Food3");
    }

    @Test
    void testGetAllFoodEntriesByProteinContentEmptyCollection() {
        Collection<FoodEntry> expected = new ArrayList<>();
        assertIterableEquals(expected, diary.getAllFoodEntriesByProteinContent(),
                "Expected empty collection");
    }

    @Test
    void testGetAllFoodEntriesByProteinContentExistingFoodEntriesUnmodifiable() throws UnknownFoodException {
        List<FoodEntry> expected = new ArrayList<>();
        FoodEntry food1 = new FoodEntry("Food1", 1.1, new NutritionInfo(20, 30, 50)); // 55
        FoodEntry food2 = new FoodEntry("Food2", 2.2, new NutritionInfo(30, 50, 20)); // 44
        FoodEntry food3 = new FoodEntry("Food3", 3.3, new NutritionInfo(50, 20, 30)); // 99
        expected.add(food2);
        expected.add(food1);
        expected.add(food3);

//        expected.sort(new FoodEntryProteinContentComparator());

        when(nutritionInfoAPIMock.getNutritionInfo("Food1"))
                .thenReturn(new NutritionInfo(20, 30, 50));
        when(nutritionInfoAPIMock.getNutritionInfo("Food2"))
                .thenReturn(new NutritionInfo(30, 50, 20));
        when(nutritionInfoAPIMock.getNutritionInfo("Food3"))
                .thenReturn(new NutritionInfo(50, 20, 30));

        diary.addFood(Meal.BREAKFAST, "Food1", 1.1);
        diary.addFood(Meal.DINNER, "Food2", 2.2);
        diary.addFood(Meal.LUNCH, "Food3", 3.3);

        List<FoodEntry> actual = diary.getAllFoodEntriesByProteinContent();

        assertIterableEquals(expected, actual, "Expected sorted by protein content collection with 3 elements");
        assertThrows(UnsupportedOperationException.class, () -> actual.add(food1),
                "method getAllFoodEntriesByProteinContent() must return unmodifiable copy");
    }

    @Test
    void testGetDailyCaloriesIntakeWhenNoFoodIsAdded() {
        assertEquals(0.0, diary.getDailyCaloriesIntake(),
                "Expected 0 calories intake when no food has been added");
    }

    @Test
    void testGetDailyCaloriesIntakeWithDifferentFoods() throws UnknownFoodException {
        when(nutritionInfoAPIMock.getNutritionInfo("breakfast")).
                thenReturn(new NutritionInfo(20, 30, 50));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner1")).
                thenReturn(new NutritionInfo(50, 20, 30));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner2")).
                thenReturn(new NutritionInfo(30, 10, 60));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner3")).
                thenReturn(new NutritionInfo(80, 5, 15));

        diary.addFood(Meal.BREAKFAST, "breakfast", 1); // (4*20 + 9*30 + 4*50) * 1 = 550
        diary.addFood(Meal.DINNER, "dinner1", 1.1); // (4*50 + 9*20 + 4*30) * 1.1 = 550
        diary.addFood(Meal.DINNER, "dinner2", 2.2); // (4*30 + 9*10 + 4*60) * 2.2 = 990
        diary.addFood(Meal.DINNER, "dinner3", 3.3); // (4*80 + 9*5 + 4*15) * 3.3 = 1402.5
                                                                        //                             3492.5

        assertEquals(3492.5, diary.getDailyCaloriesIntake(),
                "Expected 3492.5 calories, but " + diary.getDailyCaloriesIntakePerMeal(Meal.DINNER) +
                        " was returned");
    }

    @Test
    void testGetDailyCaloriesIntakePerMealWhenMealIsNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> diary.getDailyCaloriesIntakePerMeal(null),
                "meal is null - Expected IllegalArgumentException");
    }

    @Test
    void testGetDailyCaloriesIntakePerMealWhenNoFoodIsAdded() {
        assertEquals(0.0, diary.getDailyCaloriesIntakePerMeal(Meal.DINNER),
                "Expected 0 calories intake when no food has been added");
    }

    @Test
    void testGetDailyCaloriesIntakePerMealWhenNoFoodIsAddedForDinner() throws UnknownFoodException {
        when(nutritionInfoAPIMock.getNutritionInfo("breakfast")).
                thenReturn(new NutritionInfo(20, 30, 50));

        diary.addFood(Meal.BREAKFAST, "breakfast", 2.3);

        assertEquals(0.0, diary.getDailyCaloriesIntakePerMeal(Meal.DINNER),
                "Expected 0 calories intake when no food has been added for dinner");
    }

    @Test
    void testGetDailyCaloriesIntakePerMealWithDifferentFoods() throws UnknownFoodException {
        when(nutritionInfoAPIMock.getNutritionInfo("breakfast")).
                thenReturn(new NutritionInfo(20, 30, 50));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner1")).
                thenReturn(new NutritionInfo(50, 20, 30));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner2")).
                thenReturn(new NutritionInfo(30, 10, 60));

        when(nutritionInfoAPIMock.getNutritionInfo("dinner3")).
                thenReturn(new NutritionInfo(80, 5, 15));

        diary.addFood(Meal.BREAKFAST, "breakfast", 1); // (4*20 + 9*30 + 4*50) * 1 = 550, but it is ignored
        diary.addFood(Meal.DINNER, "dinner1", 1.1); // (4*50 + 9*20 + 4*30) * 1.1 = 550
        diary.addFood(Meal.DINNER, "dinner2", 2.2); // (4*30 + 9*10 + 4*60) * 2.2 = 990
        diary.addFood(Meal.DINNER, "dinner3", 3.3); // (4*80 + 9*5 + 4*15) * 3.3 = 1402.5
                                                                       //                             2942.5

        assertEquals(2942.5, diary.getDailyCaloriesIntakePerMeal(Meal.DINNER),
                "Expected 2942.5 calories for dinner, but " + diary.getDailyCaloriesIntakePerMeal(Meal.DINNER) +
                " was returned");
    }
}
