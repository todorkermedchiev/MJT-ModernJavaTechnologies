package bg.sofia.uni.fmi.mjt.flightscanner.flight;

import java.util.Comparator;

public class FlightsByDestinationComparator implements Comparator<Flight> {
    @Override
    public int compare(Flight first, Flight second) {
        return first.getTo().ID().compareTo(second.getTo().ID());
    }
}
