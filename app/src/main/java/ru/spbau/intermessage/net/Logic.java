package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;

// somebody asked us to give new messages.

// states:
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
    
    private int state = 0;
    private User user = null;
    private Chat chat = null;

    private User askedUser = null;
    private int askedId = -1;
    
    public Logic(Messenger msg_, IStorage store_) {
        msg = msg_;
        store = store_;
    }

    public ByteVector feed0(ByteVector packet) {
        // TODO: add security check here.
        ReadHelper helper = new ReadHelper(packet);
        
        user = User.load(helper);
        if (user == null)
            return null;

        chat = Chat.load(helper);
        if (chat == null)
            return null;

        if (helper.available() > 0)
            return null;

        WriteHelper writer = new WriteHelper(new ByteVector());
        
        IStorage.IList list = store.getList("chat.info." + chat.id);
        writer.writeInt(list.size());
        for (int i = 0; i != list.size(); ++i) {
            IStorage.Union obj = list.get(i);

            ReadHelper unpacker = new ReadHelper(ByteVector.wrap(obj.getData()));
            User u = User.load(unpacker);
            int cnt = unpacker.readInt();

            u.write(writer);
            writer.write(cnt);
        }

        state = 1;
        
        return writer.data();
    }

    public ByteVector feed1(ByteVector packet) {
        ReadHelper reader = new ReadHelper(packet);

        askedUser = User.load(reader);
        if (askedUser == null)
            return null;

        if (reader.available() != 4)
            return null;

        askedId = reader.readInt();

        IStorage.IList list = store.getList("chat.msg." + chat.id + "." + askedUser.publickey);

        if (askedId >= list.size())
            return null;

        WriteHelper writer = new WriteHelper(new ByteVector());
        Message msg = Message.read(new ReadHelper(ByteVector.wrap(list.get(askedId).getBytes())));

        msg.write(writer);
        return writer.getData();
    }

    private const byte[] ACK = new byte[] {65, 67, 75};
    
    public ByteVector feed2(ByteVector packet) {
        if (packet.size() != ACK.length)
            return null;

        for (int i = 0; i != ACK.length; ++i)
            if (packet.get(i) != ACK[i])
                return null;

        // TODO: update info.
        
        state = 1;
        return ACK;
    }
    
    public ByteVector feed(ByteVector packet) {
        switch (state) {
        case 0: return feed0(packet);
        case 1: return feed1(packet);
        case 2: return feed2(packet);
        }
    }

    public void disconnect();
};
