package com.hzh.chabaike.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hzh.chabaike.MyLruCache;
import com.hzh.chabaike.R;
import com.hzh.chabaike.beans.TouTiao;
import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.net.ByteAsyncTask;
import com.hzh.chabaike.utils.SdCardUtils;

import java.util.List;

/**
 * Created by my on 2016/11/14.
 */
public class ToutiaoAdapter extends BaseAdapter {

    private List<TouTiao.DataBean> data;
    private Context mContext;
    private MyLruCache mMyLruCache;
    public ToutiaoAdapter(List<TouTiao.DataBean> data, Context context) {
        this.data = data;
        this.mContext = context;
        mMyLruCache = new MyLruCache((int) (Runtime.getRuntime().maxMemory()/8));
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
            ret = LayoutInflater.from(mContext).inflate(R.layout.toutiao_item,parent,false);
            holder = new ViewHolder();
            holder.mImageView = (ImageView) ret.findViewById(R.id.image);
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
        final String path = data.get(position).getWap_thumb();
        if(path.length()>0){

            Bitmap bitmap = getCache(path);
            if(bitmap!=null){
                holder.mImageView.setImageBitmap(bitmap);
            }else{
                final ViewHolder fholder = holder;
                fholder.mImageView.setImageResource(R.mipmap.ic_launcher);
                fholder.mImageView.setTag(path);
                new ByteAsyncTask(new ByteCallback() {
                    @Override
                    public void callback(byte[] ret) {
                        String tag = (String) fholder.mImageView.getTag();
                        if(ret!=null&&path.equals(tag)){
                            Bitmap image = BitmapFactory.decodeByteArray(ret,0,ret.length);
                            fholder.mImageView.setImageBitmap(image);
                            String root = mContext.getExternalCacheDir().getAbsolutePath();
                            String fileName = path.substring(path.lastIndexOf("/")+1);
                            mMyLruCache.put(fileName,image);
                            SdCardUtils.saveToCache(root,fileName,ret);
                        }
                    }
                }).execute(path);
            }
        }else{
            holder.mImageView.setImageResource(R.mipmap.ic_launcher);
        }
        return ret;
    }

    private Bitmap getCache(String path) {
        String img = path.substring(path.lastIndexOf("/")+1);
        Bitmap bitmap = mMyLruCache.get(img);
        if(bitmap!=null){
            return bitmap;
        }else{
            String root = mContext.getExternalCacheDir().getAbsolutePath();
            byte[] byteFromCache = SdCardUtils.getByteFromCache(root, img);
            if(byteFromCache!=null){
                Bitmap image = BitmapFactory.decodeByteArray(byteFromCache,0,byteFromCache.length);
                mMyLruCache.put(img,image);
                return image;
            }
        }
        return null;
    }

    private static class ViewHolder{
        private ImageView mImageView;
        private TextView title,source,nickname,time;
    }
}
