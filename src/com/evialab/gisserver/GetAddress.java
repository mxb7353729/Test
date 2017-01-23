package com.evialab.gisserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.LinkedList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;
import org.json.JSONObject;
public class GetAddress extends HttpServlet {

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


	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		
		response.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}

		boolean isOldVersion = false;

		String uid = RequestUtil.getParameter(request, "uid");
		String pt = request.getParameter("point");
		String ip = RequestUtil.getIp(request);
		
		String output = request.getParameter("output");
		String callback = request.getParameter("callback");
		String encoding = request.getParameter("encoding");
		String version = request.getParameter("v");
		
		if (version == null)
			version = "1";
		double x = Double.parseDouble(pt.substring(0, pt.indexOf(',')));
		double y = Double.parseDouble(pt.substring(pt.indexOf(',') + 1));
		

		//JSONObject jo = RGCPoiSearch.SearchAddress(x, y);
		
		//ResponseUtil.writeJson2Response(response, output, "utf-8", "", jo);
		ResponseUtil.error(response, "未实现，请联系技术支�?");
		
	}

}
