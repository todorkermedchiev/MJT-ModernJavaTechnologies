package bg.sofia.uni.fmi.mjt.airbnb.accommodation;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.location.Location;

public class Apartment extends Accommodation {
    private static int apartmentsCounter = 0;

    public Apartment(Location location, double pricePerNight) {
        super(location, pricePerNight);
        id = new String("APA-" + apartmentsCounter);
        ++apartmentsCounter;
    }
}
