package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;

// asked to give us new messages.
// 0: Other side identifies and sends specific chat ID.
// OUT: Send pairs of (user, cnt) we have
// 1: Other side asks for (user, subid)
// OUT: Send requested message.
// 2: Other side acks
// OUT: Send ack as well.
// STORAGE: update known information about this party.
// goto 1.


public class Logic implements ILogic {
    private Messenger msg;
    private IStorage store;
    
    public Logic(Messenger msg_, IStorage store_) {
        msg = msg_;
        store = store_;
    }

    public ByteVector feed(ByteVector packet) {
    
    }

    public void disconnect();
};
