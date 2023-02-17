package bg.sofia.uni.fmi.mjt.airbnb.accommodation;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.location.Location;

public class Hotel extends Accommodation{
    private static int hotelsCounter = 0;

    public Hotel(Location location, double pricePerNight) {
        super(location, pricePerNight);
        id = new String("HOT-" + hotelsCounter);
        ++hotelsCounter;
    }
}
