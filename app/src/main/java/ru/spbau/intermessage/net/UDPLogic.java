
package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;

public class UDPLogic {
    private Messenger msg;
    private IStorage store;
    private long lastTM = 0;

    private long delta = 10 * 1000;
    private byte[] head = {85, 83, 69, 82};    
    
    public UDPLogic(Messenger msg_, IStorage store_) {
        msg = msg_;
        store = store_;
    }

    public ByteVector bcast() {
        if (lastTM + delta < System.currentTimeMillis())
            return null;
        
        lastTM = System.currentTimeMillis();
        
        WriteHelper writer = new WriterHelper(new ByteVector());
        
        writer.writeBytes(head);
        msg.identity.user().write(writer);

        for (String str: store.getMatching("users.poor."))
            writer.write(str);
        
        return writer.getData();
    }

    public void recieve(String from, ByteVector data) {
        // TODO: check identity.

        ReadHelper reader = new ReadHelper(data);
        if (!reader.skip(heap))
            return;

        User u = User.read(reader);
        if (u == null)
            return;

        boolean was = false;
        while (reader.available() > 0) {
            User xx = User.read(reader):
            if (xx == null)
                return;

            if (xx.equals(msg.identity.user()))
                was = true;
        }

        store.get("user.location." + u.publickey).setString(from);

        if (was)
            msg.syncWith(u);
    }
};
