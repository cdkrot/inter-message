package ru.spbau.intermessage.net;

import ru.spbau.intermessage.util.ByteVector;

public interface Network {
    public static interface Callback {
         public void completed(boolean suceeded);
    };
    
    public static interface IncomeListener {
        public void recieved(String from, boolean bcast, ByteVector dta);
    };

    public void open(IncomeListener listener);

    public void send(String address, ByteVector dta, Callback call);
    public void close();
}
