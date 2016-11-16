package com.hzh.chabaike;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.hzh.chabaike.adapters.ToutiaoAdapter;
import com.hzh.chabaike.beans.TouTiao;
import com.hzh.chabaike.callback.ByteCallback;
import com.hzh.chabaike.net.ByteAsyncTask;
import com.hzh.chabaike.sql.MySQLiteOpenHelper;
import com.hzh.chabaike.utils.InternetUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private ImageView mImageView,backToHome;
    private TextView mTextView;
    private ListView mListView;
    private String name,path;
    private List<TouTiao.DataBean> data = new ArrayList<>();
    private BaseAdapter mAdapter;
    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();
        mHelper = new MySQLiteOpenHelper(this);
        db = mHelper.getReadableDatabase();
        Bundle extras = getIntent().getExtras();
        path = extras.getString("path");
        name = extras.getString("name");
        initView();
        initData();
        initListView();
    }

    private void initListView() {
        mAdapter = new ToutiaoAdapter(data,this);
        mListView.setAdapter(mAdapter);
    }

    private void initData() {
        if(InternetUtils.isConnected(this)){
            new ByteAsyncTask(new ByteCallback() {
                @Override
                public void callback(byte[] ret) {
                    TouTiao touTiao = JSON.parseObject(new String(ret), TouTiao.class);
                    data.addAll(touTiao.getData());
                    mAdapter.notifyDataSetChanged();
                }
            }).execute(path);
        }
    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.backToLast);
        mTextView = (TextView) findViewById(R.id.search_title);
        mListView = (ListView) findViewById(R.id.searchListView);
        backToHome = (ImageView) findViewById(R.id.backToHome);
        mTextView.setText(name);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = db.query("history", new String[]{"_id"}, null, null, null, null, null);
                boolean iscollected = false;
                while (cursor.moveToNext()){
                    String _id = cursor.getString(cursor.getColumnIndex("_id"));
                    if(data.get(position).getId().equals(_id)){
                        iscollected = true;
                        break;
                    }
                }
                if(!iscollected){
                    ContentValues values = new ContentValues();
                    values.put("_id",data.get(position).getId());
                    values.put("title",data.get(position).getTitle());
                    values.put("source",data.get(position).getSource());
                    values.put("nickname",data.get(position).getNickname());
                    values.put("create_time",data.get(position).getCreate_time());
                    db.insert("history",null,values);
                }
                cursor.close();
                Intent intent = new Intent(SearchActivity.this, WebActivity.class);
                intent.putExtra("id",data.get(position).getId());
                startActivity(intent);
            }
        });
    }
}
