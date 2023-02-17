package bg.sofia.uni.fmi.mjt.flightscanner.airport;

import bg.sofia.uni.fmi.mjt.flightscanner.Validator;

import java.util.Objects;

public record Airport(String ID) {
    public Airport {
        Validator.validateString(ID);
    }
}
