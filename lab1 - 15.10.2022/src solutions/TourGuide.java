public class TourGuide {
    public static int getBestSightseeingPairScore(int[] places) {
        int maxRating = 0;
        for (int i = 0; i < places.length; ++i) {
            for (int j = i + 1; j < places.length; ++j) {
                if (maxRating < places[i] + places[j] + i - j) {
                    maxRating = places[i] + places[j] + i - j;
                }
            }
        }

        return maxRating;
    }
}
