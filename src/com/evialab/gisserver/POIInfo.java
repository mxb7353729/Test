package com.evialab.gisserver;

public class POIInfo {
	public String name;
	public String address;
	public double x;
	public double y;
	public double dist;
	public double angle;	//给定坐标相对于POI的方�?
	public String number;
	
	public double x0;
	public double y0;
	
	double R = 6378137;
	public String dir;
	POIInfo()
	{
		name = "";
		address = "";
	}
	public void CreateDistAngle()
	{
		double ELat = R * Math.PI / 180.0;
		double c = Math.cos(y0 * Math.PI / 180);
		dist = Math.sqrt((x0 - x) * (x0 - x) * c * c + (y0 - y) * (y0 - y)) * ELat;
		double s = (y0 - y) * ELat;
		angle = Math.asin(s / dist) * 180 / Math.PI;
		
		if(x0 < x)
		{
			angle = 180 - angle;
		}
		if(angle < 0)
		{
			angle += 360;
		}
		dir = "�?";
		if((angle > 20) & (angle < 70))
		{
			dir = "东北";
		}
		else if((angle >= 70) && (angle <= 110))
		{
			dir = "�?";
		}
		else if((angle > 110) && (angle < 160) )
		{
			dir = "西北";
		}
		else if((angle >= 160) && (angle <= 200))
		{
			dir = "�?";
		}
		else if((angle > 200) && (angle < 250))
		{
			dir = "西南";
		}
		else if((angle >=250) && (angle <=290))
		{
			dir = "�?";
		}
		else if((angle > 290) && (angle < 340))
		{
			dir = "东南";
		}
		
	}
	
}
