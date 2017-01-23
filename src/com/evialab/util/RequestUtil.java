package com.evialab.util;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {
	public static String GetStringFromServlet(String value) throws Exception
	{
		if(value == null)
			return "";
		String result = new String(value.getBytes("ISO-8859-1")).trim();
		result = URLDecoder.decode(result, "UTF-8").trim();
		return result;
	}
	public static boolean isNull(String value)
	{
		if(value == null)
			return true;
		
		value = value.trim();
		if(value.isEmpty())
			return true;
		return false;
	}
	public static String getParameter(HttpServletRequest req, String pname)
	{
		String res = req.getParameter(pname);
		if(res == null)
			return null;
		return res.trim();
	}
	public static int getInt(HttpServletRequest req, String pname,int defaultv)
	{
		try {
			 return Integer.parseInt(RequestUtil.getParameter(req, pname));
		} catch (Exception e) {
			return defaultv;
		}
	}
	public static String getBasePath(HttpServletRequest req)
	{
		String path = req.getContextPath();  
		String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+path;
		return basePath;
	}
	//获取Ip
	public static String getIp(HttpServletRequest req)
	{
	     String Ip = null;
	     Ip = req.getHeader("x-forwarded-for");
	     //System.out.println("通过x-forwarded-for获取�?" +Ip );
	     if(Ip == null || Ip.length() == 0 || "unknown".equalsIgnoreCase(Ip)) 
	     {
	    	 Ip = req.getHeader("Proxy-Client-IP");
	    	 //System.out.println("通过Proxy-Client-IP获取�?" +Ip );
	     }
	     if(Ip == null || Ip.length() == 0 || "unknown".equalsIgnoreCase(Ip)) 
	     {
	    	 Ip = req.getHeader("WL-Proxy-Client-IP");
	    	 //System.out.println("通过WL-Proxy-Client-IP获取�?" +Ip );
	     }
	     if(Ip == null || Ip.length() == 0 || "unknown".equalsIgnoreCase(Ip)) 
	     {
	    	 Ip = req.getRemoteAddr();
		     if(Ip.equals("127.0.0.1"))
		     {
		    	try 
		    	{
			       //根据网卡取本机配置的IP
			       InetAddress inet = InetAddress.getLocalHost();
			       Ip= inet.getHostAddress();
				} 
		    	catch (UnknownHostException e) 
				{
					e.printStackTrace();
				}
			 }
	     }
	     //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
	     if(Ip != null && Ip.length()>15){ //"***.***.***.***".length() = 15
/*	         if(Ip.indexOf(",")>0){
	        	 Ip = Ip.substring(0,Ip.indexOf(","));
	         }*/
	     }
	     return Ip; 
	}

	//获取登录页面
	public static String getLandingPage(HttpServletRequest req)
	{
		return req.getContextPath() + "/admin.html";
	}
	//获取首页
	public static String getHomePage()
	{
		return "";
	}
	public static int getMaxFailureTime()
	{
		return 10*60;//失效时间�?10分钟,10 * 60
	}
	public static boolean valid(String value)
	{
		if(value != null && !value.isEmpty())
			return true;
		return false;
	}
}
