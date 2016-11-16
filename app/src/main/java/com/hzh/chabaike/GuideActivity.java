package com.hzh.chabaike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hzh.chabaike.adapters.MyPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class GuideActivity extends AppCompatActivity {

    private LinearLayout mLinearLayout;
    private ViewPager mViewPager;
    private Button button;
    private int lastCurrention = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        getSupportActionBar().hide();
        initView();
        mLinearLayout.getChildAt(lastCurrention).setEnabled(false);
        initViewPager();
    }

    private void initViewPager() {
        List<ImageView> data = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(getResources().getIdentifier("slide"+(i+1),"mipmap",getPackageName()));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            data.add(imageView);
        }
        PagerAdapter adapter = new MyPagerAdapter(data);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mLinearLayout.getChildAt(lastCurrention).setEnabled(true);
                mLinearLayout.getChildAt(position).setEnabled(false);
                lastCurrention=position;
                if(position==2){
                    button.setVisibility(View.VISIBLE);
                }else{
                    button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.guideViewPager);
        mLinearLayout = (LinearLayout) findViewById(R.id.guideLinear);
        button = (Button) findViewById(R.id.button);
    }

    public void toActivity(View view) {
        SharedPreferences sp = getSharedPreferences("appconfig",MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean("isFirst",false);
        edit.commit();
        startActivity(new Intent(GuideActivity.this,MainActivity.class));
        GuideActivity.this.finish();
    }
}
