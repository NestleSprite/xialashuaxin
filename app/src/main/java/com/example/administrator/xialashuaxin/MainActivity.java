package com.example.administrator.xialashuaxin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private refreshlistview lv;
    private ArrayList<String> datas;
    private int ix=1;
    private int sbb=1;
    private MyAdapter adapter;
    // private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        lv = (refreshlistview) findViewById(R.id.lv);
        datas = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            datas.add("这是第 " + (i + 1) + " 条数据");
        }
        adapter=new MyAdapter();
        lv.setAdapter(adapter);
        lv.setRefreshListener(new refreshlistview.onRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        datas.add(0,"我是新加入的第 "+ix+" 个数据");
                        ix++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                lv.onRefreshComplete();

                            }
                        });

                    }
                }.start();



            }

            @Override
            public void loadMore() {
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(400);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        datas.add("我是底部加入的第 "+sbb+" 个数据");
                        sbb++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                                lv.onLoadComplete();

                            }
                        });

                    }
                }.start();

            }
        });








    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView=new TextView(parent.getContext());
            textView.setTextSize(20);

            textView.setText(datas.get(position));
            return textView;



        }
    }
}
