package state.ge.slots;

public abstract class Slot {

    protected int position;

    public Slot(int position) {
        this.position = position;
    }

    public int getPosition() {
        return this.position;
    }
}

