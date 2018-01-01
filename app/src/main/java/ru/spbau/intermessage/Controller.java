package ru.spbau.intermessage;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.spbau.intermessage.activities.DialogActivity;
import ru.spbau.intermessage.activities.DialogsListActivity;
import ru.spbau.intermessage.core.Chat;
import ru.spbau.intermessage.core.EventListener;
import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.core.User;
import ru.spbau.intermessage.crypto.ID;
import ru.spbau.intermessage.gui.Item;
import ru.spbau.intermessage.store.Storage;
import ru.spbau.intermessage.util.Pair;
import ru.spbau.intermessage.util.Tuple3;
import ru.spbau.intermessage.util.Util;

public class Controller extends IntentService {

    private static Messenger messenger = new Messenger(new Storage(), getId());
    static {
        //main listener
        messenger.registerEventListener(new EventListener() {
            @Override
            public void onMessage(Chat chat, String userName, User user, Message message) {
                receiveMessage(userName, chat.id, message);
            }

            @Override
            public void onChatAddition(Chat chat) {
                requestDialogList();
            }
        });

        //notification listener
        messenger.registerEventListener(new EventListener() {
            @Override
            public void onMessage(Chat chat, String uname, User user, Message message) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(Intermessage.getAppContext())
                                .setSmallIcon(R.mipmap.ic_launcher_mascot)
                                .setContentTitle("Dialog notification")
                                .setContentText("New message(s)");

                Intent resultIntent = new Intent(Intermessage.getAppContext(), DialogActivity.class);
                resultIntent.putExtra("ChatId", chat.id);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        Intermessage.getAppContext(),
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                builder.setVibrate(new long[] { 500, 300, 300});
                builder.setLights(Color.WHITE, 3000, 3000);

                NotificationManager notificationManager =
                        (NotificationManager) Intermessage.getAppContext().getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());
            }

            @Override
            public void onChatAddition(Chat chat) {

            }
        });

    }

    private static final String ACTION_SEND_MESSAGE = "Controller.action.SEND";
    private static final String ACTION_RECEIVE_MESSAGE = "Controller.action.RECEIVE";
    private static final String ACTION_KILL_MESSENGER = "Controller.action.KILL";
    private static final String ACTION_USER_CHANGE_NAME = "Controller.action.USER_CHANGE_NAME";
    private static final String ACTION_REQUEST_DIALOGS_LIST = "Controller.action.REQUEST_DIALOGS_LIST";
    private static final String ACTION_RETURN_DIALOGS_LIST = "Controller.action.RETURN_DIALOGS_LIST";
    private static final String ACTION_CREATE_NEW_CHAT = "Controller.action.CREATE_NEW_CHAT";
    private static final String ACTION_REQUEST_LATEST = "Controller.action.REQUEST_LATEST";
    private static final String ACTION_REQUEST_UPDATES = "Controller.action.REQUEST_UPDATES";
    private static final String ACTION_RETURN_UPDATES = "Controller.action.RETURN_UPDATES";
    private static final String ACTION_RETURN_LATEST = "Controller.action.RETURN_LATEST";
    private static final String ACTION_REQUEST_ADD_USER = "Controller.action.REQUEST_ADD_USER";
    private static final String ACTION_ADD_USER = "Controller.action.ADD_USER";
    private static final String ACTION_ADD_USERS = "Controller.action.ADD_USERS";
    private static final String ACTION_GET_USERS_IN_CHAT = "Controller.action.GET_USERS_IN_CHAT";
    private static final String ACTION_CHANGE_CHAT_NAME = "Controller.action.CHANGE_CHAT_NAME";

    public Controller() {
        super("Controller");
    }

    private static ID getId() {
        SharedPreferences sharedPreferences = Intermessage.getAppContext().getSharedPreferences("preferences", MODE_PRIVATE);
        String publicKey = sharedPreferences.getString("publicKey", "trustno1");
        String privateKey = sharedPreferences.getString("privateKey", "beliveinlie");
        return new ID(privateKey, publicKey);
    }

    public static void sendMessage(Item message, String chatId) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra("Date", message.date);
        intent.putExtra("Message", message.messageText);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void requestLastMessages(String chatId, int limit) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_LATEST);
        intent.putExtra("ChatId", chatId);
        intent.putExtra("Limit", limit);

        context.startService(intent);
    }

    public static void requestUpdates(String chatId, int last) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_UPDATES);
        intent.putExtra("ChatId", chatId);
        intent.putExtra("Last", last);

        context.startService(intent);
    }

    public static void receiveMessage(String userName, String chatId,  Message message) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_RECEIVE_MESSAGE);
        intent.putExtra("User", userName);
        intent.putExtra("Date", message.timestamp);
        intent.putExtra("Message", Util.bytesToString(message.data));
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void requestDialogList() {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_DIALOGS_LIST);

        context.startService(intent);
    }

    public static void returnDialogsList(List<Pair<String, Chat>> chats) {
        Context context = Intermessage.getAppContext();
        ArrayList<String> chatIds = new ArrayList<>();
        ArrayList<String> chatNames = new ArrayList<>();

        for (Pair<String, Chat> c : chats) {
            chatIds.add(c.second.id);
            chatNames.add(c.first);
        }

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(Controller.ACTION_RETURN_DIALOGS_LIST);
        intent.putStringArrayListExtra("Ids", chatIds);
        intent.putStringArrayListExtra("Names", chatNames);

        context.startService(intent);
    }

    public static void returnLatest(String chatId, List<Tuple3<User, String, Message>> messages, int firstPosition) {
        Context context = Intermessage.getAppContext();
        String[] texts = new String[messages.size()];
        long[] timestamps = new long[messages.size()];
        String[] userNames = new String[messages.size()];

        for (int i = 0; i < messages.size(); i++) {
            Tuple3<User, String, Message> tr = messages.get(i);
            texts[i] = Util.bytesToString(tr.third.data);
            timestamps[i] = tr.third.timestamp;
            userNames[i] = tr.second;
        }

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(Controller.ACTION_RETURN_LATEST);
        intent.putExtra("FirstPosition", firstPosition);
        intent.putExtra("Timestamps", timestamps);
        intent.putExtra("UserNames", userNames);
        intent.putExtra("Texts", texts);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void returnUpdates(String chatId, List<Tuple3<User, String, Message>> messages, int firstPosition) {
        Context context = Intermessage.getAppContext();
        String[] texts = new String[messages.size()];
        long[] timestamps = new long[messages.size()];
        String[] userNames = new String[messages.size()];
        for (int i = 0; i < messages.size(); i++) {
            Tuple3<User, String, Message> tr = messages.get(i);
            texts[i] = Util.bytesToString(tr.third.data);
            timestamps[i] = tr.third.timestamp;
            userNames[i] = tr.second;
        }
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(Controller.ACTION_RETURN_UPDATES);
        intent.putExtra("FirstPosition", firstPosition);
        intent.putExtra("Timestamps", timestamps);
        intent.putExtra("UserNames", userNames);
        intent.putExtra("Texts", texts);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void changeUserName(String newName) {
        Context context = Intermessage.getAppContext();

        if (newName == null)
            return;

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_USER_CHANGE_NAME);
        intent.putExtra("NewName", newName);

        context.startService(intent);
    }

    public static void createNewChat(String chatName) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_CREATE_NEW_CHAT);
        intent.putExtra("ChatName", chatName);

        context.startService(intent);
    }

    public static void requestAddUser(String chatId) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_ADD_USER);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void addUser(String userId, String chatId) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_ADD_USER);
        intent.putExtra("UserId", userId);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void addUsers(ArrayList<String> userIds, String chatId) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_ADD_USERS);
        intent.putExtra("UserIds", userIds);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void requestUsersInChat(String chatId) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_GET_USERS_IN_CHAT);
        intent.putExtra("ChatId", chatId);

        context.startService(intent);
    }

    public static void changeChatName(String chatId, String chatName) {
        Context context = Intermessage.getAppContext();
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_CHANGE_CHAT_NAME);
        intent.putExtra("ChatId", chatId);
        intent.putExtra("ChatName", chatName);

        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (ACTION_SEND_MESSAGE.equals(action)) {

            long date = intent.getLongExtra("Date", 0);
            String textMessage = intent.getStringExtra("Message");
            String chatId = intent.getStringExtra("ChatId");
            byte[] bytes = Util.stringToBytes(textMessage);
            messenger.sendMessage(new Chat(chatId), new Message("text", date, bytes));

        } else if (ACTION_RECEIVE_MESSAGE.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_RECEIVE);
            broadcastIntent.putExtra("User", intent.getStringExtra("User"));
            broadcastIntent.putExtra("Date", intent.getLongExtra("Date", 0));
            broadcastIntent.putExtra("Message", intent.getStringExtra("Message"));
            broadcastIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
            sendBroadcast(broadcastIntent);

        } else if (ACTION_KILL_MESSENGER.equals(action)) {

            android.os.Process.killProcess(android.os.Process.myPid());

        } else if (ACTION_USER_CHANGE_NAME.equals(action)) {

            String newName = intent.getStringExtra("NewName");
            messenger.changeUserName(newName);

        } else if (ACTION_REQUEST_DIALOGS_LIST.equals(action)) {

            messenger.getListOfChats(Controller::returnDialogsList);
        } else if (ACTION_RETURN_DIALOGS_LIST.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.putStringArrayListExtra("Ids", intent.getStringArrayListExtra("Ids"));
            broadcastIntent.putStringArrayListExtra("Names", intent.getStringArrayListExtra("Names"));
            broadcastIntent.setAction(DialogsListActivity.MessageReceiver.ACTION_RECEIVE_DIALOGS_LIST);
            sendBroadcast(broadcastIntent);

        } else if (ACTION_CREATE_NEW_CHAT.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogsListActivity.MessageReceiver.ACTION_CHAT_CREATED);
            Chat newChat = messenger.createChat(intent.getStringExtra("ChatName"), new ArrayList<>());
            broadcastIntent.putExtra("ChatName", intent.getStringExtra("ChatName"));
            broadcastIntent.putExtra("ChatId", newChat.id);
            sendBroadcast(broadcastIntent);

        } else if (ACTION_REQUEST_LATEST.equals(action)) {

            int limit = intent.getIntExtra("Limit", 0);
            String chatId = intent.getStringExtra("ChatId");
            messenger.getLastMessages(new Chat(chatId), limit,
                    (firstPosition, messages) -> Controller.returnLatest(chatId, messages, firstPosition));

        } else if (ACTION_REQUEST_UPDATES.equals(action)) {

            int last = intent.getIntExtra("Last", 0);
            String chatId = intent.getStringExtra("ChatId");
            messenger.getMessagesSince(new Chat(chatId), last + 1, 10000000,
                    (messages) -> Controller.returnUpdates(chatId, messages, last + 1));

        } else if (ACTION_RETURN_LATEST.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_GOT_LAST_MESSAGES);
            broadcastIntent.putExtra("UserNames", intent.getStringArrayExtra("UserNames"));
            broadcastIntent.putExtra("Timestamps", intent.getLongArrayExtra("Timestamps"));
            broadcastIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
            broadcastIntent.putExtra("Texts", intent.getStringArrayExtra("Texts"));
            broadcastIntent.putExtra("FirstPosition", intent.getIntExtra("FirstPosition", 0));
            sendBroadcast(broadcastIntent);

        } else if (ACTION_RETURN_UPDATES.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_GOT_UPDATES);
            broadcastIntent.putExtra("UserNames", intent.getStringArrayExtra("UserNames"));
            broadcastIntent.putExtra("Timestamps", intent.getLongArrayExtra("Timestamps"));
            broadcastIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
            broadcastIntent.putExtra("Texts", intent.getStringArrayExtra("Texts"));
            broadcastIntent.putExtra("FirstPosition", intent.getIntExtra("FirstPosition", 0));
            sendBroadcast(broadcastIntent);

        } else if (ACTION_REQUEST_ADD_USER.equals(action)) {

            String chatId = intent.getStringExtra("ChatId");
            List<Pair<User, String>> usersNearby = messenger.getUsersNearby();
            List<Pair<User, String>> usersInChat = messenger.getUsersInChat(new Chat(chatId));
            Collections.sort(usersNearby, (a, b) -> a.first.publicKey.compareTo(b.first.publicKey));
            Collections.sort(usersInChat, (a, b) -> a.first.publicKey.compareTo(b.first.publicKey));

            ArrayList<String> userIds = new ArrayList<>();
            ArrayList<String> userNames = new ArrayList<>();
            int position = 0;
            for (Pair<User, String> p : usersNearby) {
                while (position != usersInChat.size() &&
                        p.first.publicKey.compareTo(usersInChat.get(position).first.publicKey) < 0) {
                    position++;
                }
                if (position == usersInChat.size() ||
                        p.first.publicKey.compareTo(usersInChat.get(position).first.publicKey) > 0) {
                    userIds.add(p.first.publicKey);
                    userNames.add(p.second);
                }
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_GET_USERS_FOR_ADD);

            broadcastIntent.putExtra("ChatId", chatId);
            broadcastIntent.putExtra("UserNames", userNames);
            broadcastIntent.putExtra("UserIds", userIds);
            sendBroadcast(broadcastIntent);

        } else if (ACTION_ADD_USER.equals(action)) {

            String userId = intent.getStringExtra("UserId");
            String chatId = intent.getStringExtra("ChatId");
            messenger.addUserToChat(new Chat(chatId), new User(userId));

        } else if (ACTION_ADD_USERS.equals(action)) {

            ArrayList<String> userIds = intent.getStringArrayListExtra("UserIds");
            String chatId = intent.getStringExtra("ChatId");
            ArrayList<User> users = new ArrayList<>();
            for (String id : userIds) {
                users.add(new User(id));
            }

            messenger.addUsersToChat(new Chat(chatId), users);

        } else if (ACTION_GET_USERS_IN_CHAT.equals(action)) {

            String chatId = intent.getStringExtra("ChatId");
            List<Pair<User, String>> usersInChat = messenger.getUsersInChat(new Chat(chatId));
            ArrayList<String> userNames = new ArrayList<>();
            for (Pair<User, String> p : usersInChat) {
                userNames.add(p.second);
            }

            Intent broadcastIntent = new Intent();
            broadcastIntent.putExtra("ChatId", chatId);
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_GET_USERS);
            broadcastIntent.putExtra("UserNames", userNames);
            sendBroadcast(broadcastIntent);

        } else if (ACTION_CHANGE_CHAT_NAME.equals(action)) {

            String chatId = intent.getStringExtra("ChatId");
            String chatName = intent.getStringExtra("ChatName");
            //messenger.changeChatName(new Chat(chatId), chatName);

        }
    }
}
