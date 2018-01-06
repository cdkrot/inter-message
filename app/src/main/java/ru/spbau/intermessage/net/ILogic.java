package ru.spbau.intermessage.net;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.util.ByteVector;

public interface ILogic {
    /** return null to close or response to send.
     */
    @Nullable
    public ByteVector feed(ByteVector packet);

    /** connection is closed
     * (either by us or the other side).
     */
    public void disconnect();
};
