package ru.spbau.intermessage;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

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
            public void onMessage(User chat, User user, ru.spbau.intermessage.core.Message message) {
                receiveMessage(Intermessage.getAppContext(), chat, message);
            }
        });
    }

    private static final String ACTION_SEND_MESSAGE = "Controller.action.SEND";
    private static final String ACTION_RECEIVE_MESSAGE = "Controller.action.RECEIVE";
    private static final String ACTION_KILL_MESSENGER = "Controller.action.KILL";
    private static final String ACTION_USER_CHANGE_NAME = "Controller.action.USER_CHANGE_NAME";
    private static final String ACTION_REQUEST_DIALOGS_LIST = "Controller.action.REQUEST_DIALOGS_LIST";


    public Controller() {
        super("Controller");
    }

    public static void sendMessage(Context context, Item message) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra("Date", message.date);
        intent.putExtra("Message", message.messageText);
        context.startService(intent);
    }

    public static void receiveMessage(Context context, User chat, Message message) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_RECEIVE_MESSAGE);
        intent.putExtra("User", "Dima");
        intent.putExtra("Date", message.timestamp);
        intent.putExtra("Message", Util.bytesToString(message.data));
        context.startService(intent);
    }

    public static void requestDialogList(Context context) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_REQUEST_DIALOGS_LIST);
        context.startService(intent);
    }

    public static void changeUserName(Context context, String newName) {
        if (newName == null)
            return;

        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_USER_CHANGE_NAME);
        intent.putExtra("NewName", newName);
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
            messenger.sendMessage(null, new Message("text", date, Util.stringToBytes(textMessage)));
        } else if (ACTION_RECEIVE_MESSAGE.equals(action)) {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_RECEIVE);
            broadcastIntent.putExtra("User", intent.getStringExtra("User"));
            broadcastIntent.putExtra("Date", intent.getLongExtra("Date", 0));
            broadcastIntent.putExtra("Message", intent.getStringExtra("Message"));
            sendBroadcast(broadcastIntent);
        } else if (ACTION_KILL_MESSENGER.equals(action)) {
            // Kill messenger and listener thread
        } else if (ACTION_USER_CHANGE_NAME.equals(action)) {
            String newName = intent.getStringExtra("NewName");
            messenger.changeUserName(newName);
        } else if (ACTION_REQUEST_DIALOGS_LIST.equals(action)) {
            messenger.requestDialogsList();
        }
    }

}
