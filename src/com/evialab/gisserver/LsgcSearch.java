package com.evialab.gisserver;
import java.io.IOException;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

public class LsgcSearch extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			resp.sendError(400, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_LSGC")) {
			resp.sendError(400, "无效UID");
			return;
		}
		JSONObject lsresult = new JSONObject();
		JSONObject jresult = new JSONObject();
		String id = RequestUtil.getParameter(req,"uid");
		if(id==null||id=="")
		{
			lsresult.put("status", "error");
			lsresult.put("error", "缺少uid参数");
			ResponseUtil.writeJson2Response(resp, "xml", "utf-8", null,
					lsresult);
			resp.setHeader("ResponseStatus", "ERROR");
			return ;
		}
		String output = RequestUtil.getParameter(req,"output");
		boolean xmlorjson = true;
		if(output!=null)
			xmlorjson = !output.equals("json");
		
		try{
			String st = RequestUtil.getParameter(req,"st");
			String city = RequestUtil.getParameter(req,"city");
			String words =  RequestUtil.getParameter(req,"words");
			String callback = RequestUtil.getParameter(req,"callback");
			
			if(st.equalsIgnoreCase("LsgcSearch")){
				LocalSearch ls=new LocalSearch();
				lsresult = ls.doGetLs(req,resp,xmlorjson);	
			//	GCServlet gc=new GCServlet();
				//�?始切�?
				long begintime = (new Date()).getTime();
				GCCach.CutResult res = server.admin.gcCach.addressCut(city, words);
				if(res == null){
					resp.sendError(400, "切词错误");
					return;
				}
				//精确�?
				JSONObject addressinfo = new JSONObject();	
				int accuracy = 0;
				String addresstype = "";
				if(res.roadAndDoor!=null)
				{
					accuracy = 100;
					addresstype = "Point";
				}
				else if(res.road!=null)
				{
					accuracy = 90;
					addresstype = "Line";
				}
				else if(res.block!=null)
				{
					accuracy = 80;
					addresstype = "Block";
				}
				else if(res.district!=null)
				{
					accuracy = 60;
					addresstype = "District";
				}
				else if(res.city!=null)
				{
					accuracy = 50;
					addresstype = "City";
				}
				addressinfo.put("accuracy",accuracy);
				addressinfo.put("addresstype",addresstype);
				
				//切词信息
				String  cutwordinfo = "";
				for(int i = 0; i  < res.cutwords.size(); i++){
					
					GCCach.CutUnit unit = res.cutwords.get(i);
					cutwordinfo += unit.words + "^" + unit.level + " ";
				}
				
				if(!res.address.isEmpty())
				{
					cutwordinfo += res.address + "^0";
				}
		 
				//返回�?
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
				int    poinumber = Math.min(10, res.points.size());
				JSONArray po = null;
				if(lsresult.has("points")){
                   if(output!=null&&output.equals("json")){
                	   po=lsresult.getJSONArray("points");  
                   }else{
                	   JSONObject tmp = lsresult.getJSONObject("points");   
                	   po=tmp.getJSONArray("point");  
                   } 
			    lsresult.remove("points");
				}	
				for(int i = 0; i < res.points.size(); i++)
				{
					JSONObject point = res.points.get(i);
					point.remove("id");
					point.put("zipcode", " ");
					point.put("telephone", " ");
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
					
					lsresult.append("points", point);
				}	
				if(po!=null){
				 for(int j=0;j<po.length();j++){
					JSONObject po1 = (JSONObject)po.get(j);
					lsresult.append("points", po1);
				 }
				}
				if(lsresult.length()==0){
					lsresult.put("status", "没有数据");	
				}
				ResponseUtil.writeJson2Response(resp,output,"utf-8",callback,lsresult);
			}
			
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
}
