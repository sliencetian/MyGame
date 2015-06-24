package com.mygame;

import com.mygame.view.GamePintuLayout;
import com.mygame.view.GamePintuLayout.GamePintuListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private GamePintuLayout mGamePintuLayout;
	private TextView mLevel;
	private TextView mTime;
	private ImageView mImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGamePintuLayout = (GamePintuLayout) findViewById(R.id.id_gamepintu);
		mLevel = (TextView) findViewById(R.id.id_level);
		mTime = (TextView) findViewById(R.id.id_time);
		mImage = (ImageView) findViewById(R.id.id_iv);
		mGamePintuLayout.setTimeEnable(true);
		mGamePintuLayout.setOnGamePintuListener(new GamePintuListener() {

			@Override
			public void timeChanged(int currentTime) {
				mTime.setText(currentTime + "");
			}

			@Override
			public void nextLevel(final int nextLevel) {
				new AlertDialog.Builder(MainActivity.this).setTitle("拼图")
						.setMessage("恭喜过关...")
						.setPositiveButton("下一关", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								mGamePintuLayout.nextLevel();
								mLevel.setText(nextLevel + "");
							}
						}).setNeutralButton("返回", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create().show();
			}

			@Override
			public void gomeOver() {
				new AlertDialog.Builder(MainActivity.this).setTitle("拼图")
						.setMessage("啊哦...失败了...")
						.setPositiveButton("重新开始", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
								mGamePintuLayout.restart();
							}
						}).setNeutralButton("返回", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								arg0.dismiss();
							}
						}).create().show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGamePintuLayout.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGamePintuLayout.pause();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Bitmap bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.yangmi);
			mGamePintuLayout.setmBitmap(bm);
			mImage.setImageBitmap(formatBitmap(bm));
			mGamePintuLayout.restart();
			break;
		case 2:
			Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
			intent1.setType("image/*");
			startActivityForResult(intent1, 2);
			break;
		case 3:
			Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent2, 1);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				Bitmap bitmap = (Bitmap) data.getExtras().get("data");
				mImage.setImageBitmap(formatBitmap(bitmap));
				mGamePintuLayout.setmBitmap(bitmap);
				mGamePintuLayout.restart();
			}
			break;
		case 2:
			if (resultCode == RESULT_OK) {
				ContentResolver resolver = getContentResolver();
				Uri uri = data.getData();
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver,
							uri);
					mImage.setImageBitmap(formatBitmap(bitmap)); 
					mGamePintuLayout.setmBitmap(bitmap);
					mGamePintuLayout.restart();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}
	public Bitmap formatBitmap(Bitmap bitmap){
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		w = Math.min(w, h);
		return Bitmap.createBitmap(bitmap, 0, 0,w,w);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "选择默认图片");
		menu.add(0, 2, 0, "从相册选择图片");
		menu.add(0, 3, 0, "拍照");
		return true;
	}
}
