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
	// ����
	private int mColumn = 3;
	// �������ڱ߾�
	private int mPadding;
	// ÿ��Сͼ֮��ľ���
	private int mMagin = 3;
	// �����Ŀ��
	private int mWidth;

	public interface GamePintuListener {
		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void gomeOver();
	}

	public GamePintuListener mListener;

	/**
	 * ���ýӿڻص�
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
	private int mItemWidth;// ÿ��Item�Ŀ�Ⱥ͸߶�

	// ��Ϸ��ͼƬ
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
	// �����Ƿ���ʱ��
	private boolean isTimeEnable = false;
	private int mTime;

	public void setTimeEnable(boolean isTimeEnable) {
		this.isTimeEnable = isTimeEnable;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// ȡ��͸ߵ���Сֵ
		mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
		if (!once) {
			// ������ͼ�Լ�����
			initBitmap();
			// ����ImageView��Item���Ŀ�͸ߵ�����
			initItem();
			// �ж��Ƿ���ʱ��
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

	// ����ImageView��Item���Ŀ�͸�
	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMagin * (mColumn - 1)) / mColumn;
		mGamePintuItems = new ImageView[mColumn * mColumn];
		// ����Item������Rule��ÿ��Item֮��Ĺ�ϵ��
		for (int i = 0; i < mGamePintuItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
			mGamePintuItems[i] = item;
			item.setId(i + 1);
			// ��Item��tag�д洢��Index
			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// �������һ�У������ұ߾࣬ͨ��rightMargin
			if ((i + 1) % mColumn != 0) {
				lp.rightMargin = mMagin;
			}
			// ���ǵ�һ��
			if (i % mColumn != 0) {
				lp.addRule(RelativeLayout.RIGHT_OF,
						mGamePintuItems[i - 1].getId());
			}
			// ���ǵ�һ��,����topMargin����Rule
			if ((i + 1) > mColumn) {
				lp.topMargin = mMagin;
				lp.addRule(RelativeLayout.BELOW,
						mGamePintuItems[i - mColumn].getId());
			}
			addView(item, lp);
		}
	}

	// ������ͼ�Լ�����
	private void initBitmap() {
		if (mBitmap == null) {
			mBitmap = BitmapFactory.decodeResource(getResources(),
					R.drawable.yangmi);
		}
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		// ��ͼƬ����
		Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {

			@Override
			public int compare(ImagePiece arg0, ImagePiece arg1) {
				return Math.random() > 0.5 ? 1 : -1;
			}
		});
	}

	// ������һ��
	public void nextLevel() {
		this.removeAllViews();
		mAnimLayout = null;
		mColumn++;
		isGameSuccess = false;
		checkTimeEnable();
		initBitmap();
		initItem();
	}

	// ���¿�ʼ
	public void restart() {
		isGameOver = false;
		mColumn--;
		nextLevel();
	}

	/**
	 * ��ȡ�����������Сֵ
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
		// �����Ƿ�����ִ��
		if (isAniming) {
			return;
		}
		// ���ε��ͬһ��ͼƬ
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
			// ����Item
			exchangeView();
		}
	}

	/**
	 * ������
	 */
	private RelativeLayout mAnimLayout;
	private boolean isAniming = false;

	/**
	 * ����Item
	 */
	private void exchangeView() {
		//���ø���
		mFirst.setColorFilter(null);
		// ���춯����
		setUpAnimLayout();
		//����view1
		ImageView first = new ImageView(getContext());
		final Bitmap firstBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mFirst.getTag())).getBitmap();
		first.setImageBitmap(firstBitmap);
		LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
		lp.leftMargin = mFirst.getLeft() - mPadding;
		lp.topMargin = mFirst.getTop() - mPadding;
		first.setLayoutParams(lp);
		mAnimLayout.addView(first);
		//����view2
		ImageView second = new ImageView(getContext());
		final Bitmap secondBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mSecond.getTag())).getBitmap();
		second.setImageBitmap(secondBitmap);
		LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
		lp2.leftMargin = mSecond.getLeft() - mPadding;
		lp2.topMargin = mSecond.getTop() - mPadding;
		second.setLayoutParams(lp2);
		mAnimLayout.addView(second);

		// ���ö���
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

		// ��������
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

				// �ж���Ϸ�Ƿ�ɹ�
				checkSuccess();

				isAniming = false;
			}
		});

	}

	// �ж���Ϸ�Ƿ�ɹ�
	private void checkSuccess() {
		boolean isSuccess = true;
		for (int i = 0; i < mGamePintuItems.length; i++) {
			ImageView imageView = mGamePintuItems[i];
			System.out.println("index--->"
					+ getImageIndex((String) imageView.getTag()));
			if (getImageIndex((String) imageView.getTag()) != i) {
				// System.out.println("û�й���....");
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
	 * @return����tag���Id
	 */
	public int getImageIdByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[0]);
	}

	/**
	 * @param tag
	 * @return����tag���index
	 */
	public int getImageIndex(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[1]);
	}

	/**
	 * ���춯����
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
