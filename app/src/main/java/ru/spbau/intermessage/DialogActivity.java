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

public class DialogActivity extends AppCompatActivity {

    static final ArrayList<String> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        final ListView messagesList = (ListView)findViewById(R.id.messagesList);
        final EditText input = (EditText)findViewById(R.id.input);
        final ArrayAdapter messagesAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        messagesList.setAdapter(messagesAdapter);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String message = input.getText().toString();
                    messages.add(message);
                    input.setText("");
                    messagesAdapter.notifyDataSetChanged();
                    handled = true;
                }
                return handled;
            }
        });
    }
}
