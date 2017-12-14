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

import ru.spbau.intermessage.gui.Message;
import ru.spbau.intermessage.gui.MessageAdapter;

public class DialogActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final private List<Message> messages = new ArrayList<>();
    //static final private String dialogID
    private MessageReceiver messageReceiver;
    private MessageAdapter messagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

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
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
