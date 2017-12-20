package ru.spbau.intermessage.core;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class Chat {
    public Chat() {}
    public Chat(String id_) {id = id_;}
    
    public String id;

    public static Chat read(ReadHelper helper) {
        String s = helper.readString();
        if (s == null)
            return null;
        return new Chat(s);
    }

    public void write(WriteHelper writer) {
        writer.writeString(id);
    }
}
