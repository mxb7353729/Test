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

public class GetJSLIB extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		// �?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_JSLIB")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}

		String uid = RequestUtil.getParameter(req, "uid");
		if (uid == null || uid == "") {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		resp.setContentType("application/x-javascript");

		boolean canzip = false;
		String acc = req.getHeader("Accept-Encoding");
		if(acc != null)
		{
			canzip = acc.contains("gzip");
		}
		
		if(canzip)
		{
			ByteArrayOutputStream stm = new ByteArrayOutputStream();  
	        GZIPOutputStream gos = new GZIPOutputStream(stm); 
	        
	        tr2Stream(gos,uid, server, req);
	        
	        gos.finish();
 
	         
	        stm.flush();  
	        stm.close();  
 
	        resp.setCharacterEncoding("utf-8");  
	        resp.setHeader("Content-Encoding", "gzip");
	        resp.getOutputStream().write(stm.toByteArray()); 
		}
		else
		{
			tr2Stream(resp.getOutputStream(),uid, server, req);
		}
		 
		/*if ("true".equals(RequestUtil.getParameter(req, "service"))) {
			String uidstr = "window.__UID__=\"" + uid + "\";\r\n";
			resp.getOutputStream().write(uidstr.getBytes("utf-8"));

			String serverPath = RequestUtil.getBasePath(req);

			String prjstr = "window.__PRJ__=\"" + serverPath + "\";\r\n";
			resp.getOutputStream().write(prjstr.getBytes("utf-8"));

			resp.getOutputStream().write(server.tr_server);
		}

		if ("true".equals(RequestUtil.getParameter(req, "maptool"))) {
			resp.getOutputStream().write(server.tr_maptool);
		}
		if ("true".equals(RequestUtil.getParameter(req, "cluster"))) {
			resp.getOutputStream().write(server.tr_cluster);
		}
		if ("true".equals(RequestUtil.getParameter(req, "mappicker"))) {
			resp.getOutputStream().write(server.tr_mappicker);
		}
		if ("true".equals(RequestUtil.getParameter(req, "mapsnap"))) {
			resp.getOutputStream().write(server.tr_mapsnap);
		}*/
		 

		return;

	}

	void tr2Stream(OutputStream stm, String uid, GisServer server, HttpServletRequest req) throws UnsupportedEncodingException, IOException
	{
		if ("true".equals(RequestUtil.getParameter(req, "service"))) {
			String uidstr = "window.__UID__=\"" + uid + "\";\r\n";
			stm.write(uidstr.getBytes("utf-8"));

			String serverPath = RequestUtil.getBasePath(req);

			String prjstr = "window.__PRJ__=\"" + serverPath + "\";\r\n";
			stm.write(prjstr.getBytes("utf-8"));

			stm.write(server.tr_server);
		}

		if ("true".equals(RequestUtil.getParameter(req, "maptool"))) {
			stm.write(server.tr_maptool);
		}
		if ("true".equals(RequestUtil.getParameter(req, "cluster"))) {
			stm.write(server.tr_cluster);
		}
		if ("true".equals(RequestUtil.getParameter(req, "mappicker"))) {
			stm.write(server.tr_mappicker);
		}
		if ("true".equals(RequestUtil.getParameter(req, "mapsnap"))) {
			stm.write(server.tr_mapsnap);
		}
	}
}