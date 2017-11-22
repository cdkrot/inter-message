package ru.spbau.intermessage;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import ru.spbau.intermessage.core.Messenger;
import ru.spbau.intermessage.core.User;
import ru.spbau.intermessage.gui.Message;

public class Controller extends IntentService {

    private static Messenger messenger = new Messenger();

    private static final String ACTION_SEND_MESSAGE = "controller.action.SEND";
    private static final String ACTION_RECEIVE_MESSAGE = "controller.action.RECEIVE";
    private static final String ACTION_KILL_MESSENGER = "controller.action.KILL";

    public Controller() {
        super("Controller");
    }

    public static void sendMessage(Context context, Message message) {
        Intent intent = new Intent(context, Controller.class);
        intent.setAction(ACTION_SEND_MESSAGE);
        intent.putExtra("User", message.userName);
        intent.putExtra("Date", message.date);
        intent.putExtra("Message", message.messageText);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_MESSAGE.equals(action)) {
                final String userName = intent.getStringExtra("User");
                final long date = intent.getLongExtra("Date", 0);
                final String textMessage = intent.getStringExtra("Message");
                messenger.sendMessage(null, new ru.spbau.intermessage.core.Message("text", date, textMessage.getBytes()));

            } else if (ACTION_RECEIVE_MESSAGE.equals(action)) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(DialogActivity.MessageReceiver.ACTION_RECEIVE);
                broadcastIntent.putExtra("User", intent.getStringExtra("User"));
                broadcastIntent.putExtra("Date", intent.getLongExtra("Date", 0));
                broadcastIntent.putExtra("Message", intent.getStringExtra("Message"));
                sendBroadcast(broadcastIntent);
            } else if (ACTION_KILL_MESSENGER.equals(action)) {
                // Kill messenger and listener thread
            } else {
                // Should I fail?
            }
        }
    }
}
