package bg.sofia.uni.fmi.mjt.flightscanner;

public class Validator {
    public static void validateString(String string) {
        if (string == null || string.isEmpty() || string.isBlank()) {
            throw new IllegalArgumentException("Invalid string");
        }
    }

    public static void validateObject(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Object can not be null");
        }
    }

    public static void validateInt(int num) {
        if (num < 0) {
            throw new IllegalArgumentException("The value can not be negative");
        }
    }
}
