package ru.spbau.intermessage.net;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;
import ru.spbau.intermessage.util.Tuple3;

// somebody asked us to give new messages.

// states:
// 0: I: Other side identifies, check
// 0: O: (chat, subid, id) to sync in, goto 1.
// 1: I: SKIP | GET
// 1: O: SKIP => (chat, subid, id), GET => Message, goto 2.
// 1: *: SKIP => Update storage.
// 2: I: ACK
// 2: O: (chat, subid, id)
// 2: *: Update storage.

public class Logic implements ILogic {
    private Messenger msg;
    private IStorage store;
    
    private int state = 0;
    private User user = null;

    private Chat lastchat = null;
    private User lastuser = null;
    private int lastid = -1;
    
    public Logic(Messenger msg_, IStorage store_) {
        msg = msg_;
        store = store_;
    }

    public ByteVector getNextTuple() {
        Tuple3<Chat, User, Integer> nextm = msg.getNextMessageFor(user);
        if (nextm == null)
            return null;

        lastchat = nextm.first;
        lastuser = nextm.second;
        lastid   = nextm.third;
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        lastchat.write(writer);
        lastuser.write(writer);
        writer.writeInt(lastid);

        return writer.getData();
    }

    @Nullable
    public ByteVector feed0(ByteVector packet) {
        // TODO: add security check here.
        ReadHelper reader = new ReadHelper(packet);
        
        user = User.read(reader);
        if (user == null || reader.available() != 0)
            return null;

        if (!msg.setBusy(user))
            return null;
        
        state = 1;
        return getNextTuple();
    }

    @Nullable
    public ByteVector feed1(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);

        String str = reader.readString();
        if (str == null || reader.available() != 0 || (!str.equals("GET") && !str.equals("SKIP")))
            return null;

        if (str.equals("SKIP")) {
            msg.sentMessageToParty(user, lastchat, lastuser, lastid);
            return getNextTuple();
        } else {
            Message m = msg.getMessageById(lastchat, lastuser, lastid);
            WriteHelper writer = new WriteHelper(new ByteVector());
            m.write(writer);
            state = 2;
            return writer.getData();
        }
    }

    @Nullable
    public ByteVector feed2(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);
        String str = reader.readString();
        if (str == null || reader.available() != 0 || !str.equals("ACK"))
            return null;

        msg.sentMessageToParty(user, lastchat, lastuser, lastid);
        state = 1;
        return getNextTuple();
    }

    @Override
    public ByteVector feed(ByteVector packet) {
        switch (state) {
        case 0: return feed0(packet);
        case 1: return feed1(packet);
        case 2: return feed2(packet);
        }
        return null;
    }

    @Override
    public void disconnect() {
        if (user != null)
            msg.setNotBusy(user);
    }
};
