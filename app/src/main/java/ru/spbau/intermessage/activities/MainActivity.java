package ru.spbau.intermessage.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.intermessage.Controller;
import ru.spbau.intermessage.Intermessage;
import ru.spbau.intermessage.R;
import ru.spbau.intermessage.console.ConsoleActivity;
import ru.spbau.intermessage.crypto.ID;

//import ru.spbau.intermessage.store.test.StorageTest;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_FILE = "preferences";
    private static final String PREF_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Allow non-blocking networking.
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        
        ListView mainMenuList = (ListView) findViewById(R.id.mainMenu);
        ArrayList<String> buttonNames = new ArrayList<>();
        buttonNames.add(getString(R.string.menu_dialogs));
        buttonNames.add(getString(R.string.menu_options));
        buttonNames.add(getString(R.string.menu_about));
        buttonNames.add(getString(R.string.menu_console));
        buttonNames.add(getString(R.string.menu_kill));

        @SuppressWarnings("unchecked")
        ArrayAdapter mainMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);

        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String userName = sharedPreferences.getString(PREF_NAME, "");

        String publicKey = sharedPreferences.getString("publicKey", "");
        if (publicKey.length() == 0) {
            ID id = ID.create();
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putString("publicKey", id.pub());
            ed.putString("privateKey", id.priv());
            ed.commit();
        }

        Intent newIntent = new Intent(this, Controller.class);
        newIntent.setAction("ACTION.START");
        startService(newIntent);

        if (userName.length() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("Enter your name:");

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
                        SharedPreferences.Editor ed = sharedPreferences.edit();
                        ed.putString(PREF_NAME, enteredName);
                        ed.apply();
                        Controller.changeUserName(enteredName);
                    } else {
                        Toast.makeText(MainActivity.this, "Incorrect name", Toast.LENGTH_LONG).show();
                    }
                }
            });

            alert.show();
        }



        mainMenuList.setAdapter(mainMenuAdapter);
        mainMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String name = buttonNames.get(i);
                if (getString(R.string.menu_dialogs).equals(name)) {
                    Intent intent = new Intent(MainActivity.this, DialogsListActivity.class);
                    startActivity(intent);
                } else if (getString(R.string.menu_options).equals(name)) {
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else if (getString(R.string.menu_about).equals(name)) {
                    Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(intent);
                } else if (getString(R.string.menu_console).equals(name)) {
                    Intent intent = new Intent(MainActivity.this, ConsoleActivity.class);
                    startActivity(intent);
                } else if (getString(R.string.menu_kill).equals(name)) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }
}
