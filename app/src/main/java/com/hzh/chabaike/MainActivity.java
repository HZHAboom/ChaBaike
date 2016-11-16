package com.hzh.chabaike;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzh.chabaike.Uri.Constants;
import com.hzh.chabaike.adapters.MyFragmentPagerAdapter;
import com.hzh.chabaike.fragments.OtherFragment;
import com.hzh.chabaike.fragments.TouTiaoFragment;
import com.hzh.chabaike.sql.MySQLiteOpenHelper;
import com.softpo.viewpagertransformer.RotateUpTransformer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private ImageView more;
    private ImageView backToMain;
    private EditText mEditText;
    private ImageView search;
    private MySQLiteOpenHelper mMySQLiteOpenHelper;
    private TextView myCollection,history;
    private List<Fragment> mFragments = new ArrayList<>();
    private long mExitTime;
    private ImageView backToHome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mMySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        initView();
        initDrawerLayout();
        initViewPager();
        initTabLayout();
    }

    private void initDrawerLayout() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerView.setClickable(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void initTabLayout() {
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initViewPager() {
        Fragment toutiaoFragment = new TouTiaoFragment();
        mFragments.add(toutiaoFragment);
        for (int i = 0; i < 4; i++) {
            Fragment otherFragment = new OtherFragment();
            Bundle bundle = new Bundle();
            if (i==0){
                bundle.putString("path", Constants.BASE_URL+"&type=16&rows=15&page=");
            }else{
                bundle.putString("path",Constants.BASE_URL+"&type="+(51+i)+"&rows=15&page=");
            }
            otherFragment.setArguments(bundle);
            mFragments.add(otherFragment);
        }
        String[] titles = new String[]{"头条","百科","资讯","经营","数据"};
        FragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),mFragments,titles);
        mViewPager.setAdapter(adapter);
        mViewPager.setPageTransformer(false, new RotateUpTransformer());
    }

    private void initView() {
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        more = (ImageView) findViewById(R.id.more);
        backToMain = (ImageView) findViewById(R.id.backToMain);
        mEditText = (EditText) findViewById(R.id.editText);
        search = (ImageView) findViewById(R.id.search);
        myCollection = (TextView) findViewById(R.id.myCollection);
        history = (TextView) findViewById(R.id.history);
        backToHome = (ImageView) findViewById(R.id.backToHome);
        more.setOnClickListener(this);
        backToMain.setOnClickListener(this);
        search.setOnClickListener(this);
        myCollection.setOnClickListener(this);
        history.setOnClickListener(this);
        backToHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.more:
                mDrawerLayout.openDrawer(Gravity.RIGHT);
                break;
            case R.id.backToMain:
                mDrawerLayout.closeDrawer(Gravity.RIGHT);
                break;
            case R.id.search:
                String text = mEditText.getText().toString();
                //编码
                String encode = null;
                try {
                   encode = URLEncoder.encode(text, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                String path = Constants.SEARCH_URL + encode;
                Intent intent = new Intent(this,SearchActivity.class);
                bundle.putString("name",text);
                bundle.putString("path",path);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.myCollection:
                Intent collection = new Intent(this,MyCollectionActivity.class);
                collection.putExtra("isCollection",true);
                startActivity(collection);
                break;
            case R.id.history:
                Intent history = new Intent(this,MyCollectionActivity.class);
                history.putExtra("isCollection",false);
                startActivity(history);
                break;
            case R.id.backToHome:
                Intent back = new Intent();
                back.setAction(Intent.ACTION_MAIN);
                back.addCategory(Intent.CATEGORY_HOME);
                startActivity(back);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis()-mExitTime>2000){
                Toast.makeText(this,"再按一次退出茶百科",Toast.LENGTH_LONG).show();
                mExitTime = System.currentTimeMillis();
            }else{
                finish();
                System.exit(0);
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    public ViewPager getViewPager_main() {
        return mViewPager;
    }
}
