package com.hzh.chabaike.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hzh.chabaike.R;
import com.hzh.chabaike.beans.TouTiao;

import java.util.List;

/**
 * Created by my on 2016/11/14.
 */
public class CollectAdapter extends BaseAdapter {

    private List<TouTiao.DataBean> data;
    private Context mContext;
    public CollectAdapter(List<TouTiao.DataBean> data, Context context) {
        this.data = data;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return data!=null?data.size():0;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        View ret = null;
        ViewHolder holder = null;
        if(convertView!=null){
            ret = convertView;
            holder = (ViewHolder) ret.getTag();
        }else{
            ret = LayoutInflater.from(mContext).inflate(R.layout.collection_item,parent,false);
            holder = new ViewHolder();
            holder.title = (TextView) ret.findViewById(R.id.title);
            holder.source = (TextView) ret.findViewById(R.id.source);
            holder.nickname = (TextView) ret.findViewById(R.id.nickname);
            holder.time = (TextView) ret.findViewById(R.id.time);
            ret.setTag(holder);
        }
        holder.title.setText(data.get(position).getTitle());
        holder.source.setText(data.get(position).getSource());
        holder.nickname.setText(data.get(position).getNickname());
        holder.time.setText(data.get(position).getCreate_time());

        return ret;
    }

    private static class ViewHolder{
        private TextView title,source,nickname,time;
    }
}
