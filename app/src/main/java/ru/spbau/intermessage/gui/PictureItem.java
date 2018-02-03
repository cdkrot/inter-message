package ru.spbau.intermessage.gui;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.spbau.intermessage.R;
import ru.spbau.intermessage.util.BitmapHelper;

public class PictureItem extends AbstractItem {
    private Bitmap bmp;
    private String userName;
    private long date;

    public PictureItem(String userName, Bitmap bmp, long date, int position) {
        this.userName = userName;
        this.bmp = bmp;
        this.date = date;
        setPosition(position);
    }

    private PictureItem(Parcel in) {
        userName = in.readString();
        int length = in.readInt();
        byte bytes[] = new byte[length];
        in.readByteArray(bytes);
        bmp = BitmapHelper.bitmapFromBytes(bytes);
        date = in.readLong();
        setPosition(in.readInt());
    }


    @Override
    public View getConvertView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.picture_layout, parent, false);
    }

    @Override
    public ViewHolder createViewHolder(View convertView) {
        PictureItem.ViewPictureHolder holder = new PictureItem.ViewPictureHolder();

        holder.date = (TextView) convertView.findViewById(R.id.date);
        holder.userName = (TextView) convertView.findViewById(R.id.userName);
        holder.image = (ImageView) convertView.findViewById(R.id.image);

        return holder;
    }

    @Override
    public void handleHolder(ViewHolder holder) {
        PictureItem.ViewPictureHolder pictureHolder = (PictureItem.ViewPictureHolder) holder;
        TextView date = pictureHolder.date;
        TextView userName = pictureHolder.userName;
        ImageView image = pictureHolder.image;

        image.setImageBitmap(bmp);

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
        return "picture";
    }

    @Override
    public byte[] getData() {
        return BitmapHelper.bytesFromBitmap(bmp);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(userName);
        byte bytes[] = BitmapHelper.bytesFromBitmap(bmp);
        parcel.writeInt(bytes.length);
        parcel.writeByteArray(bytes);
        parcel.writeLong(date);
        parcel.writeInt(getPosition());
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PictureItem createFromParcel(Parcel in) {
            return new PictureItem(in);
        }

        public PictureItem[] newArray(int size) {
            return new PictureItem[size];
        }
    };

    public class ViewPictureHolder implements ViewHolder {
        TextView date;
        TextView userName;
        ImageView image;
    }
}
