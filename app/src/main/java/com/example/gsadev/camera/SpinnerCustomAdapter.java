package com.example.gsadev.camera;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by GSA Dev on 2/27/2018.
 */

class SpinnerCustomAdapter extends BaseAdapter implements SpinnerAdapter {

    private List<Camera.Size> sizes;
    private LayoutInflater mInflater;
    private Context context;

    public SpinnerCustomAdapter(Context context, List<Camera.Size> sizes) {
        mInflater=LayoutInflater.from(context);
        this.sizes=sizes;
        this.context=context;
    }

    @Override
    public int getCount() {
        return sizes.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = mInflater.inflate(R.layout.custom_spinner_items, null);
        TextView textView=(TextView) view.findViewById(R.id.spinner_item);
        textView.setText(sizes.get(i).height+"x"+sizes.get(i).width);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_black_24dp, 0);
        return view;
    }

    @Override
    public View getDropDownView(int i, View convertView, ViewGroup parent) {
        TextView textView = new TextView(context);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextSize(20);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setText(sizes.get(i).height+"x"+sizes.get(i).width);
        textView.setTextColor(Color.parseColor("#000000"));
        return  textView;
    }


}
