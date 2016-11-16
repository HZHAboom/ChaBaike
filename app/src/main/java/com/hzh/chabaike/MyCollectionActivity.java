package com.hzh.chabaike;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hzh.chabaike.adapters.CollectAdapter;
import com.hzh.chabaike.beans.TouTiao;
import com.hzh.chabaike.sql.MySQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MyCollectionActivity extends AppCompatActivity {

    private ImageView mImageView,backToHome;
    private ListView mListView;
    private List<TouTiao.DataBean> data = new ArrayList<>();
    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase db;
    private CollectAdapter mAdapter;
    private TextView empty;
    private TextView shoucangjia;
    private boolean mIsCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);
        getSupportActionBar().hide();
        mHelper = new MySQLiteOpenHelper(this);
        db = mHelper.getReadableDatabase();
        mIsCollection = getIntent().getBooleanExtra("isCollection", true);
        initView();
        initData();
        initListView();
    }

    private void initListView() {
        mAdapter = new CollectAdapter(data,this);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(empty);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MyCollectionActivity.this,CollectionWebActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("id",data.get(position).getId());
                bundle.putString("title",data.get(position).getTitle());
                bundle.putString("source",data.get(position).getSource());
                bundle.putString("create_time",data.get(position).getCreate_time());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyCollectionActivity.this);
                builder.setIcon(R.mipmap.ic_logo);
                builder.setTitle(data.get(position).getTitle());
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder builderTwo = new AlertDialog.Builder(MyCollectionActivity.this);
                        builderTwo.setIcon(R.mipmap.ic_logo);
                        builderTwo.setTitle("提示:");
                        builderTwo.setMessage("确定要删除吗?");
                        builderTwo.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String deleteId = data.get(position).getId();
                                data.remove(position);
                                mAdapter.notifyDataSetChanged();
                                if(mIsCollection){
                                    db.delete("collect","_id = ?",new String[]{deleteId});
                                }else{
                                    db.delete("history","_id = ?",new String[]{deleteId});
                                }
                                Toast.makeText(MyCollectionActivity.this,"删除成功！",Toast.LENGTH_LONG).show();
                            }
                        });
                        builderTwo.setNegativeButton("取消",null);
                        builderTwo.create().show();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();

                return true;
            }
        });
    }

    private void initData() {
        Cursor cursor = null;
        if(mIsCollection){
            cursor = db.query("collect", null, null, null, null, null, null);
        }else{
            cursor = db.query("history",null,null,null,null,null,null);
        }
        while (cursor.moveToNext()){
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String source = cursor.getString(cursor.getColumnIndex("source"));
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            String create_time = cursor.getString(cursor.getColumnIndex("create_time"));
            TouTiao.DataBean dataBean = new TouTiao.DataBean();
            dataBean.setId(id);
            dataBean.setTitle(title);
            dataBean.setSource(source);
            dataBean.setNickname(nickname);
            dataBean.setCreate_time(create_time);
            data.add(dataBean);
        }
    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.backToLast);
        mListView = (ListView) findViewById(R.id.collectionListView);
        empty = (TextView) findViewById(R.id.empty);
        shoucangjia = (TextView) findViewById(R.id.shoucangjia);
        backToHome = (ImageView) findViewById(R.id.backToHome);
        if(mIsCollection){
            shoucangjia.setText("我的收藏");
        }else{
            shoucangjia.setText("历史访问记录");
        }
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCollectionActivity.this.finish();
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
    }
}
