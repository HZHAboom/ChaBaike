package com.hzh.chabaike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private TextView mTextView;
    private int currention = 2;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    mTextView.setText(currention+"秒后自动进入");
                    if(currention>0){
                        this.sendEmptyMessageDelayed(1,1000);
                    }else{
                        this.sendEmptyMessage(2);
                    }
                    currention--;
                    break;
                case 2:
                    if(mIsFirst){
                        startActivity(new Intent(SplashActivity.this,GuideActivity.class));
                        SplashActivity.this.finish();
                    }else{
                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                        SplashActivity.this.finish();
                    }
                    break;
            }
        }
    };
    private boolean mIsFirst;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        SharedPreferences sp = getSharedPreferences("appconfig",MODE_PRIVATE);
        mIsFirst = sp.getBoolean("isFirst",true);
        initView();
        ComeToActivity();
    }

    private void ComeToActivity() {
        mHandler.sendEmptyMessageDelayed(1,1000);
    }

    private void initView() {
        mTextView = (TextView) findViewById(R.id.countDown);
    }
}
