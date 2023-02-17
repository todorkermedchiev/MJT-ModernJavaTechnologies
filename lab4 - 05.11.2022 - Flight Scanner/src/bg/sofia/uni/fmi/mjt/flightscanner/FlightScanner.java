package bg.sofia.uni.fmi.mjt.flightscanner;

import java.util.*;

import bg.sofia.uni.fmi.mjt.flightscanner.airport.Airport;
import bg.sofia.uni.fmi.mjt.flightscanner.exception.InvalidFlightException;
import bg.sofia.uni.fmi.mjt.flightscanner.flight.Flight;
import bg.sofia.uni.fmi.mjt.flightscanner.flight.FlightByFreeSeatsDescComparator;
import bg.sofia.uni.fmi.mjt.flightscanner.flight.FlightsByDestinationComparator;

public class FlightScanner implements FlightScannerAPI {
    Map<Airport, Set<Flight>> flights;

    public FlightScanner() {
        flights = new HashMap<Airport, Set<Flight>>();
    }

    @Override
    public void add(Flight flight) {
        Validator.validateObject(flight);

        if (!flights.containsKey(flight.getFrom())) {
            flights.put(flight.getFrom(), new HashSet<Flight>());
        }
        if (!flights.containsKey(flight.getTo())) {
            flights.put(flight.getTo(), new HashSet<Flight>());
        }

        flights.get(flight.getFrom()).add(flight);
    }

    @Override
    public void addAll(Collection<Flight> flights) {
        Validator.validateObject(flights);

        for (Flight flight : flights) {
            add(flight);
        }
    }

    @Override
    public List<Flight> searchFlights(Airport from, Airport to) {
        Validator.validateObject(from);
        Validator.validateObject(to);

        if (from.equals(to)) {
            throw new IllegalArgumentException("from and to are equal");
        }

        Queue<Flight> flightsQueue = new LinkedList<>();
        Set<Airport> visited = new HashSet<>();
        Map<Airport, Flight> cameFrom = new HashMap<>();

        Airport current = from;
        flightsQueue.add(null); // doesn't work with ArrayDeque
        cameFrom.put(from, null);
        visited.add(from);

        while (!current.equals(to)) {
            for (Flight flight : flights.get(current)) {
                if (!visited.contains(flight.getTo())) {
                    flightsQueue.add(flight);
                    visited.add(flight.getTo());
                }
            }

            flightsQueue.poll();

            // The destination is not reachable
            if (flightsQueue.isEmpty()) {
                return new ArrayList<>();
            }

            current = flightsQueue.peek().getTo();
            cameFrom.put(current, flightsQueue.peek());
        }

        if (cameFrom.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedList<Flight> result = new LinkedList<>();
        Flight cameWith = cameFrom.get(to);

        while (cameWith != null) {
            result.addFirst(cameWith);
            cameWith = cameFrom.get(cameWith.getFrom());
        }

        return result;
    }

    @Override
    public List<Flight> getFlightsSortedByFreeSeats(Airport from) {
        Validator.validateObject(from);

        if (!flights.containsKey(from) || flights.get(from).isEmpty()) {
            return new ArrayList<>();
        }

        List<Flight> result = new ArrayList<>(flights.get(from));
        result.sort(new FlightByFreeSeatsDescComparator());

        return List.copyOf(result);
    }

    @Override
    public List<Flight> getFlightsSortedByDestination(Airport from) {
        Validator.validateObject(from);

        if (!flights.containsKey(from) || flights.get(from).isEmpty()) {
            return new ArrayList<>();
        }

        List<Flight> result = new ArrayList<>(flights.get(from));
        result.sort(new FlightsByDestinationComparator());

        return List.copyOf(result);
    }
}
