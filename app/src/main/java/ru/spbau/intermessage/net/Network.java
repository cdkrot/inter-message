package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.store.IStorage;

import java.io.IOException;

public interface Network {
    void begin(Messenger msg, IStorage store) throws IOException;
    void create(String addr, ILogic logic) throws IOException;
    void work() throws IOException;
    void interrupt();
    void close() throws IOException;
}
