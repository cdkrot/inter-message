package ru.spbau.intermessage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.gui.MessageAdapter;

public class DialogActivity extends AppCompatActivity {

    static final ArrayList<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        final ListView messagesList = (ListView)findViewById(R.id.messagesList);
        final EditText input = (EditText)findViewById(R.id.input);
        final MessageAdapter messagesAdapter = new MessageAdapter(this, messages);
        messagesList.setAdapter(messagesAdapter);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString();
                    Message newMessage = new Message();
                    //newMessage.date = "10:57 21 April 2014";
                    String date = Calendar.getInstance().getTime().toString();
                    int pos = date.indexOf(" GMT");
                    date = date.substring(0, pos == -1 ? date.length() : pos);
                    newMessage.date = date;
                    newMessage.userName = "Dima";
                    newMessage.messageText = text;
                    messages.add(newMessage);
                    input.setText("");
                    messagesAdapter.notifyDataSetChanged();
                    handled = true;
                }
                return handled;
            }
        });
    }
}
