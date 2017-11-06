package ru.spbau.intermessage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ListView mainMenuList = (ListView) findViewById(R.id.mainMenu);
        final ArrayList<String> buttonNames = new ArrayList<>();
        String name = getString(R.string.menu_dialogs);
        buttonNames.add(name);
        name = getString(R.string.menu_options);
        buttonNames.add(name);
        final ArrayAdapter mainMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);
        mainMenuList.setAdapter(mainMenuAdapter);
        mainMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0) {
                    Intent intent = new Intent(MainActivity.this, DialogsListActivity.class);
                    startActivity(intent);
                    //Toast.makeText(MainActivity.this, "Dialogs", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(MainActivity.this, "It's a crutch", Toast.LENGTH_LONG).show();
            }
        });
    }
}
