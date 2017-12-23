package ru.spbau.intermessage.core;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.net.*;

import ru.spbau.intermessage.util.*;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.store.IStorage;
import ru.spbau.intermessage.store.InMemoryStorage;

import java.util.*;

public class Messenger extends ServiceCommon {
    public Messenger(IStorage store, String tmp) {
        storage = store;
        identity = ID.create(tmp);
    }
    
    private static class ListenerRequest extends RequestCommon {
        public ListenerRequest(boolean a, EventListener l) {
            add = a;
            listener = l;
        }
        
        public boolean add;
        public EventListener listener;
    };

    private static class SendMessageRequest extends RequestCommon {
        public SendMessageRequest(Chat ch, Message msg) {
            chat = ch;
            message = msg;
        }
        
        public Chat chat;
        public Message message;
    }

    private static class ChatCreateRequest extends RequestCommon {
        public ChatCreateRequest(ArrayList<User> lst) {
            users = lst;
        }
        
        public Chat result;
        public ArrayList<User> users;
    }

    
    protected Set<EventListener> listeners = new HashSet<EventListener>();
    protected NNetwork network;
    public final ID identity;
    protected IStorage storage;

    protected void special() {
        network.work();
    }

    protected void interrupt() {
        network.interrupt();
        super.interrupt();
    }
    
    protected void handleRequest(RequestCommon req) {
        System.out.println(req.toString());
        
        if (req instanceof ListenerRequest) {
            ListenerRequest reqc = (ListenerRequest)req;
            if (reqc.add)
                listeners.add(reqc.listener);
            else
                listeners.remove(reqc.listener);
        }

        if (req instanceof SendMessageRequest) {
            System.out.println("Messenger: trying to send");
            SendMessageRequest reqc = (SendMessageRequest)req;

            doSendMessage(reqc.chat, reqc.message);
        }

        if (req instanceof ChatCreateRequest) {
            System.out.println("Messenger: creating chat");
            ChatCreateRequest reqc = (ChatCreateRequest)req;

            reqc.result = doCreateChat(reqc.users);
        }
    }
    
    protected void onMessage(User chat, User user, Message msg) {
        synchronized (this) {
            for (EventListener listener: listeners)
                listener.onMessage(chat, user, msg);
        }
    }
    
    protected void warmUp() {
        network = new WifiNNetwork();
        network.begin(this, storage);
    }
    
    protected void onClose() {
        network.close();
    }
    
    public void registerEventListener(EventListener listener) {
        postRequest(new ListenerRequest(true, listener));
    }
    
    public void deleteEventListener(EventListener listener) {
        postRequest(new ListenerRequest(false, listener));
    }
    
    public void sendMessage(Chat chat, Message message) {
        postRequest(new SendMessageRequest(chat, message));
    }

    public Chat createChat(ArrayList<User> users) {
        ChatCreateRequest req = new ChatCreateRequest(users);
        postRequest(req);
        req.waitCompletion();
        return req.result;
    }
    
    public Message[] getMessagesFromChat(Chat chat, Object restrictions) {
        throw new UnsupportedOperationException("TODO");
    }

    /*** INTERNAL, DO NOT USE */
    
    public ArrayList<Chat> getChatsWithUser(User u) {
        ArrayList<Chat> list = new ArrayList<Chat>();
        
        for (String str: storage.getMatching("chatswith." + u.publicKey + "."))
            list.add(new Chat(str.substring(("chatswith." + u.publicKey + ".").length())));
        return list;
    }

    public ArrayList<User> getChatMembers(Chat chat) {
        ArrayList<User> list = new ArrayList<User>();

        for (String str: storage.getMatching("chatmembers." + chat.id + "."))
            list.add(new User(str.substring(("chatmembers." + chat.id + ".").length())));
        return list;
    }
    
    public void recalcNeedsSync(User u) {
        storage.get("user.poor." + u.publicKey).setNull();

        for (Chat chat: getChatsWithUser(u)) {
            for (User member: getChatMembers(chat)) {
                if (storage.getList("msg." + chat.id + "." + member.publicKey).size() >
                    storage.get("info." + u.publicKey + "." + chat.id + "." + member.publicKey).getInt()) {
                    
                    storage.get("user.poor." + u.publicKey).setInt(1);
                    return;
                }
            }
        }
    }

    public Tuple3<Chat, User, Integer> getNextMessageFor(User u) {
        storage.get("user.poor." + u.publicKey).setNull();

        for (Chat chat: getChatsWithUser(u)) {
            for (User member: getChatMembers(chat)) {
                int we = storage.getList("msg." + chat.id + "." + member.publicKey).size();
                int they = storage.get("info." + u.publicKey + "." + chat.id + "." + member.publicKey).getInt();
                if (we > they) {
                    return new Tuple3<Chat, User, Integer>(chat, member, they);
                }
            }
        }
        return null;
    }

    private HashSet<String> blad = new HashSet<String>();
    
    public void syncWith(User u) {
        if (blad.contains(u.publicKey))
            return;

        blad.add(u.publicKey);
        network.create(storage.get("user.location." + u.publicKey).getString(), new SLogic(this, storage));
    }

    public void sentMessageToParty(User u, Chat ch, User sub, int id) {
        storage.get("info." + u.publicKey + "." + ch.id + "." + sub.publicKey).setInt(id + 1);
        recalcNeedsSync(u);
    }

    public boolean registerMessage(Chat ch, User u, int id, Message m) {
        System.err.println("Recieved new(?) message " + m.type);

        int len = storage.getList("msg." + ch.id + "." + u.publicKey).size();
        if (id > len || id < 0)
            return false;
        if (id == len) {
            WriteHelper writer = new WriteHelper(new ByteVector());
            m.write(writer);

            storage.getList("msg." + ch.id + "." + u.publicKey).push(writer.getData().toBytes());

            for (User member: getChatMembers(ch))
                recalcNeedsSync(member);
            
            return true;
        }
        return true;
    }
    
    public int needMessage(Chat ch, User u, int i) {
        int len = storage.getList("msg." + ch.id + "." + u.publicKey).size();
        if (i == len)
            return 1; // yep
        if (i > len || i < 0)
            return -1; // wtf
        return 0; // don't need.
    }

    protected void doSendMessage(Chat ch, Message msg) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        msg.write(writer);

        storage.getList("msg." + ch.id + "." + identity.user().publicKey).push(writer.getData().toBytes());

        System.err.println("tried to send message ");
        
        for (User u: getChatMembers(ch)) {
            storage.get("user.poor." + u.publicKey).setInt(1);
        }
    }
    
    public Chat doCreateChat(ArrayList<User> users) {
        String id = "" + (System.currentTimeMillis() % 1000);

        for (User u: users)
            storage.get("chatmembers." + id + "." + u.publicKey).setInt(1);
        
        doSendMessage(new Chat(id), new Message("!/chatcreated", 100500, Util.stringToBytes("chat created")));

        return new Chat(id);
    }
    
    public ArrayList<User> getPoor() {
        ArrayList<User> res = new ArrayList<User>();
        for (String s: storage.getMatching("user.poor."))
            res.add(new User(s.substring("user.poor.".length())));
        return res;
    }
    
    public Message getMessageById(Chat ch, User u, int id) {
        byte[] data = storage.getList("msg." + ch.id + "." + u.publicKey).get(id).getData();
        
        ReadHelper reader = new ReadHelper(ByteVector.wrap(data));
        return Message.read(reader);
    }

    public void setUserLocation(User u, String addr) {
        storage.get("user.location." + u.publicKey).setString(addr);
    }

    public String getUserLocation(User u) {
        IStorage.Union obj = storage.get("user.location." + u.publicKey);

        if (obj.getType() != IStorage.ObjectType.STRING)
            return null;
        return obj.getString();
    }
};
