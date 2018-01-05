package ru.spbau.intermessage.net;

import ru.spbau.intermessage.util.ByteVector;

public interface ILogic {
    // return null to close or responce to send.
    public ByteVector feed(ByteVector packet);

    // connection is closed
    // (either by us or the other side).
    public void disconnect();
};
