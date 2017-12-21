package state.ge;

public class Margin {
    private final int minimum;
    private final int maximum;

    public Margin(int minimum, int maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }
}


