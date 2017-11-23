package ru.spbau.intermessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import ru.spbau.intermessage.gui.Message;
import ru.spbau.intermessage.gui.MessageAdapter;

public class DialogActivity extends AppCompatActivity {

    static final private ArrayList<Message> messages = new ArrayList<>();
    //static final private String dialogID
    private MessageReceiver messageReceiver;
    private MessageAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        final ListView messagesList = (ListView)findViewById(R.id.messagesList);
        final EditText input = (EditText)findViewById(R.id.input);
        messagesAdapter = new MessageAdapter(this, messages);
        messagesList.setAdapter(messagesAdapter);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString();
                    Message newMessage = new Message();
                    newMessage.date = System.currentTimeMillis() / 1000L;
                    newMessage.userName = "Alexandr";
                    newMessage.messageText = text;
                    input.setText("");

                    messages.add(newMessage);
                    messagesAdapter.notifyDataSetChanged();

                    Controller.sendMessage(DialogActivity.this, newMessage);
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (messageReceiver == null)
            messageReceiver = new MessageReceiver();
        IntentFilter intentFilter = new IntentFilter(messageReceiver.ACTION_RECEIVE);
        registerReceiver(messageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (messageReceiver != null)
            unregisterReceiver(messageReceiver);
    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_RECEIVE = "receiver.action.RECEIVE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), ACTION_RECEIVE)) {
                String text = intent.getStringExtra("Message");
                long date = intent.getLongExtra("Date", 0);
                String userName = intent.getStringExtra("User");

                Message newMessage = new Message();
                newMessage.date = date;
                newMessage.messageText = text;
                newMessage.userName = userName;

                messages.add(newMessage);
                messagesAdapter.notifyDataSetChanged();
            }
        }
    }


}
