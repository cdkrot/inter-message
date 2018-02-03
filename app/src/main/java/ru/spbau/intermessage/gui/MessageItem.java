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

public class MessageItem extends AbstractItem {
    private String userName;
    private String messageText;
    private long date;

    public MessageItem(String userName, String messageText, long date, int position) {
        this.userName = userName;
        this.messageText = messageText;
        this.date = date;
        setPosition(position);
    }

    private MessageItem(Parcel in) {
        userName = in.readString();
        messageText = in.readString();
        date = in.readLong();
        setPosition(in.readInt());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userName);
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

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public byte[] getData() {
        return Util.stringToBytes(messageText);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public MessageItem createFromParcel(Parcel in) {
            return new MessageItem(in);
        }

        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };

    public class ViewMessageHolder implements ViewHolder {
        TextView date;
        TextView userName;
        TextView text;
    }
};
