package com.hzh.chabaike;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hzh.chabaike.Uri.Constants;
import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.net.ByteAsyncTask;
import com.hzh.chabaike.utils.InternetUtils;

public class CollectionWebActivity extends AppCompatActivity {

    private WebView mWebView;
    private TextView title,creat_time,source;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_web);
        getSupportActionBar().hide();
        initView();
        initData();
    }

    private void initData() {
        Bundle extras = getIntent().getExtras();
        String id = extras.getString("id");
        String path = Constants.CONTENT_URL+ id;
        String title = extras.getString("title");
        String create_time = extras.getString("create_time");
        String source = extras.getString("source");
        this.title.setText(title);
        this.creat_time.setText("时间:"+create_time);
        this.source.setText("来源:"+source);
        if(InternetUtils.isConnected(this)){
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    com.hzh.chabaike.beans.WebView webView = JSON.parseObject(new String(ret), com.hzh.chabaike.beans.WebView.class);
                    mWebView.loadUrl(webView.getData().getWeiboUrl());
                }
            }).execute(path);
        }else{
            Toast.makeText(this,"网络状况异常，请稍后重试!",Toast.LENGTH_LONG).show();
        }
    }

    private void initView() {
        title = (TextView) findViewById(R.id.title);
        creat_time = (TextView) findViewById(R.id.creat_time);
        source = (TextView) findViewById(R.id.web_source);
        mWebView = (WebView) findViewById(R.id.webView);
    }
}
