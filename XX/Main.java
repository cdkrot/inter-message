import ru.spbau.intermessage.core.*;
import ru.spbau.intermessage.util.*;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.store.InMemoryStorage;

import java.util.*;

public class Main {
    public static void sleep(int i) {
        try {
            Thread.currentThread().sleep(i);
        } catch (InterruptedException ex) {
        }
    }
    
    public static void main(String[] args) {
        System.err.println("#1");
        InMemoryStorage store = new InMemoryStorage();
        Messenger msg = new Messenger(store, ID.create());
        System.err.println("#2");
        sleep(5000);
        System.err.println("#3");

        Chat ch = null;
        if (args.length >= 2) {
            System.err.println("#4");
            
            ArrayList<User> scary = new ArrayList<User>();
            scary.add(new User(args[1]));
            
            ch = msg.createChat("the chat", scary);

            System.err.println("#5");
        }

        System.err.println("#6");
        
        int i = 0;
        while (true) {
            sleep(2000);

            if (ch != null) {
                ++i;
                msg.sendMessage(ch, new Message("text", 123, Util.stringToBytes("Ping " + i)));
            }
        }
    }
}
