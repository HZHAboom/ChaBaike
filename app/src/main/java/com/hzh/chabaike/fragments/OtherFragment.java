package com.hzh.chabaike.fragments;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.LoadingLayoutProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzh.chabaike.R;
import com.hzh.chabaike.WebActivity;
import com.hzh.chabaike.adapters.ToutiaoAdapter;
import com.hzh.chabaike.beans.TouTiao;
import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.net.ByteAsyncTask;
import com.hzh.chabaike.sql.MySQLiteOpenHelper;
import com.hzh.chabaike.utils.InternetUtils;
import com.hzh.chabaike.utils.SdCardUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hzh.chabaike.R.layout.foot;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherFragment extends Fragment {

    private String beforePath;
    private String path;
    private int page = 1;
    private PullToRefreshListView mPullToRefreshListView;
    private ToutiaoAdapter toutiaoAdapter;
    private List<TouTiao.DataBean> data = new ArrayList<>();
    private ImageView mImageView;
    private ListView mListView;
    private LoadingLayoutProxy mLoadingLayoutProxy;
    private View mFootView;
    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase db;
    public OtherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_other, container, false);
        mHelper = new MySQLiteOpenHelper(ret.getContext());
        db = mHelper.getReadableDatabase();
        Bundle arguments = getArguments();
        if(arguments!=null){
            beforePath = arguments.getString("path");
        }
        initView(ret);
        initListView(ret);
        initData();
        initPullToRefresh();
        return ret;
    }

    private void initPullToRefresh() {
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                data.clear();
                page = 1;
                initData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                mFootView.setVisibility(View.GONE);
                initData();
            }
        });
        mLoadingLayoutProxy = (LoadingLayoutProxy) mPullToRefreshListView.getLoadingLayoutProxy(true,false);
        mLoadingLayoutProxy.setPullLabel("下拉刷新");
        mLoadingLayoutProxy.setReleaseLabel("释放更新");
        mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间");
    }

    private void initData() {
        path = beforePath+page;
        if(InternetUtils.isConnected(getContext())){
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    TouTiao touTiao = JSON.parseObject(new String(ret), TouTiao.class);
                    String root = getContext().getExternalCacheDir().getAbsolutePath();
                    String fileName = path.replaceAll("/","").replaceAll(":","").replaceAll("\\?","");
                    SdCardUtils.saveToCache(root,fileName,ret);
                    data.addAll(touTiao.getData());
                    toutiaoAdapter.notifyDataSetChanged();
                    if (mPullToRefreshListView.isRefreshing()){
                        mPullToRefreshListView.onRefreshComplete();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间:"+simpleDateFormat.format(new Date()));
                        mFootView.setVisibility(View.VISIBLE);
                    }
                }
            }).execute(path);
        }else{
            String root = getContext().getExternalCacheDir().getAbsolutePath();
            String fileName = path.replaceAll("/","").replaceAll(":","").replaceAll("\\?","");
            byte[] byteFromCache = SdCardUtils.getByteFromCache(root, fileName);
            if(byteFromCache!=null){
                TouTiao touTiao = JSON.parseObject(new String(byteFromCache), TouTiao.class);
                data.addAll(touTiao.getData());
                toutiaoAdapter.notifyDataSetChanged();
            }
            if (mPullToRefreshListView.isRefreshing()){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                            mPullToRefreshListView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mPullToRefreshListView.onRefreshComplete();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                                    mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间:"+simpleDateFormat.format(new Date()));
                                    mFootView.setVisibility(View.VISIBLE);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private void initListView(View ret) {
        toutiaoAdapter = new ToutiaoAdapter(data,ret.getContext());
        mPullToRefreshListView.setAdapter(toutiaoAdapter);
        mListView = mPullToRefreshListView.getRefreshableView();
        mFootView = LayoutInflater.from(ret.getContext()).inflate(foot,mListView,false);
        mListView.addFooterView(mFootView);
        mFootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                initData();
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = db.query("history", new String[]{"_id"}, null, null, null, null, null);
                boolean iscollected = false;
                while (cursor.moveToNext()){
                    String _id = cursor.getString(cursor.getColumnIndex("_id"));
                    if(data.get(position-1).getId().equals(_id)){
                        iscollected = true;
                        break;
                    }
                }
                if(!iscollected){
                    ContentValues values = new ContentValues();
                    values.put("_id",data.get(position-1).getId());
                    values.put("title",data.get(position-1).getTitle());
                    values.put("source",data.get(position-1).getSource());
                    values.put("nickname",data.get(position-1).getNickname());
                    values.put("create_time",data.get(position-1).getCreate_time());
                    db.insert("history",null,values);
                }
                cursor.close();
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra("id",data.get(position-1).getId());
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(R.mipmap.icon_dialog);
                builder.setTitle("提示");
                builder.setMessage("亲，确定删除吗?");
                builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Animation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,
                                Animation.RELATIVE_TO_SELF,-1,
                                Animation.RELATIVE_TO_SELF,0,
                                Animation.RELATIVE_TO_SELF,0);
                        translate.setDuration(500);
                        view.startAnimation(translate);
                        translate.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation translate2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0,
                                        Animation.RELATIVE_TO_SELF,0,
                                        Animation.RELATIVE_TO_SELF,1,
                                        Animation.RELATIVE_TO_SELF,0);
                                translate2.setDuration(500);
                                int top = view.getTop();
                                int childCount = mListView.getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                    View childView = mListView.getChildAt(i);
                                    if(childView.getTop()>=top){
                                        childView.startAnimation(translate2);
                                    }
                                }
//                                mHandler.sendMessageDelayed(Message.obtain(mHandler,202,position),1000);
                                data.remove(position-1);
                                toutiaoAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                });
                builder.setPositiveButton("取消",null);
                builder.create().show();
                return true;
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListView.setSelection(0);
            }
        });
    }

    private void initView(View ret) {
        mPullToRefreshListView = (PullToRefreshListView) ret.findViewById(R.id.otherListView);
        mImageView = (ImageView) ret.findViewById(R.id.backToTop);
    }

}
