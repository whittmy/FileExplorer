package fileexplorer.lemoon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
 

import fileexplorer.lemoon.FileInfo.FileType;
 

public class DevicesActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
	public static final String TAG = "VRLanShareListActivity";
	private ListAdapter fileListAdapter;

	MyBroadcastRecv broadcastRecv = null;
  
	
	// 已经从服务器获取的数据
	private long backKeyTime;
	private LinkedList<FileInfo> devsData = new LinkedList<FileInfo>();
	private ListView deviceList;
	private Button mBtnMyPC, mBtnExit;
  	private ProgressDialog dialog = null;
  	TextView mTvCurPath;
  	
  	FileInfo mCurInfo;
  	boolean isHome = true;
  	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		public void onReceive(Context cx, Intent intent) {
		}
	};
 
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.act_vr_fp_devs);
 
		dialog = new ProgressDialog(this);
		dialog.setMessage("正在努力加载...");
		dialog.setCanceledOnTouchOutside(false);

		mBtnMyPC = (Button) findViewById(R.id.fp_devs_mypc);
		mBtnMyPC.setOnClickListener(this);
		mBtnExit = (Button) findViewById(R.id.fp_exit);
		mBtnExit.setOnClickListener(this);
		
		mTvCurPath = (TextView)findViewById(R.id.curpath);
		
		deviceList = ((ListView) findViewById(R.id.fp_devs_list));
		fileListAdapter = new ListAdapter(this, devsData);
		deviceList.setAdapter(fileListAdapter);
		deviceList.setOnItemClickListener(this);
 
		new SearchTask().execute("home");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//exit();
		
		if(broadcastRecv != null){
			unregisterReceiver(broadcastRecv);
			broadcastRecv = null;
		}
	}
	
	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean dispatchKeyEvent(KeyEvent paramKeyEvent) {
		if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getAction() == 0)) {
			if(isHome){
				long l = System.currentTimeMillis();
				if (l - backKeyTime > 2000L) {
					Toast.makeText(getApplicationContext(), "再按一次退出", 0).show();
					backKeyTime = l;
					return true;
				}
				
				exit();
			}
			
			toParent(mCurInfo);
			//exit();
			
 
 			return false;
		}
		// cond1
		return super.dispatchKeyEvent(paramKeyEvent);
	}

	
	void toHome(){
		devsData.clear();
		
		FileInfo fsi = new FileInfo("内部空间", "/mnt/mediacard/", FileType.DIR);
		devsData.add(fsi);
		
		fsi = new FileInfo("我的U盘", "/mnt/usbhost1/", FileType.DIR);
		devsData.add(fsi);
		
		fsi = new FileInfo("TF卡", "/mnt/extsd/", FileType.DIR);
		devsData.add(fsi);
		
		mCurInfo = fsi;
		
		isHome  = true;
	}
	
	boolean isHome(String path){
		if(path == null)
			return false;
		
		if(!path.endsWith("/"))
			path = path + "/";
		if(path.equals("/mnt/mediacard/")
				|| path.equals("/mnt/usbhost1/")
				|| path.equals("/mnt/extsd/"))
			return true;
		return false;	
	}
	
	
	private void toParent(FileInfo info){		
		String path = info.mPath;
		if(isHome(path)){
			path = "home";
		}
		else{
			path = "/";
			String[] items = info.mPath.split("/");
			for(int i=0; i<(items.length-1); i++){
				if(items[i].isEmpty())
					continue;
				path = path + items[i] + "/";
			}				
		}

		new SearchTask().execute(path);
	}
	
	public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
		FileInfo info = (FileInfo) ((Adapter) paramAdapterView.getAdapter()).getItem(paramInt);
		if(info.mType == FileType.PARENT_DIR){
			toParent(info);
			return;
		}
		else if (info.mType == FileType.DIR) {
			new SearchTask().execute(info.mPath);
			return;
		}
		else if(info.mType == FileType.MUSIC){
			MusicDlg mMusicDlg;
			mMusicDlg = MusicDlg.createDialog(DevicesActivity.this);
			mMusicDlg.setTitle(info.mTitle);
			mMusicDlg.setUrl(info.mPath);
			mMusicDlg.show();
		}
		else if(info.mType == FileType.VIDEO){
			Intent it = new Intent();  
			it.setComponent(new ComponentName("children.lemoon", "children.lemoon.player.org.Player")); 
			it.putExtra("video_path", info.mPath);    
 			startActivity(it);  	 
		}
		else if(info.mType == FileType.PHOTO){
			Intent i = new Intent(DevicesActivity.this, ImageActivity.class);
			i.putExtra("path", info.mPath);
			startActivity(i);
		}
		else{
			
		}
 
 
		return;
	}

	protected void onPause() {
		super.onPause();
		//MobclickAgent.onPause(this);
	}

	protected void onResume() {
		super.onResume();
        regBroadcastRecv();
		fileListAdapter.notifyDataSetChanged();
	}
	
	//广播接收器注册
	private void regBroadcastRecv(){
		if(broadcastRecv != null)
			return;
		
        broadcastRecv = new MyBroadcastRecv();
        IntentFilter bFilter = new IntentFilter();
       // bFilter.addAction(Constant.personHasChangedAction);
        registerReceiver(broadcastRecv, bFilter);
	}
	
	   //=========================广播接收器==========================================================
    private class MyBroadcastRecv extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	}
 
    //=========================广播接收器结束==========================================================	
	
	
	class SearchTask extends AsyncTask<String, Void, Void> {
		HashMap<String, FileType> mExtSets = new HashMap<String, FileType>();
		String mCurPath = "";
		public SearchTask(){
			//video
			mExtSets.put("mpeg", FileType.VIDEO);		mExtSets.put("mpg", FileType.VIDEO);			mExtSets.put("dat", FileType.VIDEO);
			mExtSets.put("ra", FileType.VIDEO);			mExtSets.put("rm", FileType.VIDEO);			mExtSets.put("rmvb", FileType.VIDEO);
			mExtSets.put("mp4", FileType.VIDEO);			mExtSets.put("flv", FileType.VIDEO);			mExtSets.put("mov", FileType.VIDEO);
			mExtSets.put("qt", FileType.VIDEO);			mExtSets.put("asf", FileType.VIDEO);			mExtSets.put("wmv", FileType.VIDEO);
			mExtSets.put("avi", FileType.VIDEO);			mExtSets.put("3gp", FileType.VIDEO);			mExtSets.put("mkv", FileType.VIDEO);
			mExtSets.put("f4v", FileType.VIDEO);			mExtSets.put("m4v", FileType.VIDEO);			mExtSets.put("m4p", FileType.VIDEO);
			mExtSets.put("m2v", FileType.VIDEO);			mExtSets.put("dat", FileType.VIDEO);			mExtSets.put("xvid", FileType.VIDEO);
			mExtSets.put("divx", FileType.VIDEO);			mExtSets.put("vob", FileType.VIDEO);			mExtSets.put("mpv", FileType.VIDEO);
			mExtSets.put("mpeg4", FileType.VIDEO);			mExtSets.put("mpe", FileType.VIDEO);			mExtSets.put("mlv", FileType.VIDEO);
			mExtSets.put("ogm", FileType.VIDEO);			mExtSets.put("m2ts", FileType.VIDEO);			mExtSets.put("mts", FileType.VIDEO);
			mExtSets.put("ask", FileType.VIDEO);			mExtSets.put("trp", FileType.VIDEO);			mExtSets.put("tp", FileType.VIDEO);
			mExtSets.put("ts", FileType.VIDEO);
			
			//music
			mExtSets.put("wma"  ,FileType.MUSIC);			mExtSets.put("wav"  ,FileType.MUSIC);			mExtSets.put("mod"  ,FileType.MUSIC);
			mExtSets.put("ra"   ,FileType.MUSIC);			mExtSets.put("cd"   ,FileType.MUSIC);			mExtSets.put("md"   ,FileType.MUSIC);
			mExtSets.put("asf"  ,FileType.MUSIC);			mExtSets.put("aac"  ,FileType.MUSIC);			mExtSets.put("mp3"  ,FileType.MUSIC);
			mExtSets.put("vqf"  ,FileType.MUSIC);			mExtSets.put("flac" ,FileType.MUSIC);			mExtSets.put("ape"  ,FileType.MUSIC);
			mExtSets.put("mid"  ,FileType.MUSIC);			mExtSets.put("ogg"  ,FileType.MUSIC);			mExtSets.put("m4a"  ,FileType.MUSIC);
			mExtSets.put("aac"  ,FileType.MUSIC);			mExtSets.put("aiff" ,FileType.MUSIC);			mExtSets.put("au"   ,FileType.MUSIC);

			//zip
			mExtSets.put("zip", FileType.ZIP);		mExtSets.put("rar", FileType.ZIP);	mExtSets.put("tgz", FileType.ZIP);
			mExtSets.put("7z", FileType.ZIP);
			
			//pic
			mExtSets.put("bmp", FileType.PHOTO);			mExtSets.put("jpg", FileType.PHOTO);			mExtSets.put("jpeg", FileType.PHOTO);
			mExtSets.put("gif", FileType.PHOTO);			mExtSets.put("png", FileType.PHOTO);			mExtSets.put("tiff", FileType.PHOTO);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();
		}

		FileType getFileType(String ext){
			FileType exta = mExtSets.get(ext);
			if(exta == null)
				return FileType.FILE; 
			return exta;
		}
 
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			String mPath = params[0];
			if(mPath == null || mPath.isEmpty())
				return null;
			
			if(mPath.equals("home")){
				toHome();
				
				return null;
			}
			
			if(!mPath.endsWith("/")){
				mPath += "/";
			}

			File f = new File(mPath);
			if(!f.exists() || f.isFile())
				return null;


			devsData.clear();
			
			//上一级
			mCurPath = mPath;
			FileInfo fsi = new FileInfo("<< 返回上一级 <<",	mPath, FileType.PARENT_DIR);
			devsData.add(fsi);
			mCurInfo = fsi;
			
			File []files = f.listFiles();
			if(files == null)
				return null;
			for(File file:files){
				FileInfo fi;

				String name = file.getName();
				String path = file.getAbsolutePath();
				FileType type;
				
				if(name.equalsIgnoreCase("lost.dir"))
					continue;
				
				if(file.isDirectory()){
					type = FileType.DIR;
				}
				else{
					int pos = name.lastIndexOf(".");
					if(pos != -1 && pos<(name.length()-1)){
						String ext = name.substring(pos+1).toLowerCase();
						type = getFileType(ext);
					}
					else{
						type = FileType.FILE;
					}
				}
				fi = new FileInfo(name,	path, type);
				devsData.add(fi);
			}
			
			isHome = false;

			
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			fileListAdapter.notifyDataSetChanged();
			mTvCurPath.setText(mCurPath);
			dialog.cancel();
		}
	}
 
 

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fp_devs_mypc:
			new SearchTask().execute("home");
			break;
		case R.id.fp_exit:
			long l = System.currentTimeMillis();
			if (l - backKeyTime > 2000L) {
				Toast.makeText(getApplicationContext(), "再按一次退出", 0).show();
				backKeyTime = l;
				break;
			}
			
			exit();
			break;
		}
	}

	
	void exit(){
		if(broadcastRecv != null){
			unregisterReceiver(broadcastRecv);
		}
		
		System.exit(0);
	}
}
