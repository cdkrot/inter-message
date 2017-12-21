package ru.spbau.intermessage;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

//import ru.spbau.intermessage.store.StorageTest;

public class MainActivity extends AppCompatActivity {

    private final String PREF_FILE = "preferences";
    private final String PREF_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView mainMenuList = (ListView) findViewById(R.id.mainMenu);
        ArrayList<String> buttonNames = new ArrayList<>();
        String name = getString(R.string.menu_dialogs);
        buttonNames.add(name);
        name = getString(R.string.menu_options);
        buttonNames.add(name);
        ArrayAdapter mainMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);

        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String userName = sharedPreferences.getString(PREF_NAME, "");

        if (userName.length() == 0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("Enter your name:");

            final EditText input = new EditText(this);
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
                        SharedPreferences.Editor ed = sharedPreferences.edit();
                        ed.putString(PREF_NAME, enteredName);
                        ed.apply();
                        //TODO: Controller.changeName();
                    }
                }
            });

            alert.show();
        }

        mainMenuList.setAdapter(mainMenuAdapter);
        mainMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0) {
                    Intent intent = new Intent(MainActivity.this, DialogsListActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "It is point of no return.", Toast.LENGTH_SHORT).show();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });

        //StorageTest.test();
    }
}
