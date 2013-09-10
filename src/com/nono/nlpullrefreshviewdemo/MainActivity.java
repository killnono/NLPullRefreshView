package com.nono.nlpullrefreshviewdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nono.nlpullrefreshviewdemo.NLPullRefreshView.RefreshListener;
/**
 * 
 * Copyright (c) 2013 Nono_Lilith All right reserved.
 * -------------------------------------------------------
 * @Description: MainActivity
 * @Author: Nono
 * @CreateDate: 2013-4-9 下午2:21:04
 * @version 1.0.0
 *
 */
public class MainActivity extends Activity implements RefreshListener {
	
	private Context mContext;
	private NLPullRefreshView mPullRefreshView ;
	private ListView mListView ;
	
	
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			mPullRefreshView.finishRefresh();
			refreshCount++;
			((MyAdapter)(mListView.getAdapter())).notifyDataSetChanged();
			Toast.makeText(mContext, "数据刷新", Toast.LENGTH_SHORT).show();
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		
	}

	private void init() {
		mContext = this;
		mPullRefreshView = (NLPullRefreshView) findViewById(R.id.refresh_root);
		mListView = (ListView) findViewById(R.id.lottery_list);
		mPullRefreshView.setRefreshListener(this);
		mListView.setAdapter(new MyAdapter());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	static int refreshCount = 0;//数据刷新标记
	
	
	/**
	 * 
	 * @CreateDate: 2013-4-10 下午1:35:41
	 * @version 1.0.0
	 *
	 */
	class MyAdapter extends BaseAdapter{
		
		
		int size = 20;
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return size;
		}
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			String tag="第"+refreshCount+"刷新:";
			if (arg1 == null) {
				arg1 = new TextView(mContext);			
			}
			
			((TextView)arg1).setText(tag+arg0+"---点击查看scrollView的下拉刷新");
			((TextView)arg1).setTextSize(20f);
			arg1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mContext, RefreshScrollActivity.class);
					mContext.startActivity(intent);	
				}
			});
			return arg1;
		}
		
	}
	
/*-----------刷新接口方法实现-------	*/
	static int  count = 0;
	@Override
	public void onRefresh(NLPullRefreshView view) {
		//伪处理
	  handler.sendEmptyMessageDelayed(1, 5000);
		
	}


	
}
