package bg.sofia.uni.fmi.mjt.airbnb.filter;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.Bookable;

public class PriceCriterion implements Criterion {
    private double minPrice;
    private double maxPrice;

    public PriceCriterion(double minPrice, double maxPrice) {
        this.maxPrice = maxPrice;
        this.minPrice = minPrice;
    }

    @Override
    public boolean check(Bookable bookable) {
        if (bookable == null) {
            return false;
        }
        return minPrice <= bookable.getPricePerNight() && maxPrice >= bookable.getPricePerNight();
    }
}
