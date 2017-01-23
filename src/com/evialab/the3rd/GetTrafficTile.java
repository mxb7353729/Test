package com.evialab.the3rd;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.gisserver.GisServer;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class GetTrafficTile
 */
public class GetTrafficTile extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetTrafficTile() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

resp.setHeader("Access-Control-Allow-Origin", "*");
		
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_Traffic")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}	
		if(server.render == null)
		{
			System.out.println("MapnikRender没有初始�?");
			ResponseUtil.error(resp, "服务器错�?");
			return;
		}
		try {
			int lev = Integer.parseInt(req.getParameter("lev"));
			int x = Integer.parseInt(req.getParameter("x"));
			int y = Integer.parseInt(req.getParameter("y"));

			Calendar sc=Calendar.getInstance(); 
			sc.setTime(new Date());
			 
			byte[] retimage = server.render.render(x, y, lev);
			
			Calendar dc=Calendar.getInstance(); 
			dc.setTime(new Date());
		    System.out.println("render:" + "\t x:"+ x + "\t y:"+ y + "\t z:"+ lev + "\t t:" + (dc.getTimeInMillis() - sc.getTimeInMillis()));
			
			if (retimage != null) {
				resp.setContentType("image/png");
				resp.setContentLength(retimage.length);
				resp.getOutputStream().write(retimage);
			} else {
				resp.sendError(404, "内部错误");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			resp.sendError(404, "内部错误");
		}
		
	}

}
