package fileexplorer.lemoon;

import java.io.IOException;

  
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicDlg extends Dialog {
	MediaPlayer mPlayer;
	Context mCx;
	static MusicDlg dlg;
	SeekBar seekBar;
	private boolean isStartTrackingTouch; 
	private Handler handler = new Handler();
	
	public MusicDlg(Context cx){
		super(cx);
		mCx = cx;
		
		init();
	}
	
	public MusicDlg(Context context, int theme) {
        super(context, theme);
        this.mCx = context;
        
        init();
    }
	
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		if(mPlayer != null){
			handler.removeCallbacks(r);
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		
		super.dismiss();
	}
 
	public void setTitle(String str){
		if(str == null)
			str = "";
		TextView t = (TextView) findViewById(R.id.music_title);
		t.setText(str);
	}
	
	Runnable r = new Runnable() {  
        public void run() {  
            // 更新进度条状态   
            if (!isStartTrackingTouch && seekBar!=null)  
                seekBar.setProgress(mPlayer.getCurrentPosition());  
            // 1秒之后再次发送   
            handler.postDelayed(this, 1000);  
        }  
    };
	
	public void setUrl(String url){
		mPlayer = new MediaPlayer();
		mPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mPlayer.start();
				
				seekBar.setMax(mPlayer.getDuration());
				
				 //发送一个Runnable, handler收到之后就会执行run()方法   
	            handler.post(r);  
			}
		});
		
		mPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
		
		try {
			mPlayer.reset();
			mPlayer.setDataSource(url);
			mPlayer.prepareAsync();

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void init(){
		setContentView(R.layout.musicdlg);
		
		Window dialogWindow = getWindow();
	    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
	    lp.gravity =  Gravity.CENTER;
        dialogWindow.setAttributes(lp);
		
		
		seekBar = (SeekBar) findViewById(R.id.seekBar);  
		//进度条监听器   
        seekBar.setOnSeekBarChangeListener(new MySeekBarListener()); 
	}
 
	private final class MySeekBarListener implements OnSeekBarChangeListener {  
        //移动触发   
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {  
        }  
  
        //起始触发   
        public void onStartTrackingTouch(SeekBar seekBar) {  
            isStartTrackingTouch = true;  
        }  
  
        //结束触发   
        public void onStopTrackingTouch(SeekBar seekBar) {  
            mPlayer.seekTo(seekBar.getProgress());  
            isStartTrackingTouch = false;  
        }  
    }  
	
	public static MusicDlg createDialog(Context context){
		dlg = new MusicDlg(context,R.style.MusicDialog);
		dlg.setCanceledOnTouchOutside(true);
		dlg.setCancelable(true);
		return dlg;
	}
	
	
}
