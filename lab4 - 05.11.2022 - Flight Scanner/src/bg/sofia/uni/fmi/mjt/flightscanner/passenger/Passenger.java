package bg.sofia.uni.fmi.mjt.flightscanner.passenger;

import bg.sofia.uni.fmi.mjt.flightscanner.Validator;

public record Passenger(String id, String name, Gender gender) {
    public Passenger {
        Validator.validateString(id);
        Validator.validateString(name);
    }
}
