package ru.spbau.intermessage.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import ru.spbau.intermessage.Controller;
import ru.spbau.intermessage.R;

public class SettingsActivity extends AppCompatActivity {

    private final String PREF_FILE = "preferences";
    private final String PREF_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        String userName = sharedPreferences.getString(PREF_NAME, "");

        TextView textView = (TextView) findViewById(R.id.changeUserName);
        if (userName.length() != 0) {
            textView.setText(userName);
        }

        textView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String enteredName = textView.getText().toString();
                if (enteredName.length() != 0 && enteredName.length() < 30) {
                    SharedPreferences.Editor ed = sharedPreferences.edit();
                    ed.putString(PREF_NAME, enteredName);
                    ed.apply();

                    Controller.changeUserName(enteredName);

                    Toast.makeText(SettingsActivity.this, "Name is set", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Incorrect name", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
    }
}
