package com.example.wensttay.securecam_project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.entity.RecordItem;

import java.util.List;

/**
 * Created by wensttay on 02/05/17.
 */

public class RecordItemAdapter extends BaseAdapter{

    private Context context;
    private List<RecordItem> recordItems;

    public RecordItemAdapter(Context context, List<RecordItem> recordItems) {
        this.context = context;
        this.recordItems = recordItems;
    }

    @Override
    public int getCount() {
        return this.recordItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.recordItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecordItem recordItem = recordItems.get(position);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.record_item, null);

        TextView recordItemId = (TextView) layout.findViewById(R.id.recordItemId);
        recordItemId.setText(recordItem.getId());
        TextView recordItemDate = (TextView) layout.findViewById(R.id.recordItemDate);
        recordItemDate.setText(recordItem.getDay());
        TextView recordItemHour = (TextView) layout.findViewById(R.id.recordItemHour);
        recordItemHour.setText(recordItem.getHour());
        ImageView imageView = (ImageView) layout.findViewById(R.id.recordItemImg);
        imageView.setImageBitmap(recordItem.getSmallImg());

        return layout;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<RecordItem> getRecordItems() {
        return recordItems;
    }

    public void setRecordItems(List<RecordItem> recordItems) {
        this.recordItems = recordItems;
    }
}
