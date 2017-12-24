package ru.spbau.intermessage.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.spbau.intermessage.R;

public class ItemAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<Item> data;

    public ItemAdapter(Context c, List<Item> list) {
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
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView == null) {

            convertView = inflater.inflate(R.layout.message_layout, parent, false);

            holder = new ViewHolder();
            holder.date = (TextView) convertView.findViewById(R.id.Date);
            holder.userName = (TextView) convertView.findViewById(R.id.UserName);
            holder.text = (TextView) convertView.findViewById(R.id.MessageText);

            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        TextView date = holder.date;
        TextView userName = holder.userName;
        TextView textMessage = holder.text;

        Item message = (Item) getItem(i);

        textMessage.setText(message.messageText);

        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
        Date d = new Date(message.date * 1000);
        date.setText(df.format(d));

        userName.setText(message.userName);

        return convertView;
    }

    private static class ViewHolder {
        TextView date;
        TextView userName;
        TextView text;
    }
}
