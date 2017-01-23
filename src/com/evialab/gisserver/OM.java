package com.evialab.gisserver;

import java.sql.Statement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class OM extends TimerTask{ 
	
	protected Statement mCurrentStatement = null;
	Connection m_conn = null;
	private String mCurrentHour = "";
	private Map<String,Integer> mRecords;
	private Timer mTimer = null;
	
	
	@Override
	public void run() {
		saveRecord();
	}
	
	
	public OM(Connection conn)
	{
		m_conn = conn;
		mRecords = new HashMap<String,Integer>();
		mTimer = new Timer();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
		formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		Date d = new Date();
		mCurrentHour = formatter.format(d);
		long delay = 3600000 - d.getTime()%3600000;
		
		mTimer.schedule(this, delay, 3600000);
	}

	private void saveRecord()
	{
		synchronized(mRecords)
		{
			for (String k : mRecords.keySet()) 
			{
				String[] str= k.split(":");
				ExcuteQuery2("insert into t_om values(\'" + str[0] + "\',\'" + str[1] + "\',\'" + mCurrentHour + "\',\'" + str[2] + "\',\'" + mRecords.get(k) +"\')");
			}
			mRecords.clear();
			Date d = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH");
			formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
			mCurrentHour = formatter.format(d);
		}
	}
	public ResultSet ExcuteQuery(String sql) {
		ResultSet set = null;
		try 
		{
			CloseCurrentStatement();
			mCurrentStatement = m_conn.createStatement();
			set = mCurrentStatement.executeQuery(sql);
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}  
		return set;
	}

	public void ExcuteQuery2(String sql) {
		try 
		{
			CloseCurrentStatement();
			mCurrentStatement = m_conn.createStatement();
			mCurrentStatement.execute(sql);
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}  
	}
	
	public void CloseCurrentStatement()
	{
		try {
			if(mCurrentStatement != null)
				mCurrentStatement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCurrentStatement = null;
	}
	
	public void record(String id,String ip, String st)
	{
		try
		{
			if(id == null)
				id = "";
			String key = id+":"+ip+":"+st;

			synchronized(mRecords)
			{
				if(!mRecords.containsKey(key))
				{
					mRecords.put(key, 1);
				}
				else
				{
					mRecords.put(key, mRecords.get(key) + 1);
				}
			}
		}
		catch(Exception e)
		{}
	}
	public void serverClose(){
		if(mTimer!= null)
			mTimer.cancel();
		saveRecord();
	}

	
}
 