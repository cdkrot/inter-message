package ru.spbau.intermessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.spbau.intermessage.core.Message;
import ru.spbau.intermessage.gui.Item;
import ru.spbau.intermessage.gui.ItemAdapter;

public class DialogActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final private List<Item> messages = new ArrayList<>();
    static private String chatId;
    private MessageReceiver messageReceiver;
    private ItemAdapter messagesAdapter;
    private String selfUserName;

    private final String PREF_FILE = "preferences";
    private final String PREF_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Intent creatorIntent = getIntent();
        String id = creatorIntent.getStringExtra("ChatId");
        if (chatId == null || !chatId.equals(id)) {
            chatId = id;
            messages.clear();
        }

        selfUserName = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString(PREF_NAME, "Default name");

        //drawer block begins

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //drawer block ends

        ListView messagesList = (ListView)findViewById(R.id.messagesList);
        final EditText input = (EditText)findViewById(R.id.input);
        messagesAdapter = new ItemAdapter(this, messages);
        messagesList.setAdapter(messagesAdapter);


        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString();
                    if (text.length() == 0)
                        return false;

                    Item newMessage = new Item();
                    newMessage.date = System.currentTimeMillis() / 1000L;
                    newMessage.userName = selfUserName;
                    newMessage.messageText = text;
                    input.setText("");

                    messages.add(newMessage);
                    messagesAdapter.notifyDataSetChanged();

                    Controller.sendMessage(DialogActivity.this, newMessage, chatId);
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        //TODO: Handle navigation view item clicks here.

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageReceiver == null) {
            messageReceiver = new MessageReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.ACTION_RECEIVE);
        intentFilter.addAction(MessageReceiver.ACTION_GOT_LAST_MESSAGES);
        intentFilter.addAction(MessageReceiver.ACTION_GOT_UPDATES);

        registerReceiver(messageReceiver, intentFilter);

        if (messages.size() == 0) {
            Controller.requestLastMessages(this, chatId, 20);
        } else{
            Controller.requestUpdates(this, chatId, messages.get(messages.size() - 1).position);
        }
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
        public static final String ACTION_GOT_LAST_MESSAGES = "DialogActivity.action.LAST_MESSAGES";
        public static final String ACTION_GOT_UPDATES = "DialogActivity.action.UPDATES";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_RECEIVE.equals(action)) {

                String toChatId = intent.getStringExtra("ChatId");
                if (!toChatId.equals(chatId))
                    return;

                String text = intent.getStringExtra("Message");
                long date = intent.getLongExtra("Date", 0);
                String userName = intent.getStringExtra("User");

                Item newMessage = new Item();
                newMessage.date = date;
                newMessage.messageText = text;
                newMessage.userName = userName;

                messages.add(newMessage);
                messagesAdapter.notifyDataSetChanged();

            } else  if (ACTION_GOT_LAST_MESSAGES.equals(action)){
                String toChatId = intent.getStringExtra("ChatId");
                if (!toChatId.equals(chatId))
                    return;

                if (messages.size() != 0)
                    return;

                int position = intent.getIntExtra("FirstPosition", 0);
                ArrayList<String> texts = intent.getStringArrayListExtra("Texts");
                long[] timestamps = intent.getLongArrayExtra("Timestamps");
                ArrayList<String> userNames = intent.getStringArrayListExtra("UserNames");
                //TODO

            } else if (ACTION_GOT_UPDATES.equals(action)) {
                String toChatId = intent.getStringExtra("ChatId");
                if (!toChatId.equals(chatId))
                    return;

                int position = intent.getIntExtra("FirstPosition", 0);
                ArrayList<String> texts = intent.getStringArrayListExtra("Texts");
                long[] timestamps = intent.getLongArrayExtra("Timestamps");
                ArrayList<String> userNames = intent.getStringArrayListExtra("UserNames");
                //TODO
            }
        }
    }
}
