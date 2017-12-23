package ru.spbau.intermessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DialogsListActivity extends AppCompatActivity {

    private MessageReceiver messageReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);

        ListView dialogsList = (ListView) findViewById(R.id.dialogs);
        ArrayList<String> buttonNames = new ArrayList<>();
        String name = getString(R.string.new_dialog);

        buttonNames.add(name);
        Controller.requestDialogList(this);

        @SuppressWarnings("unchecked")
        final ArrayAdapter dialogsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);

        dialogsList.setAdapter(dialogsAdapter);

        dialogsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0) {
                    Toast.makeText(DialogsListActivity.this, "Isn't Elite dialog sufficient for everything?", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(DialogsListActivity.this, DialogActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageReceiver == null) {
            messageReceiver = new DialogsListActivity.MessageReceiver();
        }
        IntentFilter intentFilter = new IntentFilter(messageReceiver.ACTION_RECEIVE);
        registerReceiver(messageReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_RECEIVE = "DialogActivity.action.RECEIVE";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_RECEIVE.equals(intent.getAction())) {
                /*String text = intent.getStringExtra("Message");
                long date = intent.getLongExtra("Date", 0);
                String userName = intent.getStringExtra("User");

                Item newMessage = new Item();
                newMessage.date = date;
                newMessage.messageText = text;
                newMessage.userName = userName;

                messages.add(newMessage);
                messagesAdapter.notifyDataSetChanged();*/
            }
        }
    }
}
