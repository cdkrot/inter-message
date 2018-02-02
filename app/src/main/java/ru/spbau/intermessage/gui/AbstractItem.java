package ru.spbau.intermessage.gui;

public abstract class AbstractItem implements Item{
    private int position = 0;

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
