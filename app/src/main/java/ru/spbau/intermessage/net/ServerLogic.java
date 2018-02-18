package ru.spbau.intermessage.net;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.core.*;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

// ask to give us new messages.
// states:
// 0: I: Nothing
// 0: O: "SYNC".
// 1: I: (chat, owner, id)
// 1: O: respond with "SKIP" (goto 1) or "GET" (goto 2)
// 2: I: Message
// 2: O: respond with "ACK", goto 1.

public class ServerLogic implements WLogic {
    private Messenger msg;
    private int state = 0;

    private Chat chat;
    private User owner;
    private int subid;

    private User peer;
    
    public ServerLogic(Messenger msg) {
        this.msg = msg;
    }

    public void setPeer(User u) {
        peer = u;
    }

    public ByteVector feed0(ByteVector packet) {
        ++state;
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        writer.writeString("SYNC");
        return writer.getData();
    }

    @Nullable
    public ByteVector feed1(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);

        chat = Chat.read(reader);
        if (chat == null)
            return null;
        
        owner = User.read(reader);
        if (owner == null)
            return null;

        if (reader.available() != 4)
            return null;

        subid = reader.readInt();

        int r = msg.needMessage(chat, owner, subid);
        if (r == -1) {
            return null; // invalid.
        } else {
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

    @Nullable
    public ByteVector feed2(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);
        Message m = Message.read(reader);

        if (msg == null || reader.available() > 0 || !msg.registerMessage(chat, owner, subid, m)) {
            return null;
        } else {
            WriteHelper writer = new WriteHelper(new ByteVector());
            writer.writeString("ACK");
            state = 1;
            return writer.getData();
        }
    }

    @Nullable
    public ByteVector feed(ByteVector packet) {
        ByteVector res = null;

        switch (state) {
            case 0: res = feed0(packet);
            break;
            case 1: res = feed1(packet);
            break;
            case 2: res = feed2(packet);
            break;
        }

        if (res == null)
            disconnect();

        return res;
    }

    public void disconnect() {
    }
};
