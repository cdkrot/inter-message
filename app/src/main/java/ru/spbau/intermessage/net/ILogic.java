package ru.spbau.intermessage.net;

import ru.spbau.intermessage.util.ByteVector;

public interface ILogic {
    // return null to close or responce to send.
    public ByteVector feed(ByteVector packet);

    // got disconnected.
    // trivial implementation should be fine.
    public void disconnect();
};
