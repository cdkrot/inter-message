package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class Chat {
    public Chat(String id_) {id = id_;}
    
    public final String id;

    @Nullable
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
