package ru.spbau.intermessage.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.List;
import java.util.Objects;

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
    public int getItemViewType(int i) {
        return ((Item) getItem(i)).getType().hashCode();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Item item = (Item) getItem(i);

        if (convertView == null) {
            convertView = item.getConvertView(inflater, parent);
            holder = item.createViewHolder(convertView);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        item.handleHolder(holder);

        return convertView;
    }
}
