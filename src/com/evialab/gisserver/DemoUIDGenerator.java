package com.evialab.gisserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.evialab.util.MD5Util;

public class DemoUIDGenerator extends TimerTask{

	
	Timer timer = null;
	String demoUID = null;
	public DemoUIDGenerator()
	{
		timer = new Timer();
		//与明天凌�?12点的时间间隔
 	 
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DATE, 1);
		tomorrow.set(Calendar.HOUR_OF_DAY,0);
		tomorrow.set(Calendar.MINUTE, 0);
		tomorrow.set(Calendar.SECOND, 0);
		tomorrow.set(Calendar.MILLISECOND, 0);
	 
		long delay =  tomorrow.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
		
		
		timer.schedule(this, delay, 24 * 60 * 60 * 1000);
		
		run();
	}
	public void run() {
		 demoUID = generateDemoUID();
		 System.out.println("new demoUID:" + demoUID);
	}
	
	public String getTodayDemoUID()
	{
		return demoUID;
	}
	String generateDemoUID()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("MMdd");
		 
		String todaymd5 = formatter.format(new Date());
		todaymd5 = MD5Util.MD5(todaymd5);
		return "cttic"+todaymd5;
	}
	public void serverClose(){
		if(timer!= null)
			timer.cancel();
	}
}
