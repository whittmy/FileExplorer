package fileexplorer.lemoon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class ImageActivity extends Activity {
	ImageView mImg;
	private ProgressDialog dialog = null;
	Button mBtnExit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_image);

		dialog = new ProgressDialog(this);
		dialog.setMessage("正在努力加载...");
		dialog.setCanceledOnTouchOutside(false);

		mImg = (ImageView) findViewById(R.id.photo);
		mBtnExit = (Button) findViewById(R.id.fp_exit);
		mBtnExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		String path = getIntent().getStringExtra("path");
		loadBitmap(path, mImg);
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		// 在后台加载图片。
		ImageView img;

		public BitmapWorkerTask(ImageView i) {
			img = i;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog.show();
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			final Bitmap bitmap = getLoacalBitmap(params[0]);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			// super.onPostExecute(result);
			if (result != null)
				img.setImageBitmap(result);
			dialog.cancel();
		}
	}

	public void loadBitmap(String path, ImageView imageView) {
		BitmapWorkerTask task = new BitmapWorkerTask(imageView);
		task.execute(path);
	}

	/**
	 * 加载本地图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis); // /把流转化为Bitmap图片

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_UP){
			if(mBtnExit.getVisibility() == View.INVISIBLE){
				mBtnExit.setVisibility(View.VISIBLE);
			}
			else{
				mBtnExit.setVisibility(View.INVISIBLE);
			}
		}
		return super.onTouchEvent(event);
	}
}
