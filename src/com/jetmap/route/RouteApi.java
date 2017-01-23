package com.jetmap.route;

public class RouteApi {
	public RouteApi() {
	}

	public static native int CheckKey(String s);

	public static native int Init(String s, int i);

	public static native String Plan(int i, int j, int k, int l, int i1,
			int ai[], int j1);

	public static void loadLib(String jnipath) {
		jnipath = jnipath.trim();
		System.out.println("load mroute jni  from :" + jnipath);

		//System.load(jnipath + java.io.File.separator+"libmroute.so");//Linux系统下加载这�?
		System.load(jnipath + java.io.File.separator+"mroute.dll");//Windows系统下加载这�?
		System.out.println("load mroute jni success");
	}
	
	public static boolean nativeInited = false;
	
	//当本地初始化失败时�?�，用代理服务器访问
	public static String proxyserver = "";
	
	public static void initRouteEngine(String jnipath, String key, String datapath)
	{
		try{
			//加载jni
			RouteApi.loadLib(jnipath);
			//�?测key
			int flag = RouteApi.CheckKey(key.trim());
			if(flag != 1)
			{
				System.out.println("init route engine failed: checkkey failed");
			}
			else
			{
				//初始化数�?
				flag = RouteApi.Init(datapath.trim(), 0);
				if(flag != 1)
				{
					System.out.println("init route engine failed: init datapath failed");
				}
				else
				{
					nativeInited = true;
					System.out.println("init route engine success");
				}
			} 
		}
		catch(Exception ex)
		{
			System.out.println("init route engine failed " + ex.getMessage());
		}
		 
	}
}
