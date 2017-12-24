package ru.spbau.intermessage;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.intermessage.core.Chat;
import ru.spbau.intermessage.core.EventListener;
import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.core.User;
import ru.spbau.intermessage.gui.Item;
import ru.spbau.intermessage.util.Util;

public class Controller extends IntentService {

    private static Messenger messenger = new Messenger(null, "1");
    static {
        messenger.registerEventListener(new EventListener() {
            @Override
            public void onMessage(Chat chat, User user, Message message) {
                //Dima should implement
                receiveMessage(Intermessage.getAppContext(), messenger.getUserName(user), chat.id, message);
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
    private static final String ACTION_REQUEST_LATEST = "Controller.action.REQUEST_LASTEST";
    private static final String ACTION_REQUEST_UPDATES = "Controller.action.REQUEST_UPDATES";


    public Controller() {
        super("Controller");
    }

    public static void sendMessage(Context context, Item message, String chatId) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra("Date", message.date);
        intent.putExtra("Message", message.messageText);
        intent.putExtra("ChatId", chatId);
        context.startService(intent);
    }

    public static void requestLastMessages(Context context, String chatId, int limit) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_LATEST);
        intent.putExtra("ChatId", chatId);
        intent.putExtra("Limit", limit);

        context.startService(intent);
    }

    public static void requestUpdates(Context context, String chatId, int last) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_UPDATES);
        intent.putExtra("ChatId", chatId);
        intent.putExtra("Last", last);

        context.startService(intent);
    }

    public static void receiveMessage(Context context, String userName, String chatId,  Message message) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_RECEIVE_MESSAGE);
        intent.putExtra("User", userName);
        intent.putExtra("Date", message.timestamp);
        intent.putExtra("Message", Util.bytesToString(message.data));
        intent.putExtra("ChatId", chatId);
        context.startService(intent);
    }

    public static void requestDialogList(Context context) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_DIALOGS_LIST);
        context.startService(intent);
    }

    public static void returnDialogsList(Context context, List<Chat> chats, ArrayList<String> chatNames) {
        ArrayList<String> chatIds = new ArrayList<>();
        for (Chat c : chats) {
            chatIds.add(c.id);
        }

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(Controller.ACTION_RETURN_DIALOGS_LIST);
        intent.putStringArrayListExtra("Ids", chatIds);
        intent.putStringArrayListExtra("Names", chatNames);
        context.startService(intent);
    }

    public static void returnLatest(Context context, Chat chat, List<Message> messages, int firstPosition) {
        ArrayList<String> texts = new ArrayList<>();
        ArrayList<Long> timestamps = new ArrayList<>();
        ArrayList<String> userNames = new ArrayList<>();
        for (Message m : messages) {
            texts.add(Util.bytesToString(m.data));
            timestamps.add(m.timestamp);
            //TODO
        }
        //ChatId!!!

    }

    public static void returnUpdates(Context context, Chat chat, List<Message> messages, int firstPosition) {
        //TODO
        //ChatId!!!
    }

    public static void changeUserName(Context context, String newName) {
        if (newName == null)
            return;

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_USER_CHANGE_NAME);
        intent.putExtra("NewName", newName);
        context.startService(intent);
    }

    public static void createNewChat(Context context, String chatName) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_CREATE_NEW_CHAT);
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
            messenger.sendMessage(new Chat(chatId), new Message("text", date, Util.stringToBytes(textMessage)));

        } else if (ACTION_RECEIVE_MESSAGE.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_RECEIVE);
            broadcastIntent.putExtra("User", intent.getStringExtra("User"));
            broadcastIntent.putExtra("Date", intent.getLongExtra("Date", 0));
            broadcastIntent.putExtra("Message", intent.getStringExtra("Message"));
            broadcastIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
            sendBroadcast(broadcastIntent);

        } else if (ACTION_KILL_MESSENGER.equals(action)) {
            // Kill messenger and listener thread
        } else if (ACTION_USER_CHANGE_NAME.equals(action)) {

            String newName = intent.getStringExtra("NewName");
            // Dima should implement
            messenger.changeUserName(newName);

        } else if (ACTION_REQUEST_DIALOGS_LIST.equals(action)) {

            // Dima should implement
            messenger.requestDialogsList((chats, names) -> Controller.returnDialogsList(Intermessage.getAppContext(), chats, names));
        } else if (ACTION_RETURN_DIALOGS_LIST.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.putStringArrayListExtra("Ids", intent.getStringArrayListExtra("Ids"));
            broadcastIntent.putStringArrayListExtra("Names", intent.getStringArrayListExtra("Names"));
            broadcastIntent.setAction(DialogsListActivity.MessageReceiver.ACTION_RECEIVE_DIALOGS_LIST);
            sendBroadcast(broadcastIntent);

        } else if (ACTION_CREATE_NEW_CHAT.equals(action)) {

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogsListActivity.MessageReceiver.ACTION_CHAT_CREATED);
            // Dima should implement
            Chat newChat = messenger.createNewChat(intent.getStringExtra("ChatName"));
            broadcastIntent.putExtra("ChatName", intent.getStringExtra("ChatName"));
            broadcastIntent.putExtra("ChatId", newChat.id);

        } else if (ACTION_REQUEST_LATEST.equals(action)) {
            int limit = intent.getIntExtra("Limit", 0);
            String chatId = intent.getStringExtra("ChatId");
            // Dima should implement
            messenger.requestLatest(new Chat(chatId), limit,
                    (chat, messages, firstPosition) -> Controller.returnLatest(Intermessage.getAppContext(), chat, messages, firstPosition));
        } else if (ACTION_REQUEST_UPDATES.equals(action)) {
            int last = intent.getIntExtra("Last", 0);
            String chatId = intent.getStringExtra("ChatId");
            // Dima should implement
            messenger.requestUpdates(new Chat(chatId), last,
                    (chat, messages, firstPosition) -> Controller.returnUpdates(Intermessage.getAppContext(), chat, messages, firstPosition));
        }
    }
}
