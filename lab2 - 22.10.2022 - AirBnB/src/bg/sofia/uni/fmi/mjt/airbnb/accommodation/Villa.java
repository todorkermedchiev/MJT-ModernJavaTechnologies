package bg.sofia.uni.fmi.mjt.airbnb.accommodation;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.location.Location;

public class Villa extends Accommodation {
    private static int villasCounter = 0;

    public Villa(Location location, double pricePerNight) {
        super(location, pricePerNight);
        id = new String("VIL-" + villasCounter);
        ++villasCounter;
    }
}
