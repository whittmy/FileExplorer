package fileexplorer.lemoon;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class FileInfo {
	public String mTitle;
	public FileType mType;	 
	public String mPath;
	
 
	public FileInfo(String title,  String path, FileType type){
		mTitle =  title;
		mPath = path;
		mType = type;
	}
	
	static public enum FileType{FILE, VIDEO, MUSIC, ZIP, DIR, PHOTO, PARENT_DIR};
}
