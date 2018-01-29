package state.ge.utils;

public class Margin {
    private int minimum = -1;
    private int maximum = -1;

    private long marginTimeout = 2700000;
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

    public boolean areBothValid() {
        return isMaximumValid() && isMinimumValid();
    }

    public boolean isMinimumValid() {
        boolean valid = minimum != -1 && (minimumValidUntil == -1 || System.currentTimeMillis() < minimumValidUntil);
        if(!valid) {
            minimum = -1;
            minimumValidUntil = -1;
        }
        return valid;
    }

    public boolean isMaximumValid() {
        boolean valid = maximum != -1 && (maximumValidUntil == -1 || System.currentTimeMillis() < maximumValidUntil);
        if(!valid) {
            maximum = -1;
            maximumValidUntil = -1;
        }
        return valid;
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

    public void setValidUntil(long timestamp) {
        this.maximumValidUntil = timestamp;
        this.minimumValidUntil = timestamp;
    }

    public void reset() {
        maximumValidUntil = -1;
        minimumValidUntil = -1;
        maximum = -1;
        minimum = -1;
    }
}


