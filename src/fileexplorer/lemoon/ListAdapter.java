package fileexplorer.lemoon;


//ok

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
 
import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.LinkedList;

import fileexplorer.lemoon.FileInfo.FileType;

 
public class ListAdapter extends BaseAdapter {
	protected static final String TAG = "LanShareListAdapter";
	private Context context;
	private LinkedList<FileInfo> data;
	int[] img = {R.drawable.icon_folder, 
			R.drawable.icon_file,
			R.drawable.icon_video, 
			R.drawable.icon_music, 
			R.drawable.icon_picture,
			R.drawable.icon_return};
	
	public ListAdapter(Context cx, LinkedList<FileInfo> data) {
		this.context = cx;
		this.data = data;
	}

	public int getCount() {
		return this.data.size();
	}
 

	public Object getItem(int idx) {
		return this.data.get(idx);
	}

	public long getItemId(int idx) {
		return idx;
	}

	public View getView(final int idx, View v, ViewGroup vGroup) {
		if (v == null) {
			v = View.inflate(this.context, R.layout.act_vr_fp_item, null);
			Holder holder2 = new Holder();
			holder2.title = ((TextView) v.findViewById(R.id.act_vr_fp_item_title));
			holder2.mainIv = ((ImageView) v.findViewById(R.id.act_vr_fp_item_icon));
 
			v.setTag(holder2);
		}
		
		Holder holder1 = (Holder) v.getTag();
		FileInfo info = data.get(idx);
		
		holder1.title.setTextColor(Color.rgb(0x4d, 0x4f, 0x50)); //

		if(info.mType==FileType.DIR){
			holder1.mainIv.setImageResource(img[0]);
		}
		else if(info.mType == FileType.MUSIC){
			holder1.mainIv.setImageResource(img[3]);
		}
		else if(info.mType == FileType.PHOTO){
			holder1.mainIv.setImageResource(img[4]);
		}
		else if(info.mType == FileType.VIDEO){
			holder1.mainIv.setImageResource(img[2]);
		}
		else if(info.mType==FileType.PARENT_DIR){
			holder1.mainIv.setImageResource(img[5]);
			holder1.title.setTextColor(Color.rgb(245, 124, 45));
		}
		else{
			//file
			holder1.mainIv.setImageResource(img[1]);
		}
 		holder1.title.setText(info.mTitle);
		return v;
	}

	class Holder {
		public ImageView mainIv;
		public TextView title;		
		
		Holder() {
		}
	}
}
