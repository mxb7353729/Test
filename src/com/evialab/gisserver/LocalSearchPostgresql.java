package com.evialab.gisserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.DBPool;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class LocalSearchPostgresql {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	public static void doLocalSearchPostgresql(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException, IOException {
		String city = RequestUtil.getParameter(req, "city");// 非必�?
		String words = RequestUtil.getParameter(req, "words"); // 非必�?
		String address = RequestUtil.getParameter(req, "address"); // 非必�?

		String name = RequestUtil.getParameter(req, "name"); // 非必�?

		String area = RequestUtil.getParameter(req, "area"); // 非必�?
		int areanum = RequestUtil.getInt(req, "areanum", 1);
		String classp = RequestUtil.getParameter(req, "class"); // 非必�?
		int page = RequestUtil.getInt(req, "page", 1);
		int pagecap = RequestUtil.getInt(req, "pagecap", 10);

		int sort = RequestUtil.getInt(req, "sort", 0);
		int sorttype = RequestUtil.getInt(req, "sorttype", 0);

		int mode = RequestUtil.getInt(req, "mode", 0);
		int modea = RequestUtil.getInt(req, "modea", 0);

		int radius = RequestUtil.getInt(req, "radius", 500);
		int resultmode = RequestUtil.getInt(req, "resultmode", 31);
		String admincode = RequestUtil.getParameter(req, "admincode");

		boolean xml = "json".equals(RequestUtil.getParameter(req, "output"));

		String west = "", east = "", north = "", south = "";

		// 解析wkt 目前只做RECT的只�?
		if (!RequestUtil.isNull(area)) {
			area = area.trim();
			if (area.substring(0, "RECT".length()).compareToIgnoreCase("RECT") != 0) {
				if (area.substring(0, "POINT".length()).compareToIgnoreCase("POINT") != 0) {

					ResponseUtil.error(resp, "目前支持RECT类型，其他形状不支持");
					return;
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

		Connection con = null;
		try {
			con = DBPool.getDataInstance().getConnection();

			// 这里判断是否�?要根据城市名获取行政区划编码
			if (city != null && admincode == null) {
				admincode = city2admin(con, city);
			}

			// 添加其他参数，目前要处理的参数为words city admincode classp area
			String condition = "";
			if (!RequestUtil.isNull(words))
				condition += " name like '%" + words + "%'";
			if (!RequestUtil.isNull(admincode)) {
				if (!condition.isEmpty())
					condition += " and ";
				condition += " admincode = '" + admincode + "'";
			}

			if (!RequestUtil.isNull(classp)) {
				if (!condition.isEmpty())
					condition += " and ";
				condition += " kind = '" + classp + "'";
			}
			if (!RequestUtil.isNull(area)) {
				if (!condition.isEmpty())
					condition += " and ";
				condition += " ST_Contains(st_makebox2d(ST_Point(" + west + "," + south + "),ST_Point(" + east + "," + north + ")), geom) ";
			}

			if (!condition.isEmpty())
				condition = " where " + condition;

			// 先查询�?�个�?
			int total = 0;
			ResultSet rscount = con.createStatement().executeQuery("select count(*) from v_poi " + condition);
			if (rscount.next()) {
				total = rscount.getInt(1);
			}
			rscount.close();

			Object jo = xml ? new JSONObject() : new JSONArray();
			double minx = 180;
			double maxx = -180;
			double miny = 180;
			double maxy = -180;
			if (total > 0) {
				// 查询具体数�??
				String sql = "select * from v_poi " + condition + " limit " + pagecap + " offset " + ((page - 1) * pagecap);

				PreparedStatement stm = con.prepareStatement(sql);

				String set = Integer.toBinaryString(resultmode);

				ResultSet rs = stm.executeQuery();

				while (rs.next()) {
					JSONObject poi = new JSONObject();

					if ("1".equals(set.substring(0, 1)))
						poi.put("Name", rs.getString("Name"));

					if ("1".equals(set.substring(1, 1)))
						poi.put("Address", rs.getString("Address"));

					if ("1".equals(set.substring(2, 1)))
						poi.put("Telephone", rs.getString("Telephone"));

					if ("1".equals(set.substring(3, 1)))
						poi.put("Admin_Code", rs.getString("Admin_Code"));

					if ("1".equals(set.substring(4, 1)))
						poi.put("ZipCode", rs.getString("ZipCode"));

					poi.put("lng", rs.getDouble("x"));
					poi.put("lat", rs.getDouble("y"));

					if (jo instanceof JSONObject)
						((JSONObject) jo).append("point", poi);
					else if (jo instanceof JSONArray)
						((JSONArray) jo).put(poi);
				}

				rs.close();
			}

			JSONObject jresult = new JSONObject();
			jresult.put("points", jo);
			jresult.put("curpage", page);
			jresult.put("pagecount", (total - 1) / pagecap + 1);
			jresult.put("total", total);
			jresult.put("curresult", 1);
			jresult.put("bound", Double.toString(minx) + "," + Double.toString(miny) + ";" + Double.toString(maxx) + "," + Double.toString(maxy));

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			String encoding = RequestUtil.getParameter(req, "encoding");

			ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);

		} catch (Exception e) {

			System.out.println("localsearch错误:" + e.getMessage());
		} finally {
			if (con != null)
				DBPool.getDataInstance().closeConnection(con);
		}

	}

	static String city2admin(Connection con, String city) {
		String sql = "select ccode from admin_city where cname like '" + city + "%'";
		try {

			PreparedStatement stm = con.prepareStatement(sql);

			ResultSet rs = stm.executeQuery();

			String admin = null;
			if (rs.next()) {
				admin = rs.getString(1);

			}

			rs.close();

			return admin;

		} catch (Exception e) {

			return null;
		}
	}
}
