package ru.spbau.intermessage.net;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.core.*;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

import java.security.interfaces.*;
import ru.spbau.intermessage.crypto.ID;

// encryption client logic.

// perform auth and switch to other logic.
// [0] Sent Pubkey
// [1] Get Pubkey + 512 bytes of random [encryption]
//     Sent this bytes & 512 new random [encryption]
// [2] (Recieve 512 new bytes and go to the bussiness) [encryption]
// [3] bussiness [encryption].

public class ECLogic implements ILogic {
    private Messenger msg;
    private User peer;
    private int state = 0;
    private WLogic logic;
    private RSAPublicKey peerKey;
    
    private byte[] toVerify;
    
    public ECLogic(WLogic logic, Messenger msg, User peer) {
        this.msg = msg;
        this.peer = peer;
        this.logic = logic;
    }

    private ByteVector feed0(ByteVector ig) {
        ++state;
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        msg.identity.writePubkey(writer);

        System.err.println(writer.getData());
        return writer.getData();
    }
    
    private ByteVector feed1(ByteVector packet) {
        ++state;

        packet = msg.identity.decode(packet);
        
        ReadHelper reader = new ReadHelper(packet);
        peerKey = ID.readPubkey(reader);

        if ((peerKey = ID.readPubkey(reader)) == null)
            return null;

        byte[] raw = peerKey.getEncoded();
        byte[] fingerprint = ID.getFingerprint(peerKey);

        if (reader.available() != 512 || !peer.equals(new User(fingerprint)) || !msg.checkFingerprint(fingerprint, raw))
            return null;
        
        WriteHelper writer = new WriteHelper(new ByteVector());
        for (int i = 0; i != 512; ++i)
            writer.writeByte(reader.readByte());

        toVerify = ID.getSecureRandom(512);
        for (byte b: toVerify)
            writer.writeByte(b);

        return ID.encode(peerKey, writer.getData());
    }

    public ByteVector feed2(ByteVector packet) {
        ++state;
        
        packet = msg.identity.decode(packet);

        if (packet == null || packet.size() != 512)
            return null;

        for (int i = 0; i != 512; ++i)
            if (toVerify[i] != packet.get(i))
                return null;

        // Connection established.
        logic.setPeer(peer);
        return ID.encode(peerKey, logic.feed(null));
    }

    public ByteVector feed3(ByteVector packet) {
        packet = msg.identity.decode(packet);
        if (packet == null)
            return null;
        return ID.encode(peerKey, logic.feed(packet));
    }
    
    public ByteVector feed(ByteVector packet) {
        logic.setPeer(peer);
        return logic.feed(packet);
        
        // System.err.println("ECLogic" + state);
        // switch (state) {
        // case 0: return feed0(packet);
        // case 1: return feed1(packet);
        // case 2: return feed2(packet);
        // case 3: return feed3(packet);
        // }
        // return null;
    }

    public void disconnect() {
        msg.setNotBusy(peer);
        logic.disconnect();
    }
};
