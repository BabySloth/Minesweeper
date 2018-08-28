package src.main.java;

public enum Measurements {
    windowWidth(500),
    windowHeight(450),
    displayHeight(30),
    tileSide(40);

    private final double value;

    Measurements(double value) {
        this.value = value;
    }

    public double value(){
        return value;
    }
}
