package com.jetmap.route;

public class RouteApi2 {
	public RouteApi2()
    {
    }

    public static native int CheckKey(String s);

    public static native int Init(String s, int i);

    public static native String Plan(int i, int j, int k, int l, int i1, int ai[], int j1);

    public static void loadLib()
    {
    	 System.loadLibrary("mroute");
    }
}
