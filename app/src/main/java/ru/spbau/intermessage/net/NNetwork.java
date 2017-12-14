package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.store.IStorage;

public interface NNetwork {
    public void begin(Messenger msg, IStorage store);
    public void work();
    public void interrupt();
    public void close();
}
