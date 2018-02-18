package ru.spbau.intermessage.net;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.core.*;

import ru.spbau.intermessage.util.ByteVector;
import ru.spbau.intermessage.util.ReadHelper;
import ru.spbau.intermessage.util.WriteHelper;

import java.security.interfaces.*;
import ru.spbau.intermessage.crypto.ID;

// encryption server logic.

// perform auth and switch to other logic.
// [0] Get Pubkey [no encryption] -> Sent Pubkey + 512 bytes of random [encryption]
// [1] Recv this bytes + 512 new [encryption] -> sent this bytes [encryption]
// [2] bussiness [encryption] -> bussiness [encryption].


public class ESLogic implements ILogic {
    private Messenger msg;
    private User peer;
    private int state = 0;
    private WLogic logic;
    private RSAPublicKey peerKey;
    
    private byte[] toVerify;

    private boolean peerSet = false;
    
    public ESLogic(WLogic logic, Messenger msg) {
        this.msg = msg;
        this.logic = logic;
    }

    private ByteVector feed0(ByteVector packet) {
        ++state;

        ReadHelper reader = new ReadHelper(packet);

        if ((peerKey = ID.readPubkey(reader)) == null)
            return null;

        byte[] raw = peerKey.getEncoded();
        byte[] fingerprint = ID.getFingerprint(peerKey);
        peer = new User(fingerprint);
        
        if (reader.available() != 0 || !msg.checkFingerprint(fingerprint, raw) || !msg.setBusy(peer))
            return null;

        WriteHelper writer = new WriteHelper(new ByteVector());
        msg.identity.writePubkey(writer);
        
        toVerify = ID.getSecureRandom(512);
        for (int i = 0; i != 512; ++i)
            writer.writeByte(toVerify[i]);

        return ID.encode(peerKey, writer.getData());
    }
    
    private ByteVector feed1(ByteVector packet) {
        ++state;

        packet = msg.identity.decode(packet);
        if (packet == null)
            return null;
        
        ReadHelper reader = new ReadHelper(packet);
        if (reader.available() != 1024)
            return null;

        for (int i = 0; i != 512; ++i)
            if (toVerify[i] != reader.readByte())
                return null;

        WriteHelper writer = new WriteHelper(new ByteVector());
        for (int i = 0; i != 512; ++i)
            writer.writeByte(reader.readByte());
        
        return ID.encode(peerKey, writer.getData());
    }

    public ByteVector feed2(ByteVector packet) {
        if ((packet = msg.identity.decode(packet)) == null)
            return null;

        if (!peerSet) {
            peerSet = true;
            logic.setPeer(peer);
        }
        
        return ID.encode(peerKey, logic.feed(packet));
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
        if (peer != null)
            msg.setNotBusy(peer);
        logic.disconnect();
    }
};
