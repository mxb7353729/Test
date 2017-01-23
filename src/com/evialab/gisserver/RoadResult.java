package com.evialab.gisserver;


public class RoadResult {
	public double dis;
	public double x;
	public double y;
	public String kind;
	public String limit;
	public String isCity;
	public String width;
	public double limit0;
	public double limit1;
	public String name;
	public int    lanenumber;
	public String subtype;
	public double angle;
	public int direction;
	public String linkid;
	RoadResult()
	{
		kind = "未发现道�?";
		limit = "-1";
		isCity = "0";
		name = "未发现道�?";
		width = "宽度不明";
		lanenumber = 1;
		subtype = "";
		angle = 0;
		limit0 = 0;
		limit1 = 0;
		direction = 0;
		linkid = "";
	}
}