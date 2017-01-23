package com.evialab.gisserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 



import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class GetJSAPI extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		boolean tr = "tr".equalsIgnoreCase(RequestUtil.getParameter(req,"v"));
 
		
		if (!server.processUID(req, tr ? "SE_JSAPI2" : "SE_JSAPI")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}		 
		 
		
		String uid = RequestUtil.getParameter(req,"uid");
		if(uid==null||uid=="")
		{
			ResponseUtil.error(resp, "参数错误");
			return ;
		}
	 
		 
		
		 
		String uidstr = "window.__UID__=\"" + uid + "\";\r\n";
		String serverPath = RequestUtil.getBasePath(req);
		String prjstr = "window.__PRJ__=\"" + serverPath + "\";\r\n";
		
		boolean canzip = false;
		String acc = req.getHeader("Accept-Encoding");
		if(acc != null)
		{
			canzip = acc.contains("gzip");
		}
		
		boolean citylist = "true".equalsIgnoreCase(RequestUtil.getParameter(req,"citylist"));
		boolean mapeditor = "true".equalsIgnoreCase(RequestUtil.getParameter(req,"mapeditor"));
		boolean debug = "true".equalsIgnoreCase(RequestUtil.getParameter(req,"debug"));	
		boolean mobile = "true".equalsIgnoreCase(RequestUtil.getParameter(req,"mobile"));
		
		//兼容泰瑞�?
		if(canzip)
		{
			ByteArrayOutputStream stm = new ByteArrayOutputStream();  
	        GZIPOutputStream gos = new GZIPOutputStream(stm); 
	        
	        if(tr || mobile)
	        	tr2Stream(gos, server, uidstr, prjstr, citylist,mapeditor,mobile);
	        else
	        	ev2Stream(gos,server,uidstr, prjstr, debug);
	        
	        gos.finish();
 
	         
	        stm.flush();  
	        stm.close();  
            
	        resp.setContentType("application/x-javascript");
	        resp.setCharacterEncoding("utf-8");  
	        resp.setHeader("Content-Encoding", "gzip");
	        resp.getOutputStream().write(stm.toByteArray()); 
		}
		else
		{
			resp.setContentType("application/x-javascript");
		   if(tr || mobile)
	        	tr2Stream(resp.getOutputStream(), server, uidstr, prjstr, citylist,mapeditor,mobile);
	        else
	        	ev2Stream(resp.getOutputStream(),server,uidstr, prjstr,debug);
		}
		 
	 
		 
 
	}
	
	
	void tr2Stream(OutputStream stm ,GisServer server, String uidstr, String prjstr, boolean citylist, boolean mapeditor,boolean mobile) throws UnsupportedEncodingException, IOException
	{
		stm.write(getServers(server).getBytes("utf-8"));
		stm.write(uidstr.getBytes("utf-8"));
		stm.write(prjstr.getBytes("utf-8"));
		
		if(mobile){
			stm.write(server.tr_mobile);
		}
		else
		{
			stm.write(server.evtrjs);
			if(citylist)
			{
				stm.write(server.tr_citylist);
			}
			if(mapeditor)
			{
				stm.write(server.tr_mapeditor);
			}
		}
		
		
	}
	
	void ev2Stream(OutputStream stm ,GisServer server, String uidstr, String prjstr,boolean debug) throws UnsupportedEncodingException, IOException
	{
		stm.write(getServers(server).getBytes("utf-8"));
		stm.write(uidstr.getBytes("utf-8"));
		stm.write(prjstr.getBytes("utf-8"));
		
		if(!debug){ 
			stm.write(server.eviamap_oljs);
			stm.write(server.eviamapex_oljs);
			stm.write(server.eviamapjs);
		}
		else
		{
			stm.write(server.openlayers);
			stm.write(server.eviamapex_src_oljs);
			stm.write(server.openlayers_evjs);
			stm.write(server.openlayers_compjs);
			stm.write(server.ev_serverjs);
			stm.write(server.ev_clusterjs);
		}
 
	}

	
	
	  String getServers(GisServer server)
	  {
		  if(server.servers!=null)
	        {
	        	String ss = "";
	        	for(String ser : server.servers)
	        	{
	        		if(ss.length() > 0)
	        			ss += ",";
	        		
	        		ss += "'" + ser + "'";
	        	}
	        	if(ss.length()>0)
	        		ss = "\r\n window._SERVERS_=[" + ss + "];\r\n";
	        	
	        	return ss;
	        	
	        	
	        }
		  return "";
	  }
	
}
