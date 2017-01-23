package com.evialab.gisserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.RequestUtil;

public class MonitorRequest extends HttpServlet {

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
	
	static private Vector<String> buffer = new Vector<String>();
	static private boolean bMonitor = false;
	
	static public void AddRequest(String url)
	{
		try
		{
		if(bMonitor)
			buffer.add(url);
			if(buffer.size() > 10000)
				bMonitor = false;
		}
		catch(Exception ex)
		{
			
		}
		return;
	}
	static public void AddRequest(String name, HttpServletRequest request)
	{
		try{
		if(bMonitor)
			buffer.add(name + "?" + request.getQueryString());
		if(buffer.size() > 10000)
			bMonitor = false;
		}
		catch(Exception ex)
		{
			
		}
		return;		
	}
	static public void ClearRequests()
	{
		buffer.removeAllElements();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/plain");
		int length =  Integer.parseInt(RequestUtil.getParameter(request, "length"));
		if(length > 100) length = 100;

		bMonitor = true;
		
		try
		{
			Thread.sleep(length * 1000);
		}
		catch(Exception e)
		{
			
		}
		
		bMonitor = false;
		
		for(String item : buffer)
		{
			response.getWriter().println(item);
			
		}
		buffer.removeAllElements();
		response.getWriter().flush();
	}

}
