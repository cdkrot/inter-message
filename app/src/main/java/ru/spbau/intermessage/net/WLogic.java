package ru.spbau.intermessage.net;

import ru.spbau.intermessage.core.User;

public interface WLogic extends ILogic {
    // called by wrapping logic to show the user with whom connection is.
    void setPeer(User u);
};
