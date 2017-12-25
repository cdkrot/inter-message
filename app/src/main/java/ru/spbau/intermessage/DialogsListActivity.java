package ru.spbau.intermessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DialogsListActivity extends AppCompatActivity {

    private MessageReceiver messageReceiver;

    static private ArrayList<String> buttonNames;
    static private ArrayList<String> chatIds;
    private ArrayAdapter dialogsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);

        ListView dialogsList = (ListView) findViewById(R.id.dialogs);

        if (buttonNames == null || chatIds == null) {
            buttonNames = new ArrayList<>();
            chatIds = new ArrayList<>();
            String name = getString(R.string.new_dialog);
            buttonNames.add(name);
        }


        //noinspection unchecked
        dialogsAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);

        dialogsList.setAdapter(dialogsAdapter);

        dialogsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(DialogsListActivity.this);
                    alert.setTitle("Hello!");
                    alert.setMessage("Enter name of new Dialog:");

                    final EditText input = new EditText(DialogsListActivity.this);
                    alert.setView(input);

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Nothing to do
                        }
                    });

                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String enteredName = input.getText().toString();
                            if (enteredName.length() != 0) {
                                Controller.createNewChat(DialogsListActivity.this, enteredName);
                            }
                        }
                    });

                    alert.show();

                } else {
                    Intent newIntent = new Intent(DialogsListActivity.this, DialogActivity.class);
                    newIntent.putExtra("ChatId", chatIds.get(i - 1));
                    newIntent.putExtra("ChatName", buttonNames.get(i));
                    startActivity(newIntent);
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.ACTION_RECEIVE_DIALOGS_LIST);
        intentFilter.addAction(MessageReceiver.ACTION_CHAT_CREATED);
        registerReceiver(messageReceiver, intentFilter);

        Controller.requestDialogList(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_CHAT_CREATED = "DialogsListActivity.action.CHAT_CREATED";
        public static final String ACTION_RECEIVE_DIALOGS_LIST = "DialogsListActivity.action.DIALOGS_LIST";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_RECEIVE_DIALOGS_LIST.equals(action)) {
                ArrayList<String> names = intent.getStringArrayListExtra("Names");

                buttonNames.clear();
                buttonNames.add("Create new dialog");
                buttonNames.addAll(names);

                ArrayList<String> ids = intent.getStringArrayListExtra("Ids");

                chatIds = ids;

                dialogsAdapter.notifyDataSetChanged();

            } else if (ACTION_CHAT_CREATED.equals(action)){
                Intent newIntent = new Intent(DialogsListActivity.this, DialogActivity.class);
                newIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
                newIntent.putExtra("ChatName", intent.getStringExtra("ChatName"));

                buttonNames.add(intent.getStringExtra("ChatName"));
                chatIds.add(intent.getStringExtra("ChatId"));

                dialogsAdapter.notifyDataSetChanged();

                startActivity(newIntent);
            }
        }
    }
}
