package bg.sofia.uni.fmi.mjt.flightscanner.flight;

import bg.sofia.uni.fmi.mjt.flightscanner.Validator;
import bg.sofia.uni.fmi.mjt.flightscanner.airport.Airport;
import bg.sofia.uni.fmi.mjt.flightscanner.exception.FlightCapacityExceededException;
import bg.sofia.uni.fmi.mjt.flightscanner.exception.InvalidFlightException;
import bg.sofia.uni.fmi.mjt.flightscanner.passenger.Passenger;

import java.util.*;

public class RegularFlight implements Flight {
    private String flightId;
    private Airport from;
    private Airport to;
    private int totalCapacity;
    Set<Passenger> passengers;

    private RegularFlight(String flightId, Airport from, Airport to, int totalCapacity) {
        Validator.validateString(flightId);
        Validator.validateObject(from);
        Validator.validateObject(to);
        Validator.validateInt(totalCapacity);

        if (from.equals(to)) {
            throw new InvalidFlightException("from and to can not be the same airport");
        }

        this.flightId = flightId;
        this.from = from;
        this.to = to;
        this.totalCapacity = totalCapacity;
        passengers = new HashSet<Passenger>();
    }

    public static RegularFlight of(String flightId, Airport from, Airport to, int totalCapacity) {
        return new RegularFlight(flightId, from, to, totalCapacity);
    }

    @Override
    public Airport getFrom() {
        return from;
    }

    @Override
    public Airport getTo() {
        return to;
    }

    @Override
    public void addPassenger(Passenger passenger) throws FlightCapacityExceededException {
        Validator.validateObject(passenger);
        checkIfCapacityReached();

        passengers.add(passenger);
    }

    @Override
    public void addPassengers(Collection<Passenger> passengers) throws FlightCapacityExceededException {
        checkIfCapacityReached();
        Validator.validateObject(passengers);

        if (passengers.size() > getFreeSeatsCount()) {
            throw new FlightCapacityExceededException("Flight capacity is reached");
        }

        this.passengers.addAll(passengers);
    }

    @Override
    public Collection<Passenger> getAllPassengers() {
        if (passengers.isEmpty()) {
            return new ArrayList<Passenger>();
        }
        return Set.copyOf(passengers);
    }

    @Override
    public int getFreeSeatsCount() {
        return totalCapacity - passengers.size();
    }

    private void checkIfCapacityReached() throws FlightCapacityExceededException {
        if (passengers.size() >= totalCapacity) {
            throw new FlightCapacityExceededException("Flight capacity is reached");
        }
    }
}
