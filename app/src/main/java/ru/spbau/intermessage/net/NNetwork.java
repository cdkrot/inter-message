package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.store.IStorage;

import java.io.IOException;

public interface NNetwork {
    public void begin(Messenger msg, IStorage store) throws IOException;
    public void create(String addr, ILogic logic) throws IOException;
    public void work() throws IOException;
    public void interrupt();
    public void close() throws IOException;
}
