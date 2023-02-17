package bg.sofia.uni.fmi.mjt.flightscanner.flight;

import java.util.Comparator;

public class FlightByFreeSeatsDescComparator implements Comparator<Flight> {
    @Override
    public int compare(Flight first, Flight second) {
        return Integer.compare(second.getFreeSeatsCount(), first.getFreeSeatsCount());
    }
}
