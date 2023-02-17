package bg.sofia.uni.fmi.mjt.airbnb;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.Bookable;
import bg.sofia.uni.fmi.mjt.airbnb.filter.Criterion;

public class Airbnb implements AirbnbAPI {
    private Bookable[] accommodations;

    public Airbnb(Bookable[] accommodations) {
        this.accommodations = accommodations;
    }

    @Override
    public Bookable findAccommodationById(String id) {
        if (id == null) {
            return null;
        }
        for (Bookable accommodation : accommodations) {
            if (id.equalsIgnoreCase(accommodation.getId())) {
                return accommodation;
            }
        }
        return null;
    }

    @Override
    public double estimateTotalRevenue() {
        double total = 0.0;
        for (Bookable accommodation : accommodations) {
            total += accommodation.getTotalPriceOfStay();
        }
        return total;
    }

    @Override
    public long countBookings() {
        long bookingsCount = 0;
        for (Bookable accommodation : accommodations) {
            if (accommodation.isBooked()) {
                ++bookingsCount;
            }
        }
        return bookingsCount;
    }

    private boolean checkIfSuitable(Criterion[] criteria, Bookable accommodation) {
        for (Criterion criterion : criteria) {
            if (!criterion.check(accommodation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Bookable[] filterAccommodations(Criterion... criteria) {
        int resultsCount = 0;
        for (Bookable accommodation : accommodations) {
            if (checkIfSuitable(criteria, accommodation)) {
                ++resultsCount;
            }
        }

        Bookable[] results = new Bookable[resultsCount];
        int counter = 0;
        for (Bookable accommodation : accommodations) {
            if (checkIfSuitable(criteria, accommodation)) {
                results[counter] = accommodation;
                ++counter;
            }
        }

        return results;
    }
}
