package ru.spbau.intermessage.gui;


import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PictureItem extends AbstractItem {

    private PictureItem(Parcel in) {
        // TODO
        setPosition(in.readInt());
    }


    @Override
    public View getConvertView(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }

    @Override
    public ViewHolder createViewHolder(View convertView) {
        return null;
    }

    @Override
    public void handleHolder(ViewHolder holder) {

    }

    @Override
    public long getDate() {
        return 0;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public byte[] getData() {
        return new byte[0];
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PictureItem createFromParcel(Parcel in) {
            return new PictureItem(in);
        }

        public PictureItem[] newArray(int size) {
            return new PictureItem[size];
        }
    };
}
