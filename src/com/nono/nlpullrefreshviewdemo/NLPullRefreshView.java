package com.nono.nlpullrefreshviewdemo;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Copyright (c) 2013 Nono_Lilith All right reserved.
 * -------------------------------------------------------
 * 
 * @Description: NLPullRefreshView
 * @Author: Nono(陈凯)
 * @CreateDate: 2013--9 下午1:44:17
 * @version 1.0.0
 * 
 */
public class NLPullRefreshView extends LinearLayout {
	private static final String TAG = "LILITH";

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	enum Status {
		NORMAL, DRAGGING, REFRESHING,
	}

	private Status status = Status.NORMAL;

	private final static String REFRESH_RELEASE_TEXT = "释放后执行刷新";
	private final static String REFRESH_DOWN_TEXT = "下拉可准备执行刷新";
	private final static float MIN_MOVE_DISTANCE = 5.0f;// 最小移动距离，用于判断是否在下拉，设置为0则touch事件的判断会过于平凡。具体值可以根据自己来设定

	private Scroller scroller;
	private View refreshView;
	private ImageView refreshIndicatorView;
	private int refreshTargetTop = -50;
	private ProgressBar bar;
	private TextView downTextView;
	private TextView timeTextView;

	private RefreshListener refreshListener;// 刷新监听器

	// 需要用到的文字引用
	private String downCanRefreshText;
	private String releaseCanRefreshText;

	private String refreshTime ;
	private int lastY;
	private Context mContext;

	public NLPullRefreshView(Context context) {
		super(context);
		mContext = context;
	}

	public NLPullRefreshView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	/**
	 * 初始化
	 * 
	 * @MethodDescription init
	 * @exception
	 * @since 1.0.0
	 */
	private void init() {
		// TODO Auto-generated method stub
		refreshTargetTop = getPixelByDip(mContext, refreshTargetTop);
		// 滑动对象，
		scroller = new Scroller(mContext);
		// 刷新视图顶端的的view
		refreshView = LayoutInflater.from(mContext).inflate(
				R.layout.refresh_top_item, null);
		// 指示器view
		refreshIndicatorView = (ImageView) refreshView
				.findViewById(R.id.indicator);
		// 刷新bar
		bar = (ProgressBar) refreshView.findViewById(R.id.progress);
		// 下拉显示text
		downTextView = (TextView) refreshView.findViewById(R.id.refresh_hint);
		// 下来显示时间
		timeTextView = (TextView) refreshView.findViewById(R.id.refresh_time);
		LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, -refreshTargetTop);
		lp.topMargin = refreshTargetTop;
		lp.gravity = Gravity.CENTER;
		addView(refreshView, lp);
		// //文字资源可以归档在资源集中，此处为了方便。
		downCanRefreshText = REFRESH_DOWN_TEXT;
		releaseCanRefreshText = REFRESH_RELEASE_TEXT;
		refreshTime = "1989-12-24 12:12:12";//可以从保存文件中取得上次的更新时间
		if (refreshTime != null) {
			setRefreshTime(refreshTime);
		}
	}

	/**
	 * 设置刷新后的内容
	 * 
	 * @MethodDescription setRefreshText
	 * @param time
	 * @exception
	 * @since 1.0.0
	 */
	private void setRefreshText(String time) {
		Log.i(TAG, "------>setRefreshText");
		timeTextView.setText(time);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (status == Status.REFRESHING)
			return false;
		
		int y = (int) event.getRawY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "MotionEvent.ACTION_DOWN");
			// 记录下y坐标
			lastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i(TAG, "MotionEvent.ACTION_MOVE");
			// y移动坐标
			int m = y - lastY;
			doMovement(m);
			// 记录下此刻y坐标
			this.lastY = y;
			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "MotionEvent.ACTION_UP");
			fling();
			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		
		// layout截取touch事件
		int action = e.getAction();
		int y = (int) e.getRawY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			// y移动坐标
			int m = y - lastY;
			// 记录下此刻y坐标
			this.lastY = y;
			if (m > MIN_MOVE_DISTANCE && canScroll()) {
				Log.i(TAG, "canScroll");
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return false;
	}

	/**
	 * up事件处理
	 */
	private void fling() {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams lp = (LayoutParams) refreshView
				.getLayoutParams();

		if (lp.topMargin > 0) {// 拉到了触发可刷新事件
			status = Status.REFRESHING;
			Log.i(TAG, "fling ----->REFRESHING");
			refresh();
		} else {
			Log.i(TAG, "fling ----->NORMAL");
			status = Status.NORMAL;
			returnInitState();
		}
	}

	private void returnInitState() {
		// TODO Auto-generated method stub
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
				.getLayoutParams();
		int i = lp.topMargin;
		Log.i(TAG, "returnInitState top = "+i);
		scroller.startScroll(0, i, 0, refreshTargetTop);
		invalidate();
	}

	/**
	 * 执行刷新
	 * 
	 * @MethodDescription refresh
	 * @exception
	 * @since 1.0.0
	 */
	private void refresh() {
		// TODO Auto-generated method stub
		Log.i(TAG, " ---> refresh()");
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
				.getLayoutParams();
		int i = lp.topMargin;
		refreshIndicatorView.setVisibility(View.GONE);
		bar.setVisibility(View.VISIBLE);
		downTextView.setVisibility(View.GONE);
		scroller.startScroll(0, i, 0, 0 - i);
		invalidate();
		if (refreshListener != null) {
			refreshListener.onRefresh(this);
		}
	}

	/**
	 * 
	 */
	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {//scroll 动作还未结束
			Log.i(TAG, "----->computeScroll()");
			int i = this.scroller.getCurrY();
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
					.getLayoutParams();
			int k = Math.max(i, refreshTargetTop);
			lp.topMargin = k;
			this.refreshView.setLayoutParams(lp);
			postInvalidate();
		}
	}

	/**
	 * 下拉move事件处理
	 * 
	 * @param moveY
	 */
	private void doMovement(int moveY) {
		status = Status.DRAGGING;
		LinearLayout.LayoutParams lp = (LayoutParams) refreshView
				.getLayoutParams();
		float f1 = lp.topMargin;
		float f2 = moveY * 0.3F;// 以0.3比例拖动
		int i = (int) (f1 + f2);
		// 修改上边距
		lp.topMargin = i;
		// 修改后刷新
		refreshView.setLayoutParams(lp);
		refreshView.invalidate();
		invalidate();

		timeTextView.setVisibility(View.VISIBLE);
		downTextView.setVisibility(View.VISIBLE);
		refreshIndicatorView.setVisibility(View.VISIBLE);
		bar.setVisibility(View.GONE);
		if (lp.topMargin > 0) {
			downTextView.setText(releaseCanRefreshText);
			refreshIndicatorView.setImageResource(R.drawable.refresh_arrow_up);
		} else {
			downTextView.setText(downCanRefreshText);
			refreshIndicatorView
					.setImageResource(R.drawable.refresh_arrow_down);
		}

	}

	/**
	 * 设置刷新时间
	 * @MethodDescription setRefreshTime 
	 * @param refreshTime 
	 * @exception 
	 * @since  1.0.0
	 */
	public void setRefreshTime(String refreshTime){
		timeTextView.setText("更新于:"+refreshTime);
	}

	/**
	 * 设置监听
	 * @MethodDescription setRefreshListener 
	 * @param listener 
	 * @exception 
	 * @since  1.0.0
	 */
	public void setRefreshListener(RefreshListener listener) {
		this.refreshListener = listener;
	}

	/**
	 * 刷新时间
	 * 
	 * @param refreshTime2
	 */
	private void refreshTimeBySystem() {
		String dateStr = dateFormat.format(new Date());//可以将时间保存起来
		this.setRefreshText("更新于:"+dateStr);
		
	}

	/**
	 * 结束刷新事件
	 */
	public void finishRefresh() {
		Log.i(TAG, "------->finishRefresh()");
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.refreshView
				.getLayoutParams();
		int i = lp.topMargin;
		refreshIndicatorView.setVisibility(View.VISIBLE);//下拉箭头显示
		timeTextView.setVisibility(View.VISIBLE);//时间控件
		downTextView.setVisibility(VISIBLE);//下拉提示语控件
		refreshTimeBySystem();//修改时间；
		bar.setVisibility(GONE);
		scroller.startScroll(0, i, 0, refreshTargetTop);
		invalidate();
		status = Status.NORMAL;
	}

	/**
	 * 此方法兼容两种子布局的判断，listview，scrollview 主要作用是判断两个子View是否滚动到了最上面，若是，则表示此次touch
	 * move事件截取然后让layout来处理，来移动下拉视图，反之则不然
	 * 
	 * @MethodDescription canScroll
	 * @return
	 * @exception
	 * @since 1.0.0
	 */
	private boolean canScroll() {
		// TODO Auto-generated method stub
		View childView;
		if (getChildCount() > 1) {
			childView = this.getChildAt(1);
			if (childView instanceof ListView) {
				int top = ((ListView) childView).getChildAt(0).getTop();
				int pad = ((ListView) childView).getListPaddingTop();
				if ((Math.abs(top - pad)) < 3
						&& ((ListView) childView).getFirstVisiblePosition() == 0) {
					return true;
				} else {
					return false;
				}
			} else if (childView instanceof ScrollView) {
				if (((ScrollView) childView).getScrollY() == 0) {
					return true;
				} else {
					return false;
				}
			}

		}
		return false;
	}

	public static int getPixelByDip(Context c, int pix) {
		float f1 = c.getResources().getDisplayMetrics().density;
		float f2 = pix;
		return (int) (f1 * f2 + 0.5F);
	}

	/**
	 * 刷新监听接口
	 * 
	 * @author Nono
	 * 
	 */
	public interface RefreshListener {
		public void onRefresh(NLPullRefreshView view);
	}
	
	
}
