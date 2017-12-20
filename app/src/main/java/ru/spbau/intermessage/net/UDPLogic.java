
package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.store.IStorage;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.WriteHelper;
import ru.spbau.intermessage.util.ReadHelper;

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
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        
        writer.writeBytes(head);
        msg.identity.user().write(writer);

        for (User u: msg.getPoor())
            u.write(writer);
        
        return writer.getData();
    }

    public void recieve(String from, ByteVector data) {
        // TODO: check identity.

        ReadHelper reader = new ReadHelper(data);
        if (!reader.skip(head))
            return;

        User u = User.read(reader);
        if (u == null)
            return;

        boolean was = false;
        while (reader.available() > 0) {
            User xx = User.read(reader);
            if (xx == null)
                return;

            if (xx.equals(msg.identity.user()))
                was = true;
        }

        msg.setUserLocation(u, from);

        if (was)
            msg.syncWith(u);
    }
};
