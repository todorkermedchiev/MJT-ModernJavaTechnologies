package bg.sofia.uni.fmi.mjt.airbnb.accommodation.location;

public class Location {
    private double x;
    private double y;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distance(Location other) {
        double xDiff = Math.abs(other.x - x);
        double yDiff = Math.abs(other.y - y);
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }
}
