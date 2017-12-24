package state.ge;

public class Margin {
    private int minimum = -1;
    private int maximum = -1;

    private long marginTimeout = 1800000;
    private long maximumValidUntil = -1;
    private long minimumValidUntil = -1;

    public Margin() {}

    public Margin(int minimum, int maximum) {
        setMinimum(minimum);
        setMaximum(maximum);
    }

    public Margin(int minimum, int maximum, long marginTimeout) {
        this.marginTimeout = marginTimeout;
        setMinimum(minimum);
        setMaximum(maximum);
    }

    public boolean minimumValid() {
        return marginTimeout == -1 || System.currentTimeMillis() < minimumValidUntil;
    }

    public boolean maximumValid() {
        return marginTimeout == -1 || System.currentTimeMillis() < maximumValidUntil;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
        this.minimumValidUntil = System.currentTimeMillis() + marginTimeout;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
        this.maximumValidUntil = System.currentTimeMillis() + marginTimeout;
    }

    public void setMarginTimeout(long marginTimeout) {
        this.maximumValidUntil += marginTimeout - this.marginTimeout;
        this.minimumValidUntil += marginTimeout - this.marginTimeout;
        this.marginTimeout = marginTimeout;

    }
}


