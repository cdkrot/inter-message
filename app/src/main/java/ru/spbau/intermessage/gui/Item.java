package ru.spbau.intermessage.gui;

public class Item {
    public String userName;
    public String messageText;
    public long date;
    public int position;

    public Item(String userName, String messageText, long date, int position) {
        this.userName = userName;
        this.messageText = messageText;
        this.date = date;
        this.position = position;
    }
};
