package cn.xydzjnq.tetris;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class BlockAdapter extends BaseAdapter {
    //以后可以自定义颜色
    private int[] colors;

    public void setColors(int[] colors) {
        this.colors = colors;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return colors == null ? 0 : colors.length;
    }

    @Override
    public Object getItem(int position) {
        return colors[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block, parent, false);
        ImageView imageView = convertView.findViewById(R.id.iv_block);
        if (colors[position] == 0) {
            imageView.setEnabled(false);
        } else {
            imageView.setEnabled(true);
        }
        return convertView;
    }
}
