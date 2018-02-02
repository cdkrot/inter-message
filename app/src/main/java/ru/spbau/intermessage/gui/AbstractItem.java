package ru.spbau.intermessage.gui;

public abstract class AbstractItem implements Item{
    private int position = 0;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
