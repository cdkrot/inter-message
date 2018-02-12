package ru.spbau.intermessage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.intermessage.Controller;
import ru.spbau.intermessage.R;

public class DialogsListActivity extends AppCompatActivity {

    private MessageReceiver messageReceiver;

    private static ArrayList<String> buttonNames;
    private static ArrayList<String> chatIds;
    private ArrayAdapter dialogsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);

        ListView dialogsList = findViewById(R.id.dialogs);

        if (buttonNames == null || chatIds == null) {
            buttonNames = new ArrayList<>();
            chatIds = new ArrayList<>();
            String name = getString(R.string.new_dialog);
            buttonNames.add(name);
        }


        //noinspection unchecked
        dialogsAdapter = new ArrayAdapter(this, R.layout.dialogs_list_item, buttonNames);

        dialogsList.setAdapter(dialogsAdapter);

        dialogsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(DialogsListActivity.this);
                    alert.setMessage("Enter name of new Dialog:");

                    final EditText input = new EditText(DialogsListActivity.this);
                    input.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
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
                            if (enteredName.length() != 0 && enteredName.length() < 30) {
                                Controller.createNewChat(enteredName);
                            } else {
                                Toast.makeText(DialogsListActivity.this, "Incorrect name of dialog", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    alert.show();

                } else {
                    Intent newIntent = new Intent(DialogsListActivity.this, DialogActivity.class);
                    newIntent.putExtra("ChatId", chatIds.get(i - 1));
                    newIntent.putExtra("ChatName", buttonNames.get(i));
                    newIntent.putExtra("Created", false);
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

        Controller.requestDialogList();
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
                buttonNames.add(getString(R.string.action_create_new_dialog));
                buttonNames.addAll(names);

                chatIds = intent.getStringArrayListExtra("Ids");

                dialogsAdapter.notifyDataSetChanged();

            } else if (ACTION_CHAT_CREATED.equals(action)){
                Intent newIntent = new Intent(DialogsListActivity.this, DialogActivity.class);
                newIntent.putExtra("ChatId", intent.getStringExtra("ChatId"));
                newIntent.putExtra("ChatName", intent.getStringExtra("ChatName"));
                newIntent.putExtra("Created", true);
                buttonNames.add(intent.getStringExtra("ChatName"));
                chatIds.add(intent.getStringExtra("ChatId"));

                dialogsAdapter.notifyDataSetChanged();

                startActivity(newIntent);
            }
        }
    }
}
