package com.evialab.gisserver;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class LocalSearch extends HttpServlet {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	@Override
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

		if (!server.processUID(req, "SE_LS")) {
			ResponseUtil.error(resp, output, encoding, callback, "无效UID");
			return;
		}
 
 
		JSONObject jresult = new JSONObject();

		try {

			String st = RequestUtil.getParameter(req, "st");
			if (st.equalsIgnoreCase("LocalSearch")) {
				int result = doGetLocalSearch(jresult, req, resp, !"json".equals(output));

				if (result == 0) {
					ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);
				} else {

					ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);
				}

			} else if (st.equalsIgnoreCase("Navigate")) {
				jresult = doGetNavigate(req, resp);
			} else if (st.equalsIgnoreCase("Obtain")) {
				jresult = doGetObtain(req, resp);
			} else if (st.equalsIgnoreCase("Meta")) {
				jresult = doGetMeta(req, resp);
			} else {
				ResponseUtil.error(resp, output, encoding, callback, "st参数错误");
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
			return LocalSearchLucene.NameSearcher(jresult, city, words, classp, r, page, pagecap, resultmode, xml);
		} else {
			return LocalSearchLucene.NameSearcher(jresult, city, words, classp, null, page, pagecap, resultmode, xml);
		}

	}

	JSONObject doGetNavigate(HttpServletRequest req, HttpServletResponse resp) {
		JSONObject jresult = new JSONObject();

		return jresult;
	}

	JSONObject doGetObtain(HttpServletRequest req, HttpServletResponse resp) {
		JSONObject jresult = new JSONObject();

		return jresult;
	}

	JSONObject doGetMeta(HttpServletRequest req, HttpServletResponse resp) {
		JSONObject jresult = new JSONObject();

		return jresult;
	}

	JSONObject doGetLs(HttpServletRequest req, HttpServletResponse resp, boolean xml) {
		JSONObject jresult = new JSONObject();
		String city = RequestUtil.getParameter(req, "city"); // 必须
		if (RequestUtil.isNull(city)) {
			jresult.put("status", "error");
			jresult.put("error", "city参数必须指定");
			return jresult;
		}

		// 获取参数 某前制作�?单的 类型 关键�? �? 中心�? + 半径模式 �? RECT CIRCLE类型的查�?

		String words = RequestUtil.getParameter(req, "words"); // 非必�?

		String west = "", east = "", north = "", south = "";

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

		int lsr = LocalSearchLucene.NameSearcher(jresult, city, words, null, null, page, pagecap, resultmode, xml);
		return jresult;
	}

	
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(req,response);
	}
}
