package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

public class Chat {
    public final String id;

    public Chat(String id) {
        this.id = id;
    }

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
