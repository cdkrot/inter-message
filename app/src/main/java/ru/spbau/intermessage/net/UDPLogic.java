
package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.*;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.WriteHelper;
import ru.spbau.intermessage.util.ReadHelper;

public class UDPLogic {
    private Messenger msg;
    private long lastTM = 0;

    private static final long DELTA = 3 * 1000;
    private static final byte[] HEAD = {85, 83, 69, 82};
    
    public UDPLogic(Messenger msg_) {
        msg = msg_;

        lastTM = System.currentTimeMillis();
    }

    public ByteVector bcast() {
        if (lastTM + DELTA > System.currentTimeMillis())
            return null;
        
        lastTM = System.currentTimeMillis();
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        
        writer.writeBytesSimple(HEAD);
        msg.identity.user().write(writer);
        writer.writeString(msg.doGetUserName(msg.identity.user()));
        
        for (User u: msg.getPoor())
            u.write(writer);
        
        return writer.getData();
    }

    public void recieve(String from, ByteVector data) {
        // TODO: check identity.

        ReadHelper reader = new ReadHelper(data);
        
        if (!reader.skip(HEAD))
            return;
        
        User u = User.read(reader);
        String name = reader.readString();
        
        if (u == null || name == null)
            return;
        
        boolean was = false;
        while (reader.available() > 0) {
            User xx = User.read(reader);
            if (xx == null)
                return; // invalid packet.

            if (xx.equals(msg.identity.user()))
                was = true;
        }

        msg.doSetUserName(u, name);
        msg.doSetUserLocation(u, from);
        
        if (was)
            msg.syncWith(u);
    }
};
