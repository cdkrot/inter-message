package ru.spbau.intermessage.core;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.net.*;

import ru.spbau.intermessage.util.*;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.store.IStorage;
import ru.spbau.intermessage.store.InMemoryStorage;

import java.util.*;
import java.util.function.*;

public class Messenger extends ServiceCommon {
    public Messenger(IStorage store, ID id) {
        storage = store;
        identity = id;
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
    
    protected void onMessage(Chat chat, User user, Message msg) {
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

    public Chat createChat(String chatname, ArrayList<User> users) {
        ChatCreateRequest req = new ChatCreateRequest(users);
        postRequest(req);
        req.waitCompletion();
        return req.result;
    }

    public void changeUserName(String newname) {
        postRequest(new RunnableRequest() {
                public void run() {
                    doSetUserName(identity.user(), newname);
                }
            });
    }

    public void getListOfChats(Consumer<List<Pair<String, Chat>>> callback) {
        postRequest(new RunnableRequest() {
                public void run() {
                    List<Pair<String, Chat>> res = new ArrayList<Pair<String, Chat>>();
                    for (Chat ch: getChatsWithUser(identity.user()))
                        res.add(new Pair(doGetChatName(ch), ch));

                    callback.accept(res);
                }
            });
    }

    public void getLastMessages(Chat chat, int limit, BiConsumer<Integer, List<Tuple3<User, String, Message>>> callback) {
        postRequest(new RunnableRequest() {
                public void run() {
                    int total = doGetMessageCount(chat);
                    int since = Math.max(0, total - limit);
                    
                    List<Tuple3<User, String, Message>> lst = new ArrayList<Tuple3<User, String, Message>>();
                    for (int i = since; i != total; ++i) {
                        Pair<User, Message> res = doGetMessage(chat, i);

                        lst.add(new Tuple3<User, String, Message>(res.first, doGetUserName(res.first), res.second));
                    }
                    
                    callback.accept(total, lst);
                }
            });
    }

    public void getMessagesSince(Chat chat, int from, int limit, Consumer<List<Tuple3<User, String, Message>>> callback) {
        postRequest(new RunnableRequest() {
                public void run() {
                    int total = doGetMessageCount(chat);
                    int until = Math.min(total, from + limit);
                    
                    List<Tuple3<User, String, Message>> lst = new ArrayList<Tuple3<User, String, Message>>();
                    for (int i = from; i != until; ++i) {
                        Pair<User, Message> res = doGetMessage(chat, i);

                        lst.add(new Tuple3<User, String, Message>(res.first, doGetUserName(res.first), res.second));
                    }
                    
                    callback.accept(lst);
                }
            });
    }

    public void addUserToChat(Chat chat, User user) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        user.write(writer);
        sendMessage(chat, new Message("!adduser", 228410, writer.getData().toBytes()));
    }

    public List<Pair<User, String>> getUsersNearby() {
        List<Pair<User, String>> res = new ArrayList<Pair<User, String>>();
        
        RunnableRequest r = new RunnableRequest() {
                public void run() {
                    for (String str: storage.getMatching("user.location.")) {
                        User u = new User(str.substring("user.location.".length()));
                        res.add(new Pair<User, String>(u, doGetUserName(u)));
                    }
                }
            };

        postRequest(r);
        r.waitCompletion();
        
        return res;
    }

    public List<Pair<User, String>> getUsersInChat(Chat chat) {
        List<Pair<User, String>> res = new ArrayList<Pair<User, String>>();
        
        RunnableRequest r = new RunnableRequest() {
                public void run() {
                    for (User u: getChatMembers(chat))
                        res.add(new Pair(u, doGetUserName(u)));
                }
            };

        postRequest(r);
        r.waitCompletion();
    
        return res;
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
                int we = storage.getList("msg." + chat.id + "." + member.publicKey).size();
                IStorage.Union handle = storage.get("info." + u.publicKey + "." + chat.id + "." + member.publicKey);
                int they = (handle.getType() == IStorage.ObjectType.INTEGER ? handle.getInt() : 0);
                
                if (we > they) {
                    storage.get("user.poor." + u.publicKey).setInt(1);
                    return;
                }
            }
        }
    }

    public Tuple3<Chat, User, Integer> getNextMessageFor(User u) {
        for (Chat chat: getChatsWithUser(u)) {
            for (User member: getChatMembers(chat)) {
                int we = storage.getList("msg." + chat.id + "." + member.publicKey).size();
                IStorage.Union handle = storage.get("info." + u.publicKey + "." + chat.id + "." + member.publicKey);
                int they = (handle.getType() == IStorage.ObjectType.INTEGER ? handle.getInt() : 0);
                if (we > they) {
                    return new Tuple3<Chat, User, Integer>(chat, member, they);
                }
            }
        }
        return null;
    }

    private HashSet<String> busy = new HashSet<String>();

    public boolean setBusy(User u) {
        if (busy.contains(u.publicKey))
            return false;

        busy.add(u.publicKey);
        return true;
    }

    public void setNotBusy(User u) {
        busy.remove(u.publicKey);
    }
    
    public void syncWith(User u) {
        if (!setBusy(u))
            return;
        network.create(storage.get("user.location." + u.publicKey).getString(), new SLogic(this, storage, u));
    }
    
    public void sentMessageToParty(User u, Chat ch, User sub, int id) {
        storage.get("info." + u.publicKey + "." + ch.id + "." + sub.publicKey).setInt(id + 1);
        recalcNeedsSync(u);
    }

    public boolean registerMessage(Chat ch, User u, int id, Message m) {
        System.err.println("Recieved new(?) message " + m.type);
        
        if (storage.get("chats." + ch.id).getType() != IStorage.ObjectType.STRING)
            storage.get("chats." + ch.id).setString("new chat");
        
        int len = storage.getList("msg." + ch.id + "." + u.publicKey).size();
        if (id > len || id < 0)
            return false;
        if (id == len) {
            WriteHelper writer = new WriteHelper(new ByteVector());
            m.write(writer);

            storage.getList("msg." + ch.id + "." + u.publicKey).push(writer.getData().toBytes());

            u.write(writer);
            storage.getList("allmsg." + ch.id).push(writer.getData().toBytes());
            
            for (User member: getChatMembers(ch))
                recalcNeedsSync(member);

            if (m.type.equals("!newchat") || m.type.equals("!useradd")) {
                ReadHelper reader = new ReadHelper(ByteVector.wrap(m.data));
                User xx;
                while ((xx = User.read(reader)) != null) {
                    storage.get("chatmembers." + ch.id + "." + xx.publicKey).setInt(1);
                    storage.get("chatswith." + xx.publicKey + "." + ch.id).setInt(1);
                }
            }
            
            return true;
        }
        return true;
    }

    public int doGetMessageCount(Chat ch) {
        return storage.getList("allmsg." + ch.id).size();
    }
    
    public Pair<User, Message> doGetMessage(Chat ch, int id) {
        ReadHelper reader = new ReadHelper(ByteVector.wrap(storage.getList("allmsg." + ch.id).get(id).getData()));

        Message m = Message.read(reader);
        User u = User.read(reader);
        return new Pair(u, m);
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

        registerMessage(ch, identity.user(), storage.getList("msg." + ch.id + "." + identity.user().publicKey).size(), msg);
        
        for (User u: getChatMembers(ch)) {
            storage.get("user.poor." + u.publicKey).setInt(1);
        }
    }
    
    public Chat doCreateChat(ArrayList<User> users) {
        String id = "" + (System.currentTimeMillis() % 1000);

        WriteHelper writer = new WriteHelper(new ByteVector());
        identity.user().write(writer);
        for (User u: users)
            u.write(writer);
        
        doSendMessage(new Chat(id), new Message("!newchat", 100500, writer.getData().toBytes()));

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

    public void doSetUserLocation(User u, String addr) {
        storage.get("user.location." + u.publicKey).setString(addr);
    }

    public String getUserLocation(User u) {
        IStorage.Union obj = storage.get("user.location." + u.publicKey);

        if (obj.getType() != IStorage.ObjectType.STRING)
            return null;
        return obj.getString();
    }

    public void doSetUserName(User user, String newname) {
        storage.get("user.name." + user.publicKey).setString(newname);
    }

    public String doGetUserName(User user) {
        IStorage.Union obj = storage.get("user.name." + user.publicKey);

        if (obj.getType() != IStorage.ObjectType.STRING)
            return "";
        return obj.getString();
    }

    public String doGetChatName(Chat chat) {
        IStorage.Union obj = storage.get("chatname." + chat.id);

        if (obj.getType() != IStorage.ObjectType.STRING)
            return "";
        return obj.getString();
    }
};
