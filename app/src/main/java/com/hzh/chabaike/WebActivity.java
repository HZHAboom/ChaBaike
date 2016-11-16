package com.hzh.chabaike;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hzh.chabaike.Uri.Constants;
import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.net.ByteAsyncTask;
import com.hzh.chabaike.sql.MySQLiteOpenHelper;
import com.hzh.chabaike.utils.InternetUtils;

import java.util.ArrayList;
import java.util.List;

public class WebActivity extends AppCompatActivity {

    private TextView title,creat_time,source;
    private WebView mWebView;
    private String path;
    private MySQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;
    private String mId;
    private List<com.hzh.chabaike.beans.WebView.DataBean> data = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        getSupportActionBar().hide();
        dbHelper = new MySQLiteOpenHelper(this);
        db = dbHelper.getReadableDatabase();
        initView();
        mId = getIntent().getStringExtra("id");
        path = Constants.CONTENT_URL+ mId;
        initData();
    }

    private void initData() {
        if(InternetUtils.isConnected(this)){
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    com.hzh.chabaike.beans.WebView webView = JSON.parseObject(new String(ret), com.hzh.chabaike.beans.WebView.class);
                    data.add(webView.getData());
                    title.setText(webView.getData().getTitle());
                    creat_time.setText("时间:"+webView.getData().getCreate_time());
                    source.setText("来源:"+webView.getData().getSource());
                    mWebView.loadUrl(webView.getData().getWeiboUrl());
                }
            }).execute(path);
        }else{
            Toast.makeText(WebActivity.this,"网络状况异常",Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        title = (TextView) findViewById(R.id.title);
        creat_time = (TextView) findViewById(R.id.creat_time);
        source = (TextView) findViewById(R.id.web_source);
        mWebView = (WebView) findViewById(R.id.webView);
    }

    public void click(View view) {
        switch (view.getId()){
            case R.id.back:
                this.finish();
                break;
            case R.id.share:

                break;
            case R.id.collect:
                if (InternetUtils.isConnected(this)){
                    Cursor cursor = db.query("collect", new String[]{"_id"}, null, null, null, null, null);
                    boolean iscollected = false;
                    while (cursor.moveToNext()){
                        String id = cursor.getString(cursor.getColumnIndex("_id"));
                        if(mId.equals(id)){
                            iscollected = true;
                            break;
                        }
                    }
                    if(iscollected){
                        Toast.makeText(this,"已收藏",Toast.LENGTH_LONG).show();
                    }else{
                        ContentValues values = new ContentValues();
                        values.put("_id",mId);
                        values.put("title",data.get(0).getTitle());
                        values.put("source",data.get(0).getSource());
                        values.put("nickname",data.get(0).getAuthor());
                        values.put("create_time",data.get(0).getCreate_time());
                        db.insert("collect",null,values);
                        Toast.makeText(this,"成功收藏",Toast.LENGTH_LONG).show();
                    }
                    cursor.close();
                }else{
                    Toast.makeText(this,"当前无网络连接，无法收藏",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}
