package ru.spbau.intermessage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.spbau.intermessage.Controller;
import ru.spbau.intermessage.Intermessage;
import ru.spbau.intermessage.R;
import ru.spbau.intermessage.gui.Item;
import ru.spbau.intermessage.gui.MessageItem;
import ru.spbau.intermessage.gui.ItemAdapter;
import ru.spbau.intermessage.gui.PictureItem;
import ru.spbau.intermessage.util.BitmapHelper;

public class DialogActivity extends AppCompatActivity {
    private static final String PREF_FILE = "preferences";
    private static final String PREF_NAME = "userName";
    private static final int NEW_MESSAGES_LIMIT = 10;

    private static final int IMAGE_REQUEST_CODE = 3;
    private static final int PHOTO_REQUEST_CODE = 5;
    private static final int PRELOAD_INDEX = 1;

    private static final List<Item> messages = new ArrayList<>();
    private static String chatId;
    private static int waitingPosition;
    private MessageReceiver messageReceiver;
    private ItemAdapter messagesAdapter;
    private String selfUserName;

    private static Uri whereResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);

        Intent creatorIntent = getIntent();
        whereResult = null;

        String id = creatorIntent.getStringExtra("ChatId");
        if (chatId == null || !chatId.equals(id) || Intermessage.invalidated()) {
            chatId = id;
            messages.clear();
        }

        selfUserName = getSharedPreferences(PREF_FILE, MODE_PRIVATE).getString(PREF_NAME, "Default name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(creatorIntent.getStringExtra("ChatName"));
        setSupportActionBar(toolbar);

        ListView messagesList = findViewById(R.id.messagesList);
        final EditText input = findViewById(R.id.input);
        messagesAdapter = new ItemAdapter(this, messages);
        messagesList.setAdapter(messagesAdapter);

        messagesList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //Nothing to do
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == 0 || (!messages.isEmpty() && messages.get(0).getPosition() == 0)) {
                    return;
                }

                if (firstVisibleItem < PRELOAD_INDEX) {
                    waitingPosition = Math.max(0, messages.get(0).getPosition() - NEW_MESSAGES_LIMIT);
                    Controller.requestFirstMessages(chatId, messages.get(0).getPosition(), NEW_MESSAGES_LIMIT);
                }
            }
        });


        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString();
                    if (text.length() == 0) {
                        return false;
                    }

                    MessageItem newMessage = new MessageItem(selfUserName, text);
                    input.setText("");

                    Controller.sendMessage(newMessage, chatId);
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent creatorIntent = getIntent();
        boolean wasCreated = creatorIntent.getBooleanExtra("Created", false);
        creatorIntent.putExtra("Created", false);
        setIntent(creatorIntent);

        if (wasCreated) {
            Controller.requestAddUser(chatId);
        }

        if (messageReceiver == null) {
            messageReceiver = new MessageReceiver();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.ACTION_RECEIVE);
        intentFilter.addAction(MessageReceiver.ACTION_GOT_LAST_MESSAGES);
        intentFilter.addAction(MessageReceiver.ACTION_GOT_FIRST_MESSAGES);
        intentFilter.addAction(MessageReceiver.ACTION_GOT_UPDATES);
        intentFilter.addAction(MessageReceiver.ACTION_GET_USERS_FOR_ADD);
        intentFilter.addAction(MessageReceiver.ACTION_GET_USERS);
        intentFilter.addAction(MessageReceiver.ACTION_CHAT_DELETED);

        registerReceiver(messageReceiver, intentFilter);

        if (messages.size() == 0) {
            Controller.requestLastMessages(chatId, NEW_MESSAGES_LIMIT);
        } else{
            Controller.requestUpdates(chatId, messages.get(messages.size() - 1).getPosition());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dialog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        int id = item.getItemId();

        if (id == R.id.action_get_users) {
            Controller.requestUsersInChat(chatId);
            return true;
        } else if (id == R.id.action_add_users) {
            Controller.requestAddUser(chatId);
            return true;
        } else if (id == R.id.action_change_dialog_name) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Enter new name of dialog:");

            final EditText input = new EditText(this);
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

                        Controller.changeChatName(chatId, enteredName);
                        Toolbar toolbar = findViewById(R.id.toolbar);
                        toolbar.setTitle(enteredName);

                        Toast.makeText(DialogActivity.this, "New name is set", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(DialogActivity.this, "Incorrect name", Toast.LENGTH_LONG).show();
                    }
                }
            });

            alert.show();

            return true;
        } else if (id == R.id.action_send_photo) {
            try {
                File file = BitmapHelper.createImageFile();
                whereResult = Uri.fromFile(file);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Uri uri = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                startActivityForResult(intent, PHOTO_REQUEST_CODE);
            } catch (IOException e) {
                Toast.makeText(DialogActivity.this, "Unable to complete the operation", Toast.LENGTH_LONG).show();
            }

            return true;
        } else if (id == R.id.action_send_image) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_REQUEST_CODE);

            return true;
        } else if (id == R.id.action_delete_chat) {
            Controller.requestChatDeletion(chatId);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == IMAGE_REQUEST_CODE) {
            if (data == null || data.getData() == null) {
                return;
            }
            Uri uri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                bitmap = BitmapHelper.scaleBitmap(bitmap);

                Item item = new PictureItem(selfUserName, bitmap);
                Controller.sendMessage(item, chatId);
            } catch (IOException e) {
                Toast.makeText(DialogActivity.this, "Unable to complete the operation", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PHOTO_REQUEST_CODE) {
            if (whereResult == null) {
                return;
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), whereResult);
                bitmap = BitmapHelper.scaleBitmap(bitmap);

                Item item = new PictureItem(selfUserName, bitmap);
                Controller.sendMessage(item, chatId);
            } catch (IOException e) {
                Toast.makeText(DialogActivity.this, "Unable to complete the operation", Toast.LENGTH_LONG).show();
            }
        }

    }

    public class MessageReceiver extends BroadcastReceiver {
        public static final String ACTION_RECEIVE = "DialogActivity.action.RECEIVE";
        public static final String ACTION_GOT_LAST_MESSAGES = "DialogActivity.action.LAST_MESSAGES";
        public static final String ACTION_GOT_FIRST_MESSAGES = "DialogActivity.action.FIRST_MESSAGES";
        public static final String ACTION_GOT_UPDATES = "DialogActivity.action.UPDATES";
        public static final String ACTION_GET_USERS_FOR_ADD = "DialogActivity.action.GET_USERS_FOR_ADD";
        public static final String ACTION_GET_USERS = "DialogActivity.action.GET_USERS";
        public static final String ACTION_CHAT_DELETED = "DialogActivity.action.CHAT_DELETED";


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            String toChatId = intent.getStringExtra("ChatId");
            if (!toChatId.equals(chatId))
                return;

            if (ACTION_RECEIVE.equals(action)) {

                int position = (messages.size() == 0 ? 0 : messages.get(messages.size() - 1).getPosition() + 1);
                Item newMessage = (Item)intent.getParcelableExtra("Item");
                newMessage.setPosition(position);

                messages.add(newMessage);
                messagesAdapter.notifyDataSetChanged();

            } else  if (ACTION_GOT_LAST_MESSAGES.equals(action)){

                int position = intent.getIntExtra("FirstPosition", 0);
                Parcelable[] parcels = intent.getParcelableArrayExtra("Items");
                Item items[] = Arrays.copyOf(parcels, parcels.length, Item[].class);
                int length = items.length;

                for (int i = 0; i < length; i++) {
                    Item item = items[i];
                    item.setPosition(position + i);
                    messages.add(item);
                }

                messagesAdapter.notifyDataSetChanged();

            } else if (ACTION_GOT_UPDATES.equals(action)) {

                if (messages.size() == 0) {
                    return;
                }

                int position = intent.getIntExtra("FirstPosition", 0);
                Parcelable[] parcels = intent.getParcelableArrayExtra("Items");
                Item items[] = Arrays.copyOf(parcels, parcels.length, Item[].class);
                int length = items.length;
                int shift = Math.max(0, messages.get(messages.size() - 1).getPosition() - position + 1);
                for (int i = shift; i < length; i++) {
                    Item item = items[i];
                    item.setPosition(position + i);
                    messages.add(item);
                }

                messagesAdapter.notifyDataSetChanged();

            } else if (ACTION_GOT_FIRST_MESSAGES.equals(action)) {

                if (messages.size() == 0) {
                    return;
                }

                int position = intent.getIntExtra("FirstPosition", 0);
                Parcelable[] parcels = intent.getParcelableArrayExtra("Items");
                Item items[] = Arrays.copyOf(parcels, parcels.length, Item[].class);

                int length = items.length;
                int first = messages.get(0).getPosition();
                
                for (int i = 0; i < length && position + i < first; i++) {
                    Item item = items[i];
                    item.setPosition(position + i);
                    messages.add(i, item);
                }

                if (position == waitingPosition) {
                    messagesAdapter.notifyDataSetChanged();
                }

            } else if (ACTION_GET_USERS_FOR_ADD.equals(action)) {
                ArrayList<String> userNames = intent.getStringArrayListExtra("UserNames");
                ArrayList<String> userIds = intent.getStringArrayListExtra("UserIds");
                if (userNames.isEmpty()) {
                    Toast.makeText(DialogActivity.this, "No users available for addition", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder alert = new AlertDialog.Builder(DialogActivity.this);
                alert.setMessage("Choose users to add:");

                final ListView listUsers = new ListView(DialogActivity.this);
                @SuppressWarnings("unchecked")
                ArrayAdapter adapter = new ArrayAdapter(DialogActivity.this, android.R.layout.simple_list_item_multiple_choice, userNames);

                listUsers.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

                listUsers.setAdapter(adapter);
                
                alert.setView(listUsers);

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Nothing to do
                    }
                });

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int _unused) {
                        ArrayList<String> checkedIds = new ArrayList<>();
                        SparseBooleanArray checkedUsers = listUsers.getCheckedItemPositions();

                        if (checkedUsers != null) {
                            for (int i = 0; i < userNames.size(); i++)
                                if (checkedUsers.get(i)) {
                                    checkedIds.add(userIds.get(i));
                                }
                        }

                        if (!checkedIds.isEmpty()) {
                            Controller.addUsers(checkedIds, chatId);
                            if (checkedIds.size() == 1) {
                                Toast.makeText(DialogActivity.this, "1 user was added", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DialogActivity.this, checkedIds.size() + " users were added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DialogActivity.this, "No users were chosen", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.show();
            } else if (ACTION_GET_USERS.equals(action)) {
                ArrayList<String> userNames = intent.getStringArrayListExtra("UserNames");
                AlertDialog.Builder alert = new AlertDialog.Builder(DialogActivity.this);
                alert.setMessage("Users in this chat:");

                final ListView listUsers = new ListView(DialogActivity.this);
                @SuppressWarnings("unchecked")
                ArrayAdapter adapter = new ArrayAdapter(DialogActivity.this, android.R.layout.simple_list_item_1, userNames);
                listUsers.setAdapter(adapter);
                alert.setView(listUsers);

                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int _unused) {
                        // Nothing to do
                    }
                });

                alert.show();
            } else if (ACTION_CHAT_DELETED.equals(action)) {
                String delChatId = intent.getStringExtra("ChatId");
                if (chatId.equals(delChatId)) {
                    finish();
                }
            }
        }
    }
}
