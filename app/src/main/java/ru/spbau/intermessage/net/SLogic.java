package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

// ask to give us new messages.
// states:
// 0: I: nothing
// 0: O: write our id.
// 1: I: (chat, subid, id)
// 1: O: respond with "SKIP" (goto 1) or "GET" (goto 2)
// 2: I: Message
// 2: O: respond with "ACK", goto 1.

public class SLogic implements ILogic {
    private Messenger msg;
    private IStorage store;
    private int state = 0;

    private Chat chat;
    private User subid;
    private int  subsubid;

    private User peer;
    
    public SLogic(Messenger msg_, IStorage store_, User peer_) {
        msg = msg_;
        store = store_;
        peer = peer_;
    }

    public ByteVector feed0(ByteVector packet) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        msg.identity.user().write(writer);

        state = 1;
        return writer.getData();
    }

    public ByteVector feed1(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);

        chat = Chat.read(reader);
        if (chat == null)
            return null;
        
        subid = User.read(reader);
        if (subid == null)
            return null;

        if (reader.available() != 4)
            return null;

        subsubid = reader.readInt();

        int r = msg.needMessage(chat, subid, subsubid);
        if (r == -1)
            return null; // invalid.
        else {
            WriteHelper writer = new WriteHelper(new ByteVector());
            if (r == 0)
                writer.writeString("SKIP");
            else {
                writer.writeString("GET");
                state = 2;
            }

            return writer.getData();
        }
    }

    public ByteVector feed2(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);
        Message m = Message.read(reader);

        if (msg == null || reader.available() > 0 || !msg.registerMessage(chat, subid, subsubid, m))
            return null;
        else {
            WriteHelper writer = new WriteHelper(new ByteVector());
            writer.writeString("ACK");
            state = 1;
            return writer.getData();
        }
    }
    
    public ByteVector feed(ByteVector packet) {
        switch (state) {
        case 0: return feed0(packet);
        case 1: return feed1(packet);
        case 2: return feed2(packet);
        }
        return null;
    }

    public void disconnect() {
        msg.setNotBusy(peer);
    }
};
