package com.nono.nlpullrefreshviewdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.nono.nlpullrefreshviewdemo.NLPullRefreshView.RefreshListener;

/**
 * 
 * @author Nono
 *
 */
public class RefreshScrollActivity extends Activity implements RefreshListener {
    /** Called when the activity is first created. */
	private NLPullRefreshView mRefreshableView;
	private Context mContext;
	
	
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			mRefreshableView.finishRefresh();
			Toast.makeText(mContext, "刷新完成", Toast.LENGTH_SHORT).show();
		};
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll);
        mContext = this;
        init();
    }
	private void init() {
		// TODO Auto-generated method stub
		initView();
	}
	private void initView() {
		// TODO Auto-generated method stub
		mRefreshableView = (NLPullRefreshView) findViewById(R.id.refresh_root);
		initData();
	}
	private void initData() {
		mRefreshableView.setRefreshListener(this);
		
	}
	
	//实现刷新RefreshListener 中方法
	public void onRefresh(NLPullRefreshView view) {
		//伪处理
		handler.sendEmptyMessageDelayed(1, 2000);
		
	}
}