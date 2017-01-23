package com.evialab.gisserver;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.RequestUtil;

public class GetTile extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		
		long x = Long.parseLong(RequestUtil.getParameter(req,"x"));
		long y = Long.parseLong( RequestUtil.getParameter(req,"y"));
		long lev =  Long.parseLong(RequestUtil.getParameter(req,"lev"));
		
	 	
		//byte[] buffer = ArcGisCacheReader.GetImage(x, y, lev);
		byte[] buffer = ArcGisMiniBundleReader.GetImage(x, y, lev);
		
		if(buffer != null)		
		{
			response.setContentType("image/png");
			response.setContentLength(buffer.length);
			response.getOutputStream().write(buffer);
		}
		else
		{
			response.sendError(404);
		}
		

	}

}
