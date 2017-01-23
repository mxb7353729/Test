package com.evialab.gisserver;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class GCServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_GC")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}
	 
		
		  
		String st = RequestUtil.getParameter(req,"st");
		String city = RequestUtil.getParameter(req,"city");
		String address =  RequestUtil.getParameter(req,"address");		
		String output = RequestUtil.getParameter(req,"output");
		String callback = RequestUtil.getParameter(req,"callback");
		//String encoding = Util.getParameter(req,"encoding");
		 
		if(!"Geocoding".equalsIgnoreCase(st))
		{
			ResponseUtil.error(resp, " 参数错误");
			return;
		}
		
		
		//�?始切�?
		long begintime = (new Date()).getTime();
		
		GCCach.CutResult result = server.admin.gcCach.addressCut(city, address);
	 
		if(result == null){
			ResponseUtil.error(resp, "切词错误");
			return;
		}
		
		//精确�?
		JSONObject addressinfo = new JSONObject();	
		int accuracy = 0;
		String addresstype = "";
		if(result.roadAndDoor!=null)
		{
			accuracy = 100;
			addresstype = "Point";
		}
		else if(result.road!=null)
		{
			accuracy = 90;
			addresstype = "Line";
		}
		else if(result.block!=null)
		{
			accuracy = 80;
			addresstype = "Block";
		}
		else if(result.district!=null)
		{
			accuracy = 60;
			addresstype = "District";
		}
		else if(result.city!=null)
		{
			accuracy = 50;
			addresstype = "City";
		}
		addressinfo.put("accuracy",accuracy);
		addressinfo.put("addresstype",addresstype);
		
		//切词信息
		String  cutwordinfo = "";
		for(int i = 0; i  < result.cutwords.size(); i++){
			
			GCCach.CutUnit unit = result.cutwords.get(i);
			cutwordinfo += unit.words + "^" + unit.level + " ";
		}
		
		if(!result.address.isEmpty())
		{
			cutwordinfo += result.address + "^0";
		}
 
		//返回�?
		JSONObject  jresult = new JSONObject();
		jresult.put("addressinfo", addressinfo);
		jresult.put("cutwordinfo",cutwordinfo);
		
		//计算bound
		//填充结果
		boolean xml = "xml".equalsIgnoreCase(output) || output == null;
		
		Object poiresult = null;
		
		if(xml){
			poiresult = new JSONObject();			
		}
		else {
			poiresult = new JSONArray();
		}
		
		double west = 0,east=0,south=0,north=0;
		int    poinumber = Math.min(10, result.points.size());
		for(int i = 0; i < result.points.size(); i++)
		{
			JSONObject point = result.points.get(i);
			point.put("id", i);
			double lng = Double.parseDouble(point.getString("lng"));
			double lat = Double.parseDouble(point.getString("lat"));
			if(i == 0)
			{
				west = east = lng;
				south = north = lat;
			}
			else{
				west = Math.min(west,lng);
				east = Math.max(east,lng);
				south = Math.min(south,lat);
				north = Math.max(north,lat);
			}
			
			if(poiresult instanceof JSONObject)
				((JSONObject)poiresult).append("point", point);
			else if(poiresult instanceof JSONArray)
				((JSONArray)poiresult).put( point);
		}			
		jresult.put("points", poiresult);
		jresult.put("total", poinumber);
		
		String bound = String.format("%.6f, %.6f;%.6f, %.6f", west, south,east,north);
		jresult.put("bound", bound);
	
	 	
		//耗时 time
		long endtime = (new Date()).getTime();
		jresult.put("time", new DecimalFormat("0.00000").format((endtime - begintime) * 0.001));
		
		//边界 bound
		
		 
		ResponseUtil.writeJson2Response(resp,output,"utf-8",callback,jresult);
		
	}
 
	
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(req,response);
	}
}
