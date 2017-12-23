package ru.spbau.intermessage.console;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.intermessage.R;

public class ConsoleActivity extends AppCompatActivity {

    static private List<String> log = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);

        ListView logList = (ListView)findViewById(R.id.logList);


        @SuppressWarnings("unchecked") final ArrayAdapter<String> logAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, log);

        final EditText input = (EditText)findViewById(R.id.input);
        logList.setAdapter(logAdapter);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    String text = input.getText().toString();
                    log.add(text);
                    logAdapter.notifyDataSetChanged();

                    handleCommand(text);

                    logAdapter.notifyDataSetChanged();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void handleCommand(String command) {

    }
}
