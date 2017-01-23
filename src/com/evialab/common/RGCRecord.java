package com.evialab.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RGCRecord extends TimerTask {

	private String mCurrentHour = "";
	// ArrayList<String> List = new ArrayList<String>();
	String key = "";
	FileOutputStream out;
	private String rgcpath;
	private Timer mTimer = null;

	public RGCRecord(String path, boolean open) {
		if (open) {
			mTimer = new Timer();
			mTimer.schedule(this, 60 * 1000, 60 * 1000);
		}

		rgcpath = path;
	}

	@Override
	public void run() {
		saveRecord();
	}

	public void saveRecord() {
		// 将记录写入文�?
		String tmpath = "";
		Date d = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String day = formatter.format(d);
		tmpath = rgcpath + "rgc_" + day + ".txt";
		File file = new File(tmpath);
		try {
			out = new FileOutputStream(file, true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// synchronized(List)
		// {
		// try{
		//
		// for(String o:List){
		// StringBuffer sb=new StringBuffer();
		// sb.append(o);
		// out.write(sb.toString().getBytes("utf-8"));
		// }
		// out.close();
		// }catch(IOException ex){
		// ex.printStackTrace();
		// }
		// // List.removeAll(List);
		// List.clear();
		synchronized (key) {
			try {
				out.write(key.getBytes("utf-8"));
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			key = "";
		}

	}

	public void record(String uid, String ip, String st, String point) {
		if (mTimer == null)
			return;
		Date d = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		mCurrentHour = formatter.format(d);
		String temp = uid + "\t" + ip + "\t" + st + "\t" + point + "\t" + mCurrentHour + "\r\n";

		// synchronized(List)
		// {
		// List.add(key);
		// }
		synchronized (key) {
			key += temp;
		}

	}

	public void serverClose() {
		if (mTimer != null) {
			mTimer.cancel();
			saveRecord();
		}
	}
}
