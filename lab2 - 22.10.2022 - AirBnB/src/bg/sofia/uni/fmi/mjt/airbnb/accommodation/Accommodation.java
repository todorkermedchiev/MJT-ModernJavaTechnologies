package bg.sofia.uni.fmi.mjt.airbnb.accommodation;

import bg.sofia.uni.fmi.mjt.airbnb.accommodation.location.Location;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class Accommodation implements Bookable{
    protected String id;
    private Location location;
    boolean isBooked;
    double pricePerNight;
    LocalDateTime checkIn;
    LocalDateTime checkOut;

    public Accommodation(Location location, double pricePerNight) {
        this.location = location;
        this.pricePerNight = pricePerNight;
        isBooked = false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isBooked() {
        return isBooked;
    }

    @Override
    public boolean book(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (isBooked || checkIn == null || checkOut == null ||
                checkIn.isBefore(LocalDateTime.now()) || !checkOut.isAfter(checkIn))
        {
            return false;
        }

        this.checkIn = checkIn;
        this.checkOut = checkOut;
        isBooked = true;
        return true;
    }

    @Override
    public double getTotalPriceOfStay() {
        if (!isBooked) {
            return 0.0;
        }
        return pricePerNight * checkIn.until(checkOut, ChronoUnit.DAYS);
    }

    @Override
    public double getPricePerNight() {
        return pricePerNight;
    }
}
