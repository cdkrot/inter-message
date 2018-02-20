package ru.spbau.intermessage.core;

import android.support.annotation.Nullable;

import ru.spbau.intermessage.net.*;

import ru.spbau.intermessage.util.*;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.store.IStorage;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

import java.io.IOException;

public class Messenger {
    private Queue<RequestCommon> queue = new ArrayDeque<RequestCommon>();
    private Set<EventListener> listeners = new HashSet<EventListener>();
    private Network network;
    public final ID identity;
    private IStorage storage;

    public Messenger(IStorage store, ID id) throws IOException {
        storage = store;
        identity = id;

        network = new WifiNetwork();
        network.begin(Messenger.this, storage);

        new Thread() {
            public void run() {
                while (true) {
                    RequestCommon r;
                    while (true) {
                        synchronized (queue) {
                            if (!queue.isEmpty())
                                break;
                        }

                        try {
                            network.work();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    synchronized (queue) {
                        r = queue.poll();
                    }

                    // process new request.
                    if (r == null)
                        break; // termination.

                    if (r instanceof RunnableRequest)
                        ((RunnableRequest) (r)).run();
                    // else
                    //     handleRequest(r);

                    r.complete();
                }

                try {
                    network.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.start();
    }

    protected static class RequestCommon {
        public boolean completed = false;

        public void waitCompletion() {
            synchronized (this) {
                while (!completed)
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {} // poor java.
            }
        }
        
        public void complete() {
            synchronized (this) {
                completed = true;
                this.notifyAll();
            }
        }
    }

    protected abstract static class RunnableRequest extends RequestCommon {
        public abstract void run();
    }

    protected abstract static class InvokableRequest<R> extends RunnableRequest {
        public R result = null;

        public abstract R invoke();
        
        public void run() {
            result = invoke();
        }
    }

    /**
     * Pushes request to the queue
     * @param req Request to add
     */
    protected void postRequest(RequestCommon req) {
        synchronized (queue) {
            queue.add(req);
            
            network.interrupt();
            queue.notify();
        }
    }

    /**
     * Pushes request to the messenger queue and waits for it's completion.
     * @param req Request to push
     */
    protected void runRequest(RunnableRequest req) {
        postRequest(req);
        req.waitCompletion();
    }

    /**
     * Pushes InvokableRequest to the messenger queue,
     * Waits for it's completion and returns the result
     * @param <R> Request type
     * @param req Request to run
     * @return R Request result.
     */
    protected <R> R invokeRequest(InvokableRequest<R> req) {
        postRequest(req);
        req.waitCompletion();
        return req.result;
    }
    
    public void close() {
        postRequest(null); // termination request
    }

    public void registerEventListener(EventListener listener) {
        runRequest(new RunnableRequest() {
                public void run() {
                    listeners.add(listener);
                }
            });
    }
    
    public void deleteEventListener(EventListener listener) {
        runRequest(new RunnableRequest() {
                public void run() {
                    listeners.remove(listener);
                }
            });
    }
    
    public void sendMessage(Chat chat, Message message) {
        postRequest(new RunnableRequest() {
                public void run() {
                    System.err.println("Messenger: sending message");
                    doSendMessage(chat, message);
                }
            });
    }

    public Chat createChat(String chatname, ArrayList<User> users) {
        return invokeRequest(new InvokableRequest<Chat>() {
                public Chat invoke() {
                    Chat chat = doCreateChat(users);
                    doSendMessage(chat, getChangeChatName(chat, chatname));
                    return chat;
                }
            });
    }

    public void changeUserName(String newname) {
        postRequest(new RunnableRequest() {
                public void run() {
                    doSetUserName(identity.user(), newname);
                }
            });
    }

    public Message newMessage(String type, long tm, byte[] data) {
        return new Message(type, tm, data, identity);
    }

    public Message getChangeChatName(Chat chat, String newname) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        writer.writeString(newname);
        
        return newMessage("!newname", System.currentTimeMillis() / 1000, writer.getData().toBytes());
    }
    
    public void getListOfChats(Lambda1<List<Pair<String, Chat>>> callback) {
        postRequest(new RunnableRequest() {
                public void run() {
                    List<Pair<String, Chat>> res = new ArrayList<Pair<String, Chat>>();
                    for (Chat ch: getChatsWithUser(identity.user()))
                        if (storage.get("leavedmembers." + ch.id + "." + identity.user().publicKey).getType() == IStorage.ObjectType.NULL)
                            res.add(new Pair(doGetChatName(ch), ch));

                    callback.accept(res);
                }
            });
    }

    public void getLastMessages(Chat chat, int limit, Lambda2<Integer, List<Tuple3<User, String, Message>>> callback) {
        postRequest(new RunnableRequest() {
                public void run() {
                    int total = doGetMessageCount(chat);
                    int since = Math.max(0, total - limit);
                    
                    List<Tuple3<User, String, Message>> lst = new ArrayList<Tuple3<User, String, Message>>();
                    for (int i = since; i != total; ++i) {
                        Pair<User, Message> res = doGetMessage(chat, i);

                        lst.add(new Tuple3<User, String, Message>(res.first, doGetUserName(res.first), res.second));
                    }
                    
                    callback.accept(since, lst);
                }
            });
    }

    public void getMessagesSince(Chat chat, int from, int limit, Lambda1<List<Tuple3<User, String, Message>>> callback) {
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

    public void addUserToChat(Chat chat, User u) {
        addUsersToChat(chat, Arrays.asList(u));
    }
    
    public void addUsersToChat(Chat chat, List<User> users) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        for (User u: users)
            u.write(writer);
        sendMessage(chat, newMessage("!adduser", System.currentTimeMillis() / 1000, writer.getData().toBytes()));
    }

    public List<Pair<User, String>> getUsersNearby() {
        return invokeRequest(new InvokableRequest<List<Pair<User, String>>>() {
                public List<Pair<User, String>> invoke() {
                    List<Pair<User, String>> res = new ArrayList<Pair<User, String>>();
                    
                    for (String str: storage.getMatching("user.location.")) {
                        User u = new User(str.substring("user.location.".length()));
                        res.add(new Pair<User, String>(u, doGetUserName(u)));
                    }

                    return res;
                }
            });
    }

    public List<Pair<User, String>> getUsersInChat(Chat chat) {
        return invokeRequest(new InvokableRequest<List<Pair<User,String>>>() {
                public List<Pair<User, String>> invoke() {
                    List<Pair<User, String>> res = new ArrayList<Pair<User, String>>();
                    for (User u: getChatMembers(chat))
                        res.add(new Pair(u, doGetUserName(u)));
                    return res;
                }
            });
    }

    public void deleteChat(Chat chat) {
        runRequest(new RunnableRequest() {
                public void run() {
                    doSendMessage(chat, newMessage("!leave", System.currentTimeMillis() / 1000, ID.getSecureRandom(32)));
                }
            });
    }
    
    /* 
     * Methods below must be run within
     * Messenger's thread,
     * So they shouldn't be called directly from UI's code.
     */
    
    public ArrayList<Chat> getChatsWithUser(User u) {
        ArrayList<Chat> list = new ArrayList<Chat>();
        
        for (String str: storage.getMatching("chatswith." + u.publicKey + ".")) {
            String chat = str.substring(("chatswith." + u.publicKey + ".").length());
            list.add(new Chat(chat));
        }
        return list;
    }

    public ArrayList<User> getChatMembers(Chat chat) {
        ArrayList<User> list = new ArrayList<User>();

        for (String str: storage.getMatching("chatmembers." + chat.id + ".")) {
            String usr = str.substring(("chatmembers." + chat.id + ".").length());
                                        
            list.add(new User(usr));
        }
        
        return list;
    }
    
    public void recalcNeedsSync(User u) {
        storage.get("user.poor." + u.publicKey).setNull();
        
        for (Chat chat: getChatsWithUser(u)) {
            for (User member: getChatMembers(chat)) {
                if (storage.get("leavedmembers." + chat.id + "." + u.publicKey).getType() != IStorage.ObjectType.NULL)
                    continue;

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

    @Nullable
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
        //return true;
        if (busy.contains(u.toString()))
            return false;

        busy.add(u.toString());
        return true;
    }

    public void setNotBusy(User u) {
        busy.remove(u.toString());
    }
    
    public boolean syncWith(User u) {
        if (!setBusy(u))
            return false;
        try {
            network.create(storage.get("user.location." + u.publicKey).getString(), new ECLogic(new ServerLogic(this), this, u));
            return true;
        } catch (IOException ex) {
            setNotBusy(u);
            return false;
        }
    }
    
    public void sentMessageToParty(User u, Chat ch, User sub, int id) {
        storage.get("info." + u.publicKey + "." + ch.id + "." + sub.publicKey).setInt(id + 1);
        recalcNeedsSync(u);
    }

    public boolean verifyMessage(User u, Message m) {
        RSAPublicKey pubkey = ID.loadRsaPubkey(pubkeyByFingerprint(u.pubKey));

        return m.verifySignature(pubkey);
    }
    
    public boolean registerMessage(Chat ch, User u, int id, Message m) {
        if (storage.get("chatname." + ch.id).getType() != IStorage.ObjectType.STRING)
            storage.get("chatname." + ch.id).setString("new chat");
        
        int len = storage.getList("msg." + ch.id + "." + u.publicKey).size();
        if (id > len || id < 0)
            return false;
        if (id == len) {
            WriteHelper writer = new WriteHelper(new ByteVector());
            m.write(writer);

            storage.getList("msg." + ch.id + "." + u.publicKey).push(writer.getData().toBytes());
            
            for (User member: getChatMembers(ch))
                recalcNeedsSync(member);

            if (m.type.equals("!leave")) {
                storage.get("leavedmembers." + ch.id + "." + u.publicKey).setInt(1);
                recalcNeedsSync(u);
            } else if (m.type.equals("!newname")) {
                ReadHelper reader = new ReadHelper(ByteVector.wrap(m.data));

                String s = reader.readString();
                if (s != null)
                    doSetChatName(ch, s);

                for (EventListener listener: listeners)
                    listener.onChatAddition(ch);
            } else if (m.type.equals("!newchat") || m.type.equals("!adduser")) {
                ReadHelper reader = new ReadHelper(ByteVector.wrap(m.data));
                User xx = null;
                while ((xx = User.read(reader)) != null) {
                    if (storage.get("leavedmembers." + ch.id + "." + xx.publicKey).getType() == IStorage.ObjectType.NULL) {
                        storage.get("chatmembers." + ch.id + "." + xx.publicKey).setInt(1);
                        storage.get("chatswith." + xx.publicKey + "." + ch.id).setInt(1);
                        recalcNeedsSync(xx);
                    }
                }

                for (EventListener listener: listeners)
                    listener.onChatAddition(ch);
            }

            u.write(writer);
            storage.getList("allmsg." + ch.id).push(writer.getData().toBytes());

            for (EventListener listener: listeners)
                listener.onMessage(ch, doGetUserName(u), u, m);

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
        
        return new Pair<User, Message>(u, m);
    }
    
    public int needMessage(Chat ch, User u, int i) {
        if (storage.get("leavedmembers." + ch.id + "." + identity.user().publicKey).getType() != IStorage.ObjectType.NULL)
            return 0; // it's invalid but let's simply say we don't need.
        
        int len = storage.getList("msg." + ch.id + "." + u.publicKey).size();
        if (i == len)
            return 1; // yep
        if (i > len || i < 0)
            return -1; // wtf
        return 0; // don't need.
    }

    public byte[] pubkeyByFingerprint(byte[] fingerprint) {
        if (Arrays.equals(identity.user().pubKey, fingerprint))
            return identity.pubkey.getEncoded();
        
        IStorage.Union obj = storage.get("fingerprint." + Util.toHex(fingerprint));

        if (obj.getType() == IStorage.ObjectType.STRING)
            return Util.decodeHex(obj.getString());
        return null;
    }

    public boolean importPubkey(byte[] fingerprint, byte[] key) {
        RSAPublicKey rkey = ID.loadRsaPubkey(key);
        if (key == null || !Arrays.equals(ID.getFingerprint(rkey), fingerprint))
            return false;

        return checkFingerprint(fingerprint, key);
    }
    
    public boolean checkFingerprint(byte[] fingerprint, byte[] key) {
        byte[] stored = pubkeyByFingerprint(fingerprint);
        if (stored != null)
            return Arrays.equals(stored, key);
        else {
            storage.get("fingerprint." + Util.toHex(fingerprint)).setString(Util.toHex(key));
            return true;
        }
    }
    
    protected void doSendMessage(Chat ch, Message msg) {
        WriteHelper writer = new WriteHelper(new ByteVector());
        msg.write(writer);

        registerMessage(ch, identity.user(), storage.getList("msg." + ch.id + "." + identity.user().publicKey).size(), msg);
    }
    
    public Chat doCreateChat(ArrayList<User> users) {
        String id = "" + (System.currentTimeMillis() % 1000);

        WriteHelper writer = new WriteHelper(new ByteVector());
        identity.user().write(writer);
        for (User u: users)
            u.write(writer);
        
        doSendMessage(new Chat(id), newMessage("!newchat", System.currentTimeMillis() / 1000, writer.getData().toBytes()));
        
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
        
        Message m = Message.read(reader);

        if (m == null)
            throw new RuntimeException(data.toString());
        return m;
    }

    public void doSetUserLocation(User u, String addr) {
        storage.get("user.location." + u.publicKey).setString(addr);
    }

    @Nullable
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

    public void doSetChatName(Chat chat, String name) {
        storage.get("chatname." + chat.id).setString(name);
    }
        
    public String doGetChatName(Chat chat) {
        IStorage.Union obj = storage.get("chatname." + chat.id);

        if (obj.getType() != IStorage.ObjectType.STRING)
            return "";
        return obj.getString();
    }
};
