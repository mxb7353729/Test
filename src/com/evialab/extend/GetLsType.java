package com.evialab.extend;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.gisserver.GisServer;
import com.evialab.util.HttpRequest;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class GetLsType
 */
public class GetLsType extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetLsType() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 * 
	 * 
	 * @servername LsType
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// http://api.jisuapi.com/illegal/carorg?appkey=249b40044e2f9541

		response.setHeader("Access-Control-Allow-Origin", "*");
		// �?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}
		if (!server.processUID(request, "LsType")) {
			ResponseUtil.error(response, "无效UID");
			return;
		}
		try {

			String urlString = "http://api.jisuapi.com/illegal/lstype?appkey=249b40044e2f9541";
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
