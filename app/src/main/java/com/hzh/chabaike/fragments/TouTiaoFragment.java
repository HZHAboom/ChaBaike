package com.hzh.chabaike.fragments;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.LoadingLayoutProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzh.chabaike.MainActivity;
import com.hzh.chabaike.MyLruCache;
import com.hzh.chabaike.R;
import com.hzh.chabaike.Uri.Constants;
import com.hzh.chabaike.WebActivity;
import com.hzh.chabaike.adapters.MyViewPagerAdapter;
import com.hzh.chabaike.adapters.ToutiaoAdapter;
import com.hzh.chabaike.beans.HeaderImage;
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
public class TouTiaoFragment extends Fragment {

    private int page = 1;
    private String beforePath = Constants.HEADLINE_URL + Constants.HEADLINE_TYPE;
    private String path;
    private List<TouTiao.DataBean> data = new ArrayList<>();
    private PullToRefreshListView mPullToRefreshListView;
    private ToutiaoAdapter toutiaoAdapter;
    private MyLruCache mMyLruCache;
    private ImageView mImageView;
    private int lastPosition = 0;
    private int currentPosition = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 101:
                    currentPosition++;
                    if (currentPosition > 2) {
                        currentPosition = 0;
                    }
                    mViewPager.setCurrentItem(currentPosition);
                    this.sendEmptyMessageDelayed(101, 3000);
                    break;
            }
        }
    };
    private ViewPager mViewPager;
    private ListView mListView;
    private LoadingLayoutProxy mLoadingLayoutProxy;
    private View mFootView;
    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase db;

    public TouTiaoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ret = inflater.inflate(R.layout.fragment_tou_tiao, container, false);
        mMyLruCache = new MyLruCache((int) (Runtime.getRuntime().maxMemory() / 8));
        mHelper = new MySQLiteOpenHelper(ret.getContext());
        db = mHelper.getReadableDatabase();
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

//                mPullToRefreshListView.onRefreshComplete();

                initData();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        mPullToRefreshListView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d("flag", "---------->run: &&&&&&&&&&&&&&&&&&&");
//                                mPullToRefreshListView.onRefreshComplete();
//                            }
//                        });
//                    }
//                }).start();
            }
        });
        mLoadingLayoutProxy = (LoadingLayoutProxy) mPullToRefreshListView.getLoadingLayoutProxy(true, false);
        mLoadingLayoutProxy.setPullLabel("下拉刷新");
        mLoadingLayoutProxy.setReleaseLabel("释放更新");
        mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间");
    }


    private void initListView(View ret) {
        toutiaoAdapter = new ToutiaoAdapter(data, ret.getContext());
        mPullToRefreshListView.setAdapter(toutiaoAdapter);
        mListView = mPullToRefreshListView.getRefreshableView();
        final View headView = LayoutInflater.from(ret.getContext()).inflate(R.layout.head, mListView, false);
        mFootView = LayoutInflater.from(ret.getContext()).inflate(foot, mListView, false);
        mViewPager = (ViewPager) headView.findViewById(R.id.toutiaoViewPager);
        final TextView headtitle = (TextView) headView.findViewById(R.id.headtitle);
        final LinearLayout linearLayout = (LinearLayout) headView.findViewById(R.id.pageLinear);
        if (InternetUtils.isConnected(getContext())) {
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    HeaderImage headerImage = JSON.parseObject(new String(ret), HeaderImage.class);
                    String root = getContext().getExternalCacheDir().getAbsolutePath();
                    String fileName = "headerimage";
                    SdCardUtils.saveToCache(root, fileName, ret);
                    final List<HeaderImage.DataBean> data = headerImage.getData();
                    headtitle.setText(data.get(0).getTitle());
                    linearLayout.getChildAt(0).setEnabled(false);
                    final List<ImageView> imageViews = new ArrayList<>();
                    final PagerAdapter adapter = new MyViewPagerAdapter(imageViews);
                    for (int i = 0; i < data.size(); i++) {
                        final ImageView imageView = new ImageView(headView.getContext());
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        final String path = data.get(i).getImage_s();
                        Bitmap bitmap = getCache(path);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageViews.add(imageView);
                            adapter.notifyDataSetChanged();
                        } else {
                            new ByteAsyncTask(new ByteCallback() {
                                @Override
                                public void callback(byte[] ret) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(ret, 0, ret.length);
                                    String img = path.substring(path.lastIndexOf("/") + 1);
                                    imageView.setImageBitmap(bitmap);
                                    imageViews.add(imageView);
                                    mMyLruCache.put(img, bitmap);
                                    String root = getContext().getExternalCacheDir().getAbsolutePath();
                                    SdCardUtils.saveToCache(root, img, ret);
                                    adapter.notifyDataSetChanged();
                                }
                            }).execute(path);
                        }
                    }
                    mViewPager.setAdapter(adapter);
                    mViewPager.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            ((MainActivity) getContext()).getViewPager_main().requestDisallowInterceptTouchEvent(true);
                            return false;
                        }
                    });
                    mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            headtitle.setText(data.get(position).getTitle());
                            linearLayout.getChildAt(lastPosition).setEnabled(true);
                            linearLayout.getChildAt(position).setEnabled(false);
                            lastPosition = position;
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });
                    mHandler.sendEmptyMessageDelayed(101, 3000);
                }
            }).execute(Constants.HEADERIMAGE_URL);
        } else {
            String root = getContext().getExternalCacheDir().getAbsolutePath();
            String fileName = "headerimage";
            byte[] byteFromCache = SdCardUtils.getByteFromCache(root, fileName);
            HeaderImage headerImage = JSON.parseObject(new String(byteFromCache), HeaderImage.class);
            List<ImageView> imageViews = new ArrayList<>();
            final List<HeaderImage.DataBean> data = headerImage.getData();
            headtitle.setText(data.get(0).getTitle());
            linearLayout.getChildAt(0).setEnabled(false);
            for (int i = 0; i < data.size(); i++) {
                ImageView imageView = new ImageView(headView.getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                String image_s = data.get(i).getImage_s();
                String path = image_s.substring(image_s.lastIndexOf("/") + 1);
                Bitmap bitmap = getCache(path);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }
                imageViews.add(imageView);
            }
            PagerAdapter adapter = new MyViewPagerAdapter(imageViews);
            mViewPager.setAdapter(adapter);
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ((MainActivity) getContext()).getViewPager_main().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    headtitle.setText(data.get(position).getTitle());
                    linearLayout.getChildAt(lastPosition).setEnabled(true);
                    linearLayout.getChildAt(position).setEnabled(false);
                    lastPosition = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            mHandler.sendEmptyMessageDelayed(101, 3000);
        }
        mListView.addHeaderView(headView);
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
                while (cursor.moveToNext()) {
                    String _id = cursor.getString(cursor.getColumnIndex("_id"));
                    if (data.get(position - 2).getId().equals(_id)) {
                        iscollected = true;
                        break;
                    }
                }
                if (!iscollected) {
                    ContentValues values = new ContentValues();
                    values.put("_id", data.get(position - 2).getId());
                    values.put("title", data.get(position - 2).getTitle());
                    values.put("source", data.get(position - 2).getSource());
                    values.put("nickname", data.get(position - 2).getNickname());
                    values.put("create_time", data.get(position - 2).getCreate_time());
                    db.insert("history", null, values);
                }
                cursor.close();
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra("id", data.get(position - 2).getId());
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
                        Animation translate = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, -1,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0);
                        translate.setDuration(500);
                        view.startAnimation(translate);
                        translate.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                Animation translate2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                                        Animation.RELATIVE_TO_SELF, 0,
                                        Animation.RELATIVE_TO_SELF, 1,
                                        Animation.RELATIVE_TO_SELF, 0);
                                translate2.setDuration(500);
                                int top = view.getTop();
                                int childCount = mListView.getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                    View childView = mListView.getChildAt(i);
                                    if (childView.getTop() >= top) {
                                        childView.startAnimation(translate2);
                                    }
                                }
//                                mHandler.sendMessageDelayed(Message.obtain(mHandler,202,position),1000);
                                data.remove(position - 2);
                                toutiaoAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }
                });
                builder.setPositiveButton("取消", null);
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

    private Bitmap getCache(String path) {
        String img = path.substring(path.lastIndexOf("/") + 1);
        Bitmap bitmap = mMyLruCache.get(img);
        if (bitmap != null) {
            return bitmap;
        } else {
            String root = getContext().getExternalCacheDir().getAbsolutePath();
            byte[] byteFromCache = SdCardUtils.getByteFromCache(root, img);
            if (byteFromCache != null) {
                Bitmap image = BitmapFactory.decodeByteArray(byteFromCache, 0, byteFromCache.length);
                mMyLruCache.put(img, image);
                return image;
            }
        }
        return null;
    }

    private void initData() {
        path = beforePath + page;
        if (InternetUtils.isConnected(getContext())) {
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    TouTiao touTiao = JSON.parseObject(new String(ret), TouTiao.class);
                    String root = getContext().getExternalCacheDir().getAbsolutePath();
                    String fileName = "toutiao" + page;
                    SdCardUtils.saveToCache(root, fileName, ret);
                    data.addAll(touTiao.getData());
                    toutiaoAdapter.notifyDataSetChanged();
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                        mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间:" + simpleDateFormat.format(new Date()));
                        mFootView.setVisibility(View.VISIBLE);
                    }
                }
            }).execute(path);
        } else {
            String root = getContext().getExternalCacheDir().getAbsolutePath();
            String fileName = "toutiao" + page;
            byte[] byteFromCache = SdCardUtils.getByteFromCache(root, fileName);
            if (byteFromCache != null) {
                TouTiao touTiao = JSON.parseObject(new String(byteFromCache), TouTiao.class);
                data.addAll(touTiao.getData());
                toutiaoAdapter.notifyDataSetChanged();
            }
            if (mPullToRefreshListView.isRefreshing()) {
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
                                    mLoadingLayoutProxy.setLastUpdatedLabel("上次更新时间:" + simpleDateFormat.format(new Date()));
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

    private void initView(View ret) {
        mPullToRefreshListView = (PullToRefreshListView) ret.findViewById(R.id.toutiaoListView);
        mImageView = (ImageView) ret.findViewById(R.id.backToTop);
    }

}
