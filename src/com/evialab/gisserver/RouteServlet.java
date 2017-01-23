package com.evialab.gisserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.util.ResponseUtil;
import com.jetmap.route.RouteApi;

/**
 * Servlet implementation class RouteServlet
 */
public class RouteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
    public static int parse(String s)
    {
        String s1 = s.trim();
        try
        {
            return Integer.parseInt(s1);
        }
        catch(NumberFormatException numberformatexception) { }
        try
        {
            float f = Float.parseFloat(s1);
            return (int)((double)f + 0.5D);
        }
        catch(NumberFormatException numberformatexception1)
        {
            return -1;
        }
    }
    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest httpservletrequest, HttpServletResponse resp) throws ServletException, IOException {

		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}
		
		if(!RouteApi.nativeInited)
        {
            String s = "ERROR[-10]";
            resp.setContentType("text/plain");
            resp.setContentLength(s.length());
            PrintWriter printwriter = resp.getWriter();
            printwriter.println(s);
            return;
        }
        String s1 = httpservletrequest.getParameter("x0");
        String s2 = httpservletrequest.getParameter("y0");
        String s3 = httpservletrequest.getParameter("x1");
        String s4 = httpservletrequest.getParameter("y1");
        String s5 = httpservletrequest.getParameter("xv");
        String s7 = httpservletrequest.getParameter("yv");
        String s9 = httpservletrequest.getParameter("priority");
        int i = -1;
        int j = -1;
        int k = -1;
        int l = -1;
        int i1 = -1;
        int k1 = -1;
        int i2 = 0;
        if(s1 != null)
            i = parse(s1);
        if(s2 != null)
            j = parse(s2);
        if(s3 != null)
            k = parse(s3);
        if(s4 != null)
            l = parse(s4);
        if(s5 != null)
            i1 = parse(s5);
        if(s7 != null)
            k1 = parse(s7);
        if(s9 != null)
            i2 = parse(s9);
        if(i < 0 || j < 0 || k < 0 || l < 0)
        {
            String s10 = "ERROR[-11]";
            resp.setContentType("text/plain");
            resp.setContentLength(s10.length());
            PrintWriter printwriter1 = resp.getWriter();
            printwriter1.println(s10);
            return;
        }
        byte byte0 = 32;
        int j2 = 0;
        int ai[] = new int[byte0 * 2];
        if(i1 > 0 && k1 > 0)
        {
            ai[j2 * 2] = i1;
            ai[j2 * 2 + 1] = k1;
            j2++;
        }
        int k2 = 1;
        do
        {
            if(j2 >= byte0)
                break;
            int j1 = -1;
            int l1 = -1;
            String s6 = httpservletrequest.getParameter((new StringBuilder()).append("xv").append(k2).toString());
            String s8 = httpservletrequest.getParameter((new StringBuilder()).append("yv").append(k2).toString());
            if(s6 != null)
                j1 = parse(s6);
            if(s8 != null)
                l1 = parse(s8);
            k2++;
            if(j1 <= 0 || l1 <= 0)
                break;
            ai[j2 * 2] = j1;
            ai[j2 * 2 + 1] = l1;
            j2++;
        } while(true);
        String s11 = RouteApi.Plan(i, j, k, l, j2, ai, i2);
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-type", "text/plain;charset=UTF-8");
        PrintWriter printwriter2 = resp.getWriter();
        printwriter2.write(s11);
		
	}

	

}
