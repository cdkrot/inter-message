package ru.spbau.intermessage.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ru.spbau.intermessage.R;
import ru.spbau.intermessage.core.Message;

public class MessageAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Message> data;

    public MessageAdapter(Context c, ArrayList<Message> list) {
        context = c;
        data = list;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.message_layout, parent, false);

        TextView textMessage = (TextView) rowView.findViewById(R.id.MessageText);
        TextView date = (TextView) rowView.findViewById(R.id.Date);
        TextView userName = (TextView) rowView.findViewById(R.id.UserName);

        Message message = (Message) getItem(i);

        textMessage.setText(message.messageText);
        date.setText(message.date);
        userName.setText(message.userName);

        return rowView;
    }
}
