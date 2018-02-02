package ru.spbau.intermessage.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.spbau.intermessage.R;

public class ItemMessage extends AbstractItem{
    private String userName;
    private String messageText;
    private long date;

    public ItemMessage(String userName, String messageText, long date, int position) {
        this.userName = userName;
        this.messageText = messageText;
        this.date = date;
        setPosition(position);
    }

    @Override
    public View getConvertView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.message_layout, parent, false);
    }

    @Override
    public ViewHolder createViewHolder(View convertView) {
        ViewMessageHolder holder = new ViewMessageHolder();

        holder.date = (TextView) convertView.findViewById(R.id.date);
        holder.userName = (TextView) convertView.findViewById(R.id.userName);
        holder.text = (TextView) convertView.findViewById(R.id.messageText);

        return holder;
    }

    @Override
    public void handleHolder(ViewHolder holder) {
        ViewMessageHolder messageHolder = (ViewMessageHolder) holder;
        TextView date = messageHolder.date;
        TextView userName = messageHolder.userName;
        TextView textMessage = messageHolder.text;

        textMessage.setText(this.messageText);

        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
        Date d = new Date(this.date * 1000);
        date.setText(df.format(d));

        userName.setText(this.userName);
    }

    public class ViewMessageHolder implements ViewHolder {
        TextView date;
        TextView userName;
        TextView text;
    }
};
