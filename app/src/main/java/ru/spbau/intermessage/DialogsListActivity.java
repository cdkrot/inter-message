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

public class DialogsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);
        final ListView dialogsList = (ListView) findViewById(R.id.dialogs);
        final ArrayList<String> buttonNames = new ArrayList<>();
        String name = getString(R.string.new_dialog);
        buttonNames.add(name);
        buttonNames.add("Crutched dialog");
        final ArrayAdapter mainMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, buttonNames);
        dialogsList.setAdapter(mainMenuAdapter);
        dialogsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (l == 0)
                    Toast.makeText(DialogsListActivity.this, "Isn't crutched dialog sufficient for everything?", Toast.LENGTH_LONG).show();
                else {
                    Intent intent = new Intent(DialogsListActivity.this, DialogActivity.class);
                    startActivity(intent);
                    //Toast.makeText(DialogsListActivity.this, "It's a crutch", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
