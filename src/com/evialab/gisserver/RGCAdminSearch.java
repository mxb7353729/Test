package com.evialab.gisserver;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import org.json.JSONObject;

import org.dom4j.*;

public class RGCAdminSearch {

	static String connStr = "";
	static Connection conn = null;
	static PreparedStatement  ps = null;
	static long level = 11;

	public static class AdminInfo
	{
		public String code;
		public String name;
		public String city;
		public String prov;
		
	}
	public static void Init(String path)
	{
		try{
			//Class.forName("org.sqlite.JDBC");

			Class.forName("org.sqlite.JDBC");
			connStr = "jdbc:sqlite:"  + path;
			//String connStr = "jdbc:sqlite:D:\\roadindex.db3";
			conn = DriverManager.getConnection(connStr);
			 ps = conn.prepareStatement("select X,Y,Shape from adminIndex where X=?  and Y=?");
		}
		catch(Exception ex)
		{
			System.out.println("RGCAdminSearch Init Exception:" + ex.getMessage());
		}
		
	}
	public static boolean ptInPolygon(double testx, double testy, int nvert, double[]vertx, double[] verty)
	{
		  int i, j;
		  boolean c = false;
		  
		  for (i = 0, j = nvert-1; i < nvert; j = i++) {
		    if ( ((verty[i]>testy) != (verty[j]>testy)) &&
		     (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
		       c = !c;
		  }
		  return c;
	}
	synchronized public static String getRGCAdmin2011(double x, double y)
	{
		try
		{
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("Result");
			root.addAttribute("time", "0");
			root.addAttribute("error", "0");
			Element info = DocumentHelper.createElement("AInfoB");
			Element dis = info.addElement("Dis");
			 
			Element pro = info.addElement("Pro");
			 
			
			doc.setXMLEncoding("UTF-8");
			
			//System.out.println(doc.asXML());
			
			//JSONObject jo = new JSONObject();
			double blkSize = 90.0 / Math.pow(2, level); 
			int xid = (int)(x / blkSize);
			int yid = (int)(y / blkSize);

		
			ps.setInt(1, xid);
			ps.setInt(2, yid);
			//long startMili=System.currentTimeMillis();
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				String shape = rs.getString(3);
				if(shape.indexOf('>') < 0)
				{
					String strFields = shape.substring(0, shape.indexOf(":"));
					String[] fields = strFields.split(";");
					String id = fields[0];
					String name = fields[1];
					String city = fields[2];
					String prov = fields[3];
					dis.addAttribute("v", id + "000");
					pro.addAttribute("v",  prov + ">" + city + ">" + name);
					//jo.put("district", id + "000");
					//jo.put("district_text", name + ">" + city + ">" + prov);
					//System.out.println(name + ";" +  city + ";" + prov);
				}
				else
				{
					AdminInfo ainfo = getAdmin(shape, x, y);
					dis.addAttribute("v", ainfo.code + "000");
					pro.addAttribute("v",  ainfo.prov + ">" + ainfo.city + ">" + ainfo.name);
					
					//jo.put("district", ainfo.code + "000");
					//jo.put("district_text", ainfo.name + ">" + info.city + ">" + info.prov);
	
					//System.out.println(info.name + ";" +  info.city + ";" + info.prov);
				}
				root.add(info);
			}
			
			rs.close();
			return doc.asXML();
			//long endMili=System.currentTimeMillis();
			//System.out.println("Over " + (endMili - startMili));	
		}
		catch(Exception ex)
		{
			
			System.out.println(ex.getMessage());
		}		
		return null;
	}	
	 synchronized  public static JSONObject getRGCAdmin(double x, double y)
	{
		try
		{
			JSONObject jo = new JSONObject();
			double blkSize = 90.0 / Math.pow(2, level); 
			int xid = (int)(x / blkSize);
			int yid = (int)(y / blkSize);

		
			ps.setInt(1, xid);
			ps.setInt(2, yid);
			//long startMili=System.currentTimeMillis();
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				String shape = rs.getString(3);
				if(shape.indexOf('>') < 0)
				{
					String strFields = shape.substring(0, shape.indexOf(":"));
					String[] fields = strFields.split(";");
					String id = fields[0];
					String name = fields[1];
					String city = fields[2];
					String prov = fields[3];
					jo.put("district", id + "000");
					jo.put("district_text", prov + ">" + city + ">" + name);
					//System.out.println(name + ";" +  city + ";" + prov);
				}
				else
				{
					AdminInfo info = getAdmin(shape, x, y);
					jo.put("district", info.code + "000");
					jo.put("district_text", info.prov + ">" + info.city + ">" + info.name);
	
					//System.out.println(info.name + ";" +  info.city + ";" + info.prov);
				}
			}
			
			rs.close();
			return jo;
			//long endMili=System.currentTimeMillis();
			//System.out.println("Over " + (endMili - startMili));	
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}		
		return null;
	}
	 
	 
	   synchronized  public static AdminInfo getDistrictByPos(double x, double y)
		{
			try
			{
				AdminInfo info = new AdminInfo();
				double blkSize = 90.0 / Math.pow(2, level); 
				int xid = (int)(x / blkSize);
				int yid = (int)(y / blkSize);

			
				ps.setInt(1, xid);
				ps.setInt(2, yid);
				//long startMili=System.currentTimeMillis();
				ResultSet rs = ps.executeQuery();
				if(rs.next())
				{
					String shape = rs.getString(3);
					if(shape.indexOf('>') < 0)
					{
						String strFields = shape.substring(0, shape.indexOf(":"));
						String[] fields = strFields.split(";");
						info.code = fields[0];
						info.name = fields[1];
						info.city = fields[2];
						info.prov = fields[3];
					}
					else
					{
						 info = getAdmin(shape, x, y);
					}
				}
				
				rs.close();
				return info;
			}
			catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}		
			return null;
		}
	 
	 
	public static void main(String[]args){
		//RGCAdminSearch.getRGCAdmin2011(0, 1);
		RGCAdminSearch.Init("D:/roadindex.db3");
		for(int i = 0; i< 10000; i++)
		{
			JSONObject jo = RGCAdminSearch.getRGCAdmin(114+i*0.0001, 33+i*0.0001);
		}
		System.out.println("Over");
		//System.out.println(jo.toString());
	}
	
	public static void test(String[] args) {
		// TODO Auto-generated method stub
		try
		{
			RGCAdminSearch.Init("");
			long level = 11;
			double blkSize = 90.0 / Math.pow(2, level); 
			double x = 115.444;
			double y = 38.686;
			int xid = (int)(x / blkSize);
			int yid = (int)(y / blkSize);

			
			Class.forName("org.sqlite.JDBC");
			String connStr = "jdbc:sqlite:D:\\四维\\RGC\\roadindex.db3";
			Connection conn = DriverManager.getConnection(connStr);
			PreparedStatement  ps = conn.prepareStatement("select X,Y,Shape from adminIndex where X=?  and Y=?");
			ps.setInt(1, xid);
			ps.setInt(2, yid);
			long startMili=System.currentTimeMillis();
			for(int testi=0; testi < 1000; testi++)
			{
			ResultSet rs = ps.executeQuery();
			if(rs.next())
			{
				String shape = rs.getString(3);
				if(shape.indexOf('>') < 0)
				{
					String strFields = shape.substring(0, shape.indexOf(":"));
					String[] fields = strFields.split(";");
					String id = fields[0];
					String name = fields[1];
					String city = fields[2];
					String prov = fields[3];
					System.out.println(name + ";" +  city + ";" + prov);
				}
				else
				{
					AdminInfo info = getAdmin(shape, x, y);
					System.out.println(info.name + ";" +  info.city + ";" + info.prov);
				}
			}
			rs.close();
			}
			long endMili=System.currentTimeMillis();
			System.out.println("Over " + (endMili - startMili));	
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}

	}
	
	public static AdminInfo getAdmin(String shape, double x, double y)
	{
		String[]featsString = shape.split(">");
		AdminInfo info = new AdminInfo();
		boolean isOK = false;
		for(int i = 0; i < featsString.length; i++)
		{
			isOK = false;
			String strFeat = featsString[i];
			String strFields = strFeat.substring(0, strFeat.indexOf(":"));
			String[] fields = strFields.split(";");
			info.code = fields[0];
			info.name = fields[1];
			info.city = fields[2];
			info.prov = fields[3];
			
			String strShapes = strFeat.substring(strFeat.indexOf(":") + 1);
			String[]partsString = strShapes.split("[+]");
			for(int j = 0; j < partsString.length; j++)
			{
				String[] ptsString = partsString[j].split(";");
				double[] xs = new double[ptsString.length];
				double[] ys = new double[ptsString.length];
				for(int m = 0; m < ptsString.length; m++)
				{
					String[] temp = ptsString[m].split(",");
					xs[m] = Double.parseDouble(temp[0]);
					ys[m] = Double.parseDouble(temp[1]);
				}
				
				if( ptInPolygon(x, y, ptsString.length, xs, ys))
				{
					isOK = true;
					break;
				}
				
			}
			if(isOK)
				break;
		}
		return info;
	}

}
