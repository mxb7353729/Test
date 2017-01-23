package com.evialab.gisserver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class HmSearchServlet
 */
public class HmSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HmSearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		// �?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		String output = req.getParameter("output");
		String callback = req.getParameter("callback");
		String encoding = req.getParameter("encoding");

		//if (!server.processUID(req, "SE_LS")) {
		//	ResponseUtil.error(resp, output, encoding, callback, "无效UID");
		//	return;
		//}
 
 
		JSONObject jresult = new JSONObject();

		try {

			String st = RequestUtil.getParameter(req, "st");
			if (st.equalsIgnoreCase("HamletSearch")) {
				int result = doGetLocalSearch(jresult, req, resp, !"json".equals(output));

				if (result == 0) {
					ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);
				} else {

					ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);
				}

			}
		} catch (Exception e) {

			ResponseUtil.error(resp, output, encoding, callback, "查询异常:" + e.getLocalizedMessage());
		}
	}
	int doGetLocalSearch(JSONObject jresult, HttpServletRequest req, HttpServletResponse resp, boolean xml) throws Exception {

		String city = RequestUtil.getParameter(req, "city"); // 必须
		if (RequestUtil.isNull(city)) {
			jresult.put("status", "error");
			jresult.put("error", "city参数必须指定");
			return 1;
		}

		// 获取参数 某前制作�?单的 类型 关键�? �? 中心�? + 半径模式 �? RECT CIRCLE类型的查�?
		String classp = RequestUtil.getParameter(req, "class"); // 非必�?

		String words = RequestUtil.getParameter(req, "words"); // 非必�?
		
		System.out.println(city + "," + words);


		String address = RequestUtil.getParameter(req, "address");

		String area = RequestUtil.getParameter(req, "area"); // 非必�?

		if (RequestUtil.isNull(area) && RequestUtil.isNull(words) && RequestUtil.isNull(classp) && RequestUtil.isNull(address)) {
			jresult.put("status", "error");
			jresult.put("error", "words,address,class,area参数至少有一�?");
			return 1;
		}

		String west = "", east = "", north = "", south = "";

		// 解析wkt 目前只做RECT的只�?
		if (area != null) {
			/*
			 * WKTReader wktr = new WKTReader(); try { Geometry gem =
			 * wktr.read(area); //目前演示只支持RECT
			 * 
			 * } catch (ParseException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
			area = area.trim();
			if (area.substring(0, "RECT".length()).compareToIgnoreCase("RECT") != 0) {
				if (area.substring(0, "POINT".length()).compareToIgnoreCase("POINT") != 0) {

					jresult.put("status", "error");
					jresult.put("error", "支持RECT和POINT类型，其他形状不支持");
					return 1;

				}
			}
			area = area.replace("RECT(", "");
			area = area.replace(")", "");
			area = area.replace(",", " ");
			String[] pp = area.split(" ");
			west = (pp[0]);
			south = (pp[1]);
			east = (pp[2]);
			north = (pp[3]);
		}

		@SuppressWarnings("unused")
		int radius = 500;
		try {
			radius = Integer.parseInt(RequestUtil.getParameter(req, "radius"));
		} catch (Exception e) {
		}

		// 分页参数
		int page = 1;
		try {
			page = Integer.parseInt(RequestUtil.getParameter(req, "page"));
		} catch (Exception e) {
		}

		int pagecap = 10;
		try {
			pagecap = Integer.parseInt(RequestUtil.getParameter(req, "pagecap"));
		} catch (Exception e) {
		}

		int resultmode = 31;
		try {
			pagecap = Integer.parseInt(RequestUtil.getParameter(req, "resultmode"));
		} catch (Exception e) {
		}

		// LocalSearchLucene.InitSearcher();

		if (area != null) {
			String[] r = { west, east, south, north };
			return HmSearch.NameSearcher(jresult, city, words, classp, r, page, pagecap, resultmode, xml);
		} else {
			return HmSearch.NameSearcher(jresult, city, words, classp, null, page, pagecap, resultmode, xml);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
