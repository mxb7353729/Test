package com.evialab.gisserver;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class RedirectAPI extends HttpServlet{

	private static final long serialVersionUID = 1L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		resp.setHeader("Access-Control-Allow-Origin", "*");
		String st = RequestUtil.getParameter(req,"st");
		String type = "";
		if(st.equalsIgnoreCase("RC") || st.equalsIgnoreCase("Nearest"))
		{
			type = "SE_RC";
		}
		else if(st.equalsIgnoreCase("Rgc"))
		{
			type = "SE_RGC";
		}
		else if(st.equalsIgnoreCase("rgc2"))
		{
			type = "SE_RGC2";
		}
		else if(st.equalsIgnoreCase("Geocoding"))
		{
			type = "SE_GC";
		}
		else
		{
			ResponseUtil.error(resp, "参数错误");
			return;
		}
		
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, type)) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}
	 	
		
		
		Map params = req.getParameterMap();
		String url = "http://e.gis.cttic.cn:9000/";
		url+= type;
		url+= "?uid=tjysd";
		Set set = params.entrySet();
		
		for(Object obj : set)
		{
			Map.Entry en = (Map.Entry)obj;
			String p = (String) en.getKey();
			if(!p.equalsIgnoreCase("uid"))
			{
				String v = req.getParameter(p);
				
				v = URLEncoder.encode(v,"utf8");
				
				url+="&"+p+"="+v;
			}
		}
	 
		QuJiaProxy.getURL(url,resp);
		
 	 
	}

	

}
