package com.evialab.the3rd;

public class TrafficTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		LonlatBox box = getBox("325232");
		
		System.out.println(box);
	}
	
	public static class Lonlat
	{
		public double lon;
		public double lat;
	}
	 
	public static  class LonlatBox
	{
		public double west;
		public double east;
		public double south;
		public double north;
		public String toString()
		{
			return west + "\t" + east + "\t" + south + "\t" + north;
		}
	}

	public static LonlatBox getBox(String meshid)
	{
		LonlatBox box = new LonlatBox();
		Lonlat ws = getLonLat(meshid);
		
		int mid = Integer.parseInt(meshid);
		mid = mid  + 11;
		
		Lonlat en = getLonLat(mid + "");
		
		box.west = ws.lon;
		box.east = en.lon;
		box.south = ws.lat;
		box.north = en.lat;
		
		return box;
	}
	public static Lonlat getLonLat(String meshid)
	{
		try
		{
			Lonlat p = new Lonlat();
			
			String a = meshid.substring(0, 2);
			String b = meshid.substring(2, 4);
			String c = meshid.substring(4, 5);
			String d = meshid.substring(5);
			
			
			p.lon = Double.parseDouble(b) + 60.0  + Double.parseDouble(d) / 8.0;
			
			p.lat = (Double.parseDouble(a)  + Double.parseDouble(c) / 8.0 ) * 2 / 3.0;
			
			
			return p;
		}
		catch(Exception e)
		{
			return null;
		}
	
	}
	
	//计算某个点所在的mapid
	public static String getMapID(double lon, double lat){
		
		
		return "";
	}
}
