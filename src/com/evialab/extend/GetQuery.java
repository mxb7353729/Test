package com.evialab.extend;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.gisserver.GisServer;
import com.evialab.util.HttpRequest;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class GetQuery
 */
public class GetQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetQuery() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * 
	 *      carorg string �? 管局名称 lsprefix string �? 车牌前缀 lsnum string �? 车牌剩余部分
	 *      lstype string �? 车辆类型 frameno string �? 车架�? 根据管局�?要输�? engineno string �?
	 *      发动机号 根据管局�?要输�? iscity int �? 是否返回城市 1返回 默认0不返�? 不一�?100%返回结果，准确度90%
	 *      town、lat、lng仅供参�??
	 * 
	 * @servername GetQuery
	 * 
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// http://api.jisuapi.com/illegal/carorg?appkey=249b40044e2f9541
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		// �?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}
		if (!server.processUID(request, "GetQuery")) {
			ResponseUtil.error(response, "无效UID");
			return;
		}

		String carorg = "";
		String lsprefix = "";
		String lsnum = "";
		String lstype = "";
		String frameno = "";
		String engineno = "";
		String lscity = "";

		try {
			int iscity = 1;
			carorg = request.getParameter("carorg");
			lsprefix = request.getParameter("lsprefix");
			lsnum = request.getParameter("lsnum");
			lstype = request.getParameter("lstype");
			frameno = request.getParameter("frameno");
			engineno = request.getParameter("engineno");
			lscity = request.getParameter("iscity");
			if (lscity.equals("false"))
				iscity = 0;
			
			String urlString = "http://api.jisuapi.com/illegal/query?appkey=249b40044e2f9541" + "&carorg=" + carorg
					+ "&lsprefix=" + URLEncoder.encode(lsprefix,"utf-8") + "&lstype=" + lstype + "&lsnum=" + lsnum + "&frameno=" + frameno
					+ "&engineno=" + engineno + "&iscity=" + iscity;
			//System.out.println(urlString);
			response.getOutputStream().write(HttpRequest.sendGet(urlString).toString().getBytes("utf-8"));
		} catch (Exception e) {
			response.getOutputStream().write("参数错误".getBytes("utf-8"));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
