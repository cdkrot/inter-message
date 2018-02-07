package ru.spbau.intermessage.gui;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.spbau.intermessage.R;
import ru.spbau.intermessage.util.Util;

public class SystemItem extends AbstractItem {
    private String messageText;
    private long date;

    public SystemItem(String messageText, long date, int position) {
        this.messageText = messageText;
        this.date = date;
        setPosition(position);
    }

    public SystemItem(String messageText) {
        this.messageText = messageText;
        this.date = System.currentTimeMillis() / 1000L;
    }

    private SystemItem(Parcel in) {
        messageText = in.readString();
        date = in.readLong();
        setPosition(in.readInt());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(messageText);
        out.writeLong(date);
        out.writeInt(getPosition());
    }

    @Override
    public View getConvertView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.message_layout, parent, false);
    }

    @Override
    public ViewHolder createViewHolder(View convertView) {
        MessageItem.ViewMessageHolder holder = new MessageItem.ViewMessageHolder();

        holder.date = (TextView) convertView.findViewById(R.id.date);
        holder.userName = (TextView) convertView.findViewById(R.id.userName);
        holder.text = (TextView) convertView.findViewById(R.id.messageText);

        return holder;
    }

    @Override
    public void handleHolder(ViewHolder holder) {
        MessageItem.ViewMessageHolder messageHolder = (MessageItem.ViewMessageHolder) holder;
        TextView date = messageHolder.date;
        TextView userName = messageHolder.userName;
        TextView textMessage = messageHolder.text;

        textMessage.setText(this.messageText);

        SimpleDateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
        Date d = new Date(this.date * 1000);
        date.setText(df.format(d));
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public String getType() {
        return "system";
    }

    @Override
    public byte[] getData() {
        return Util.stringToBytes(messageText);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SystemItem createFromParcel(Parcel in) {
            return new SystemItem(in);
        }

        public SystemItem[] newArray(int size) {
            return new SystemItem[size];
        }
    };

    public static class ViewSystemHolder implements ViewHolder {
        TextView date;
        TextView userName;
        TextView text;

        @Override
        public String getType() {
            return "system";
        }
    }
}
