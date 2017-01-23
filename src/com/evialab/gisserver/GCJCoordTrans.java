/**
 * 
 */
package com.evialab.gisserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
 



import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper; 
import org.dom4j.Element; 
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter; 
import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;
 

 

/**
 * @author Administrator
 * 实现GCJ和经纬度之间的转�?
 */
public class GCJCoordTrans extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
 
	    doGet(req,resp);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setHeader("Access-Control-Allow-Origin", "*");
		long begintime = (new Date()).getTime();
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(request, "SE_SH")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}
 
		String st = RequestUtil.getParameter(request,"st");
		
		
		boolean comp11 = false;
		String points = RequestUtil.getParameter(request,"points");
		
		if(RequestUtil.isNull(points))
		{
			points = RequestUtil.getParameter(request,"pt");
			comp11 = true;
		}
		String show = RequestUtil.getParameter(request,"show");
		String output = RequestUtil.getParameter(request,"output");
		String callback = RequestUtil.getParameter(request,"callback");
		String tid  = RequestUtil.getParameter(request,"tid");
		
		boolean showorg = false;
		if(show!=null)
			showorg = show.equals("1");
		
	 	
		//原始坐标�?
		List<BLPoint> orgpoints = new ArrayList<BLPoint>();
		//转换后坐标点
		List<BLPoint> dstpoints = new ArrayList<BLPoint>();
		
		if( points!=null)
		{
			String[] pps = points.split(";");
			for(String p:pps)
			{
				String[] t = p.split(",");
				BLPoint bl = new BLPoint();
				bl.lng = Double.parseDouble(t[0]);
				bl.lat = Double.parseDouble(t[1]);
				orgpoints.add(bl);
			}
			
			
			for(BLPoint p: orgpoints)
			{
				BLPoint dp = new BLPoint();
				if("r".equals(st))
					r_transform(p,dp);
				else
					transform(p,dp);
				dstpoints.add(dp);
			}
		}
		
		if(comp11)
		{
			comp11(orgpoints,dstpoints, resp,showorg);
			return;
		}
		
		boolean xmlorjson = true;
		if(output!=null)
			xmlorjson = !output.equals("json");
		
		//1,构�?�json对象
		
		JSONObject jresult = new JSONObject();
		
		
	 
		//如果是xml
		if(xmlorjson){
			
			JSONObject jpoints = new   JSONObject();
			for(int i =0; i < dstpoints.size(); i++)
			{		
				JSONObject p = new JSONObject();
				p.put("lng", String.format("%.6f",dstpoints.get(i).lng));
				p.put("lat", String.format("%.6f",dstpoints.get(i).lat));
				jpoints.append("point",p);
				if(showorg)
				{
					JSONObject sp = new JSONObject();
 
					sp.put("original_lng", String.format("%.6f",orgpoints.get(i).lng));
					sp.put("original_lat", String.format("%.6f",orgpoints.get(i).lat));
					jpoints.append("point",sp);
				}
			}	
			jresult.put("points", jpoints);
		}
		else{
			JSONArray jpoints = new   JSONArray();
			for(int i =0; i < dstpoints.size(); i++)
			{		
				JSONObject p = new JSONObject();
				p.put("lng", String.format("%.6f",dstpoints.get(i).lng));
				p.put("lat", String.format("%.6f",dstpoints.get(i).lat));
				
				if(showorg)
				{
	 				p.put("original_lng", String.format("%.6f",orgpoints.get(i).lng));
					p.put("original_lat", String.format("%.6f",orgpoints.get(i).lat));
					 
				}
				jpoints.put(p);
			}	
			jresult.put("points", jpoints);
		}

		 
		if(!RequestUtil.isNull(tid)){
			jresult.put("tid",tid);
		}
		long endtime = (new Date()).getTime();
		jresult.put("time", endtime - begintime);
 	
		ResponseUtil.writeJson2Response(resp,output,"utf-8",callback,jresult);
		
		
	}
	
	
	private void comp11( List<BLPoint> orgpoints , List<BLPoint> dstpoints , HttpServletResponse resp, boolean showorg)
	{
	   
		 Document document = DocumentHelper.createDocument();  //创建文档   
	     Element result=document.addElement("Result");   
	     result.addAttribute("time", "0");
	     result.addAttribute("error", "0");
         
        for(int i =0; i < dstpoints.size(); i++)
		{
        	 Element Geo=result.addElement("Geo");  
        	 
        	 Geo.addAttribute("lo",   String.format("%.6f",dstpoints.get(i).lng));
        	 Geo.addAttribute("la",  String.format("%.6f",dstpoints.get(i).lat));
        	 if(showorg)
        	 {
        		 Geo.addAttribute("original_lo",   String.format("%.6f",orgpoints.get(i).lng));
            	 Geo.addAttribute("original_la",  String.format("%.6f",orgpoints.get(i).lat));
        	 }
		}
        resp.setContentType("text/xml");
        	         
			try {
				 
				 XMLWriter xmlWriter  = new XMLWriter(resp.getOutputStream(), OutputFormat.createPrettyPrint());
				 xmlWriter.write(document);
	             xmlWriter.close();   
			} catch (UnsupportedEncodingException e) {
				 
				e.printStackTrace();
			} catch (IOException e) {
			 
				e.printStackTrace();
			}    
            
              
	}
	 //单个点转换坐�?
		public static String tranpoint(String pt){
	    	double x = Double.parseDouble(pt.substring(0, pt.indexOf(',')));
			double y = Double.parseDouble(pt.substring(pt.indexOf(',') + 1));
			BLPoint bl =new BLPoint();
			BLPoint rebl =new BLPoint();
			bl.lng=x;
			bl.lat=y;
			transform(bl,rebl);
			String result=rebl.lng+","+rebl.lat;
			return result;

	    }
		//多个点转化坐�?
		public static String tranpbatch(String pt){
			//原始坐标�?
			List<BLPoint> orgpoints = new ArrayList<BLPoint>();
			String result="";
			if(pt!=null)
			{
				String[] pps = pt.split(";");
				for(String p:pps)
				{
					String[] t = p.split(",");
					BLPoint bl = new BLPoint();
					bl.lng = Double.parseDouble(t[0]);
					bl.lat = Double.parseDouble(t[1]);
					orgpoints.add(bl);
				}
				
				
				for(BLPoint p:orgpoints)
				{
					BLPoint dp = new BLPoint();
					transform(p,dp);
					String tmp=dp.lng+","+dp.lat+";";
					result+=tmp;
				}
			}
			result.substring(0,result.length()-1);
			return result;

	    }
    //
    // Krasovsky 1940
    //
    // a = 6378245.0, 1/f = 298.3
    // b = a * (1 - f)
    // ee = (a^2 - b^2) / a^2;
	final static double a = 6378245.0;
	final static double ee = 0.00669342162296594323;

    //
    // World Geodetic System ==> Mars Geodetic System
    public static void transform(BLPoint org, BLPoint dst)
    {
    	double wgLat = org.lat;
    	double wgLon = org.lng;
    	
    	double mgLat = org.lat;
    	double mgLon = org.lng;
    	
        if (outOfChina(wgLat, wgLon))
        {
            return;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        mgLat = wgLat + dLat;
        mgLon = wgLon + dLon;
        
        dst.lat = mgLat;
        dst.lng = mgLon;
    }
    
    
    // Mars Geodetic System ==>  World Geodetic System  近似计算
    public static void r_transform(BLPoint org, BLPoint dst)
    {
    	double wgLat = org.lat;
    	double wgLon = org.lng;
 
        if (outOfChina(wgLat, wgLon))
        {
            return;
        }
        transform(org,dst);
        
        double bx = dst.lng - org.lng;
        double by = dst.lat - org.lat;
        
       dst.lng = org.lng - bx;
       dst.lat = org.lat - by;
       return;
    }
    
    

    static boolean outOfChina(double lat, double lon)
    {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    static double transformLat(double x, double y)
    {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    static double transformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

 
	
}


