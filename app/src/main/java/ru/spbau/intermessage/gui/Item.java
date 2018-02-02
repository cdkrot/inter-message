package ru.spbau.intermessage.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface Item {

    public void setPosition(int position);

    public int getPosition();

    public View getConvertView(LayoutInflater inflater, ViewGroup parent);

    public ViewHolder createViewHolder(View convertView);

    public void handleHolder(ViewHolder holder);
}
