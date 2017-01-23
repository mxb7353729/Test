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
 * Servlet implementation class GetCarOrg
 */
public class GetCarOrg extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetCarOrg() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response) 用户账号�? 13691148809 邮箱地址�? azzhao@163.com appkey �?
	 *      249b40044e2f9541 appsecret�? cOPYkryNxREzwwQNTojEa6ANoFCdOAUg
	 * 
	 * @param onlysupport
	 *            int 是否只返回支持的城市默认0返回全部 1只返回支持的
	 * @return province string �?
	 * @return city string �?
	 * @return carorg string 市管�?和省管局不一样的，市管局可以使用省管�?的参数�??
	 * @return frameno int 车架号需要输入的长度 100为全部输�? 0为不输入
	 * @return engineno int 发动机号�?要输入的长度
	 * @return updtetime string 更新时间
	 * @return lsprefix string 车牌前缀
	 * @return lsnum string 车牌首字�?
	 * 
	 * 
	 * @servername CarOrg
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
		if (!server.processUID(request, "CarOrg")) {
			ResponseUtil.error(response, "无效UID");
			return;
		}
		String supcity = "";

		try {

			int onlysupport = 0;
			supcity = request.getParameter("getsupcity");
			if (supcity.equals("true"))
				onlysupport = 1;
			String urlString = "http://api.jisuapi.com/illegal/carorg?appkey=249b40044e2f9541&onlysupport="
					+ onlysupport;
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