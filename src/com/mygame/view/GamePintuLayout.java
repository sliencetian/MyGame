package com.mygame.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.mygame.R;
import com.mygame.utils.ImagePiece;
import com.mygame.utils.ImageSplitterUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

;
public class GamePintuLayout extends RelativeLayout implements OnClickListener {
	// 行数
	private int mColumn = 3;
	// 容器的内边距
	private int mPadding;
	// 每张小图之间的距离
	private int mMagin = 3;
	// 容器的宽度
	private int mWidth;

	public interface GamePintuListener {
		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void gomeOver();
	}

	public GamePintuListener mListener;

	/**
	 * 设置接口回调
	 * 
	 * @param mListener
	 */
	public void setOnGamePintuListener(GamePintuListener mListener) {
		this.mListener = mListener;
	}

	private static final int TIME_CHANGED = 0x110;
	private static final int NEXT_LEVEL = 0x111;

	private boolean isGameSuccess = false;
	private boolean isGameOver = false;
	private int mLevel = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case TIME_CHANGED:
				if (isGameSuccess || isGameOver || isPause) {
					return;
				}
				if (mListener != null) {
					mListener.timeChanged(mTime);

					if (mTime == 0) {
						isGameOver = true;
						mListener.gomeOver();
						return;
					}
				}

				mTime--;
				mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
				break;
			case NEXT_LEVEL:
				mLevel = mLevel + 1;
				if (mListener != null) {
					mListener.nextLevel(mLevel);
				} else {
					nextLevel();
				}
				break;
			}
		};
	};

	private ImageView[] mGamePintuItems;
	private int mItemWidth;// 每个Item的宽度和高度

	// 游戏的图片
	private Bitmap mBitmap;
	private List<ImagePiece> mItemBitmaps;
	
	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public GamePintuLayout(Context context) {
		this(context, null);
	}

	public GamePintuLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GamePintuLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mMagin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				3, getResources().getDisplayMetrics());
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(),
				getPaddingBottom());

	}

	private boolean once;
	// 设置是否开启时间
	private boolean isTimeEnable = false;
	private int mTime;

	public void setTimeEnable(boolean isTimeEnable) {
		this.isTimeEnable = isTimeEnable;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 取宽和高的最小值
		mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
		if (!once) {
			// 进行切图以及排序
			initBitmap();
			// 设置ImageView（Item）的宽和高等属性
			initItem();
			// 判断是否开启时间
			checkTimeEnable();

			once = true;
		}
		setMeasuredDimension(mWidth, mWidth);
	}

	private void checkTimeEnable() {
		if (isTimeEnable) {
			countTimeBaseLevel();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	private void countTimeBaseLevel() {
		mTime = (int) Math.pow(2, mLevel) * 60;
	}

	// 设置ImageView（Item）的宽和高
	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMagin * (mColumn - 1)) / mColumn;
		mGamePintuItems = new ImageView[mColumn * mColumn];
		// 生成Item，设置Rule（每个Item之间的关系）
		for (int i = 0; i < mGamePintuItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
			mGamePintuItems[i] = item;
			item.setId(i + 1);
			// 在Item的tag中存储了Index
			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// 不是最后一列，设置右边距，通过rightMargin
			if ((i + 1) % mColumn != 0) {
				lp.rightMargin = mMagin;
			}
			// 不是第一列
			if (i % mColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF,
						mGamePintuItems[i - 1].getId());
			}
			// 不是第一行,设置topMargin，和Rule
			if ((i + 1) > mColumn) {
				lp.topMargin = mMagin;
				lp.addRule(RelativeLayout.BELOW,
						mGamePintuItems[i - mColumn].getId());
			}
			addView(item, lp);
		}
	}

	// 进行切图以及排序
	private void initBitmap() {
		if (mBitmap == null) {
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.yangmi);
		}
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		// 将图片乱序
		Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {

			@Override
			public int compare(ImagePiece arg0, ImagePiece arg1) {
				return Math.random() > 0.5 ? 1 : -1;
			}
		});
	}

	// 进入下一关
	public void nextLevel() {
		this.removeAllViews();
		mAnimLayout = null;
		mColumn++;
		isGameSuccess = false;
		checkTimeEnable();
		initBitmap();
		initItem();
	}

	// 重新开始
	public void restart() {
		isGameOver = false;
		mColumn--;
		nextLevel();
	}

	/**
	 * 获取多个参数的最小值
	 * 
	 * @param params
	 * @return
	 */
	private int min(int... params) {
		int min = params[0];
		for (int param : params) {
			if (param < min)
				min = param;
		}
		return min;
	}

	private ImageView mFirst;
	private ImageView mSecond;

	@Override
	public void onClick(View v) {
		// 动画是否正在执行
		if (isAniming) {
			return;
		}
		// 两次点击同一个图片
		if (mFirst == v) {
			mFirst.setColorFilter(null);
			mFirst = null;
			return;
		}
		if (mFirst == null) {
			mFirst = (ImageView) v;
			mFirst.setColorFilter(Color.parseColor("#55FF0000"));
		} else {
			mSecond = (ImageView) v;
			// 交换Item
			exchangeView();
		}
	}

	/**
	 * 动画层
	 */
	private RelativeLayout mAnimLayout;
	private boolean isAniming = false;

	/**
	 * 交换Item
	 */
	private void exchangeView() {
		//设置高亮
		mFirst.setColorFilter(null);
		// 构造动画层
		setUpAnimLayout();
		//复制view1
		ImageView first = new ImageView(getContext());
		final Bitmap firstBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mFirst.getTag())).getBitmap();
		first.setImageBitmap(firstBitmap);
		LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
		lp.leftMargin = mFirst.getLeft() - mPadding;
		lp.topMargin = mFirst.getTop() - mPadding;
		first.setLayoutParams(lp);
		mAnimLayout.addView(first);
		//复制view2
		ImageView second = new ImageView(getContext());
		final Bitmap secondBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mSecond.getTag())).getBitmap();
		second.setImageBitmap(secondBitmap);
		LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
		lp2.leftMargin = mSecond.getLeft() - mPadding;
		lp2.topMargin = mSecond.getTop() - mPadding;
		second.setLayoutParams(lp2);
		mAnimLayout.addView(second);

		// 设置动画
		TranslateAnimation animFirst = new TranslateAnimation(0,
				mSecond.getLeft() - mFirst.getLeft(), 0, mSecond.getTop()
						- mFirst.getTop());
		animFirst.setDuration(300);
		animFirst.setFillAfter(true);
		first.setAnimation(animFirst);

		TranslateAnimation animSecond = new TranslateAnimation(0,
				-mSecond.getLeft() + mFirst.getLeft(), 0, -mSecond.getTop()
						+ mFirst.getTop());
		animSecond.setDuration(300);
		animSecond.setFillAfter(true);
		second.setAnimation(animSecond);

		// 监听动画
		animFirst.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
				mFirst.setVisibility(View.INVISIBLE);
				mSecond.setVisibility(View.INVISIBLE);
				isAniming = true;
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {

				String firstTag = (String) mFirst.getTag();
				String secondTag = (String) mSecond.getTag();

				mFirst.setImageBitmap(secondBitmap);
				mSecond.setImageBitmap(firstBitmap);

				mFirst.setTag(secondTag);
				mSecond.setTag(firstTag);

				mFirst.setVisibility(View.VISIBLE);
				mSecond.setVisibility(View.VISIBLE);

				mFirst = null;
				mSecond = null;

				mAnimLayout.removeAllViews();

				// 判断游戏是否成功
				checkSuccess();

				isAniming = false;
			}
		});

	}

	// 判断游戏是否成功
	private void checkSuccess() {
		boolean isSuccess = true;
		for (int i = 0; i < mGamePintuItems.length; i++) {
			ImageView imageView = mGamePintuItems[i];
			System.out.println("index--->"
					+ getImageIndex((String) imageView.getTag()));
			if (getImageIndex((String) imageView.getTag()) != i) {
				// System.out.println("没有过关....");
				isSuccess = false;
				break;
			}
		}
		if (isSuccess) {
			isGameSuccess = true;
			mHandler.removeMessages(TIME_CHANGED);
			mHandler.sendEmptyMessage(NEXT_LEVEL);
		}
	}

	/**
	 * @param tag
	 * @return根据tag获得Id
	 */
	public int getImageIdByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[0]);
	}

	/**
	 * @param tag
	 * @return根据tag获得index
	 */
	public int getImageIndex(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[1]);
	}

	/**
	 * 构造动画层
	 */
	private void setUpAnimLayout() {
		if (mAnimLayout == null) {
			mAnimLayout = new RelativeLayout(getContext());
			addView(mAnimLayout);
		}
	}

	private boolean isPause;

	public void pause() {
		isPause = true;
		mHandler.removeMessages(TIME_CHANGED);
	}

	public void resume() {
		if (isPause) {
			isPause = false;
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}
}
