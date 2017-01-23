package com.evialab.gisserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.gisserver.ChinaAdmin.CodeInfo;
import com.evialab.util.DBPool;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class AdminSearch extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// System.out.println(req.getQueryString());
		resp.setHeader("Access-Control-Allow-Origin", "*");
		// �?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_AS")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}
		// 获取查询类型
		try {

			String type = RequestUtil.getParameter(req, "type");
			if (type == null)
				type = RequestUtil.getParameter(req, "t");
			// 获取边界
			if (type == null || "bounds".equals(type) || "" == type) {
				getBounds(server.admin, req, resp);
			}
			// 根据坐标返回行政区划id
			else if ("getid".equals(type)) {
				getDistrict(server.admin,req, resp);
			}
			// 根据县区id查询同父的县区信�?
			else if ("getdis".equals(type)) {
				getDis(server.admin, req, resp);
			}
			// 根据城市id查询同父的城市信�?
			else if ("getcity".equals(type)) {
				getCity(server.admin, req, resp);
			}
			// 根据id查询省信�?
			else if ("getprovince".equals(type)) {
				getProvince(server.admin, req, resp);
			}
			// 根据id查询该城市下�?有县�?
			else if ("city".equals(type)) {
				city(server.admin, req, resp);
			}
			// 根据id查询该省�?有地�?
			else if ("province".equals(type)) {
				province(server.admin, req, resp);
			}
			// 根据坐标查询三级行政区划
			else if ("position".equals(type)) {
				position(server.admin, req, resp);
			}
			// 根据admincode返回下级行政区划
			else if ("listsub".equals(type)) {
				listsub(server.admin, req, resp);
			}
			// 根据行政区划编码或�?�名称返回行政区划信息（不包含边�?)
			else if ("info".equals(type)) {
				info(server.admin, req, resp);

			}

		} catch (Exception e) {

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.error(resp, output, "utf-8", callback, "参数错误");
			// ResponseUtil.error(resp, "参数错误");
		}
		return;
	}

	// 根据坐标查询三级行政区划
	void info(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String name = RequestUtil.getParameter(req, "name");
		String code = RequestUtil.getParameter(req, "code");
		if (RequestUtil.isNull(name) && RequestUtil.isNull(code)) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}
		// 根据名称获取code
		if (RequestUtil.isNull(code)) {

			PreparedStatement stmt = admin.getStmtName();
			synchronized (stmt) {
				try {
 
					stmt.setString(1, "%" + name + "%");

					ResultSet rs = stmt.executeQuery();
					if (rs.next()) {
						code = rs.getString("code");
					}
					rs.close();

					if (RequestUtil.isNull(code)) {
						ResponseUtil.error(resp, "未查到该行政区划");
						return;
					}

				} catch (SQLException e) {
					ResponseUtil.error(resp, "数据库错�?");
					return;
				}
			}
		}
		// 根据code判定
		int c = Integer.parseInt(code.substring(0, 6));
		// 判断code的类�?
		int p = Integer.parseInt(code.substring(2, 4));
		int d = Integer.parseInt(code.substring(4, 6));
		CodeInfo info = null;
		// 省级行政区划
		JSONObject dst = new JSONObject();

		if (p == 0 && d == 0) {
			info = admin.getProvinceInfo(c);

			dst.put("code", info.code);
			dst.put("province", info.pname);
		}
		// 市级行政区划
		else if (d == 99 || d == 0) {
			info = admin.getCityInfo(c);

			dst.put("code", info.code);
			dst.put("pcode", info.pcode);

			dst.put("province", info.pname);
			dst.put("city", info.cname);

		}
		// 县级行政区划
		else {
			info = admin.getDisInfo(c);

			dst.put("code", info.code);
			dst.put("pcode", info.pcode);
			dst.put("ccode", info.ccode);

			dst.put("province", info.pname);
			dst.put("city", info.cname);
			dst.put("dis", info.name);

		}

		dst.put("level", info.level);
		dst.put("lon", info.lon);
		dst.put("lat", info.lat);

		String output = RequestUtil.getParameter(req, "output");
		String callback = RequestUtil.getParameter(req, "callback");
		ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, dst);

	}

	// 根据坐标查询三级行政区划
	void position(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		double lon = 0;
		double lat = 0;
		String coordinate = RequestUtil.getParameter(req, "cd");// 获取坐标类型

		try {
			String pt = RequestUtil.getParameter(req, "pos");
			if (coordinate != null) {
				if (coordinate.equalsIgnoreCase("wgs84")) {// 如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标

					pt = GCJCoordTrans.tranpoint(pt);
				}
			}
			String[] pos = pt.split(",");
			lon = Double.parseDouble(pos[0]);
			lat = Double.parseDouble(pos[1]);

		} catch (Exception e) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}
		// 查询获得该位置的rgc信息 返回
	 
		JSONObject info = RGCPostgreSql.getAdmin(lon, lat);

		if (info == null || !info.has("district")) {
			ResponseUtil.error(resp, "RGC错误");
			return;
		}
		String code = info.getString("district").substring(0, 6);

		CodeInfo cityinfo = admin.getDisInfo(Integer.parseInt(code));
		if (cityinfo == null) {
			ResponseUtil.error(resp, "cityinfo错误");
			return;
		}

		JSONObject dst = new JSONObject();
		dst.put("code", cityinfo.code);
		dst.put("ccode", cityinfo.ccode);
		dst.put("pcode", cityinfo.pcode);

		dst.put("province", cityinfo.pname);
		dst.put("city", cityinfo.cname);
		dst.put("dis", cityinfo.name);
		dst.put("level", cityinfo.level);
		dst.put("lon", cityinfo.lon);
		dst.put("lat", cityinfo.lat);

		String output = RequestUtil.getParameter(req, "output");
		String callback = RequestUtil.getParameter(req, "callback");
		ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, dst);
	}

	// 根据admincode 返回下级行政区划
	void listsub(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String id = RequestUtil.getParameter(req, "id");

		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();

		// 获取全国
		if (id == null || "".equals(id)) {
			PreparedStatement stmt = admin.getStmtProvinces();
			synchronized (stmt) {
				try {
					ResultSet rs = stmt.executeQuery();
					while (rs.next()) {
						CodeInfo info = admin.getProvinceInfo(rs);
						diss.add(info);
					}
				} catch (SQLException e) {
				}
			}
		} else {
			int code = Integer.parseInt(id.substring(0, 6));
			// 判断code的类�?
			int p = Integer.parseInt(id.substring(2, 4));
			int d = Integer.parseInt(id.substring(4, 6));

			// 省级行政区划
			if (p == 0 && d == 0) {
				PreparedStatement stmt = admin.getStmtCities();
				synchronized (stmt) {
					try {
						stmt.setInt(1, code);
						ResultSet rs = stmt.executeQuery();
						while (rs.next()) {
							CodeInfo info = admin.getCityInfo(rs);
							diss.add(info);
						}
					} catch (SQLException e) {
					}
				}
			}
			// 市级行政区划
			else if (d == 99 || d == 0) {
				PreparedStatement stmt = admin.getStmtDiss();
				synchronized (stmt) {
					try {
						stmt.setInt(1, code);
						ResultSet rs = stmt.executeQuery();
						while (rs.next()) {
							CodeInfo info = admin.getDisInfo(rs);
							diss.add(info);
						}
					} catch (SQLException e) {
					}
				}
			}
			// 县级行政区划
			else {

			}
		}

		JSONObject result = new JSONObject();

		JSONArray ids = new JSONArray();
		for (int i = 0; i < diss.size(); i++) {
			JSONObject obj = new JSONObject();

			CodeInfo info = diss.get(i);

			obj.put("id", info.code);
			obj.put("lat", info.lat);
			obj.put("level", info.level);
			obj.put("name", info.name);
			obj.put("lon", info.lon);

			ids.put(obj);
		}
		result.put("id", id);
		result.put("subs", ids);

		String output = RequestUtil.getParameter(req, "output");
		String callback = RequestUtil.getParameter(req, "callback");
		ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);

	}

	void city(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = RequestUtil.getParameter(req, "id");
		if (id == null || id.length() < 6) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		int code = Integer.parseInt(id.substring(0, 6));

		// 查询该code�?对应的城�?
		CodeInfo cityinfo = admin.getCityInfo(code);
		if (cityinfo == null) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		// 查询该城市的区级列表
		PreparedStatement stmt = admin.getStmtDiss();
		
		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();
		synchronized (stmt) {
			try {
				stmt.setInt(1, cityinfo.code);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					CodeInfo info = admin.getDisInfo(rs);
					diss.add(info);
				}
			} catch (SQLException e) {
			}
		}

		// 组织返回数据
		JSONObject result = new JSONObject();

		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {
			result.put("id", new Object[] { cityinfo.code + "" });
			result.put("la", new Object[] { (int) (cityinfo.lat * 100000) });
			result.put("le", new Object[] { cityinfo.level });
			result.put("na", new Object[] { cityinfo.name });
			result.put("lo", new Object[] { (int) (cityinfo.lon * 100000) });

			JSONObject dis = new JSONObject();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);
				obj.put("citiesid", new Object[] { info.ccode + "" });
				obj.put("id", new Object[] { info.code + "" });
				obj.put("la", new Object[] { (int) (info.lat * 100000) });
				obj.put("le", new Object[] { info.level });
				obj.put("na", new Object[] { info.name });
				obj.put("lo", new Object[] { (int) (info.lon * 100000) });

				dis.put(i + "", new Object[] { obj });
			}

			result.put("dis", new Object[] { dis });

			String callback = RequestUtil.getParameter(req, "callback");
			String res = result.toString();
			if (callback != null) {
				res = callback + "&&" + callback + "(" + res + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(res.getBytes("utf-8"));
		} else {
			result.put("id", cityinfo.code);
			result.put("la", cityinfo.lat);
			result.put("le", cityinfo.level);
			result.put("na", cityinfo.name);
			result.put("lo", cityinfo.lon);

			JSONArray ids = new JSONArray();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);

				obj.put("citiesid", info.ccode);
				obj.put("id", info.code);
				obj.put("la", info.lat);
				obj.put("le", info.level);
				obj.put("na", info.name);
				obj.put("lo", info.lon);

				ids.put(obj);
			}

			result.put("dis", ids);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		}
	}

	void province(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = RequestUtil.getParameter(req, "id");
		if (id == null || id.length() < 6) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		int code = Integer.parseInt(id.substring(0, 6));

		// 查询该code�?对应的城�?
		CodeInfo provinceinfo = admin.getProvinceInfo(code);
		if (provinceinfo == null) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		// 查询该城市的省的城市列表
		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();
		
		PreparedStatement stmt = admin.getStmtCities();
		
		synchronized (stmt) {
			try {
				stmt.setInt(1, provinceinfo.pcode);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					CodeInfo info = admin.getCityInfo(rs);
					diss.add(info);
				}
			} catch (SQLException e) {
			}
		}

		// 组织返回数据
		JSONObject result = new JSONObject();

		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {

			result.put("id", new Object[] { provinceinfo.code + "" });
			result.put("la", new Object[] { (int) (provinceinfo.lat * 100000) });
			result.put("le", new Object[] { provinceinfo.level });
			result.put("na", new Object[] { provinceinfo.name });
			result.put("lo", new Object[] { (int) (provinceinfo.lon * 100000) });

			JSONObject cities = new JSONObject();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);
				obj.put("provinceid", new Object[] { info.pcode + "" });
				obj.put("id", new Object[] { info.code + "" });
				obj.put("la", new Object[] { (int) (info.lat * 100000) });
				obj.put("le", new Object[] { info.level });
				obj.put("na", new Object[] { info.name });
				obj.put("lo", new Object[] { (int) (info.lon * 100000) });

				cities.put(i + "", new Object[] { obj });
			}

			result.put("cities", new Object[] { cities });

			String callback = RequestUtil.getParameter(req, "callback");
			String res = result.toString();
			if (callback != null) {
				res = callback + "&&" + callback + "(" + res + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(res.getBytes("utf-8"));
		} else {

			result.put("id", provinceinfo.code);
			result.put("la", provinceinfo.lat);
			result.put("le", provinceinfo.level);
			result.put("na", provinceinfo.name);
			result.put("lo", provinceinfo.lon);

			JSONArray cities = new JSONArray();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);

				obj.put("provinceid", info.pcode);
				obj.put("id", info.code);
				obj.put("la", info.lat);
				obj.put("le", info.level);
				obj.put("na", info.name);
				obj.put("lo", info.lon);

				cities.put(obj);
			}

			result.put("cities", cities);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		}
	}

	// 返回省列�?
	void getProvince(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = RequestUtil.getParameter(req, "id");
		if (id == null || id.length() < 6) {
			id = "110000";
		}

		int code = Integer.parseInt(id.substring(0, 6));

		// 查询该code�?对应的城�?
		CodeInfo provinceinfo = admin.getProvinceInfo(code);
		if (provinceinfo == null) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		// 查询该城市的省的城市列表
		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();
		PreparedStatement stmt = admin.getStmtProvinces();
		synchronized (stmt) {
			try {
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					CodeInfo info = admin.getProvinceInfo(rs);
					diss.add(info);
				}
			} catch (SQLException e) {
			}
		}

		// 组织返回数据
		JSONObject result = new JSONObject();

		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {

			result.put("id", new Object[] { provinceinfo.code + "" });
			result.put("la", new Object[] { (int) (provinceinfo.lat * 100000) });
			result.put("le", new Object[] { provinceinfo.level });
			result.put("na", new Object[] { provinceinfo.name });
			result.put("lo", new Object[] { (int) (provinceinfo.lon * 100000) });

			JSONObject provinces = new JSONObject();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);
				obj.put("id", new Object[] { info.code + "" });
				obj.put("la", new Object[] { (int) (info.lat * 100000) });
				obj.put("le", new Object[] { info.level });
				obj.put("na", new Object[] { info.name });
				obj.put("lo", new Object[] { (int) (info.lon * 100000) });

				provinces.put(i + "", new Object[] { obj });
			}

			result.put("cities1", new Object[] { provinces });

			String callback = RequestUtil.getParameter(req, "callback");
			String res = result.toString();
			if (callback != null) {
				res = callback + "&&" + callback + "(" + res + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(res.getBytes("utf-8"));
		} else {

			result.put("id", provinceinfo.code);
			result.put("la", provinceinfo.lat);
			result.put("le", provinceinfo.level);
			result.put("na", provinceinfo.name);
			result.put("lo", provinceinfo.lon);

			JSONArray provinces = new JSONArray();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);

				obj.put("id", info.code);
				obj.put("la", info.lat);
				obj.put("le", info.level);
				obj.put("na", info.name);
				obj.put("lo", info.lon);

				provinces.put(obj);
			}

			result.put("provinces", provinces);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		}
	}

	// 返回城市列表
	void getCity(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = RequestUtil.getParameter(req, "id");
		if (id == null || id.length() < 6) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		int code = Integer.parseInt(id.substring(0, 6));
		// 这块处理�?下，如果传过来的�?后两位是00，那么改�?99
		// if("00".equals(id.substring(4,6)))
		// {
		// code += 99;
		// }

		// 查询该code�?对应的城�?
		CodeInfo cityinfo = admin.getCityInfo(code);
		if (cityinfo == null) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		// 查询该城市的省的城市列表
		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();
		PreparedStatement stmt = admin.getStmtCities();
		synchronized (stmt) {
			try {
				stmt.setInt(1, cityinfo.pcode);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					CodeInfo info = admin.getCityInfo(rs);
					diss.add(info);
				}
			} catch (SQLException e) {
			}
		}

		// 组织返回数据
		JSONObject result = new JSONObject();

		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {

			result.put("id", new Object[] { cityinfo.code + "" });
			result.put("la", new Object[] { (int) (cityinfo.lat * 100000) });
			result.put("le", new Object[] { cityinfo.level });
			result.put("na", new Object[] { cityinfo.name });
			result.put("lo", new Object[] { (int) (cityinfo.lon * 100000) });

			JSONObject cities = new JSONObject();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);
				obj.put("provinceid", new Object[] { info.pcode + "" });
				obj.put("id", new Object[] { info.code + "" });
				obj.put("la", new Object[] { (int) (info.lat * 100000) });
				obj.put("le", new Object[] { info.level });
				obj.put("na", new Object[] { info.name });
				obj.put("lo", new Object[] { (int) (info.lon * 100000) });

				cities.put(i + "", new Object[] { obj });
			}

			result.put("cities", new Object[] { cities });

			String callback = RequestUtil.getParameter(req, "callback");
			String res = result.toString();
			if (callback != null) {
				res = callback + "&&" + callback + "(" + res + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(res.getBytes("utf-8"));
		} else {

			result.put("id", cityinfo.code);
			result.put("la", cityinfo.lat);
			result.put("le", cityinfo.level);
			result.put("na", cityinfo.name);
			result.put("lo", cityinfo.lon);

			JSONArray cities = new JSONArray();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);

				obj.put("provinceid", info.pcode);
				obj.put("id", info.code);
				obj.put("la", info.lat);
				obj.put("le", info.level);
				obj.put("na", info.name);
				obj.put("lo", info.lon);

				cities.put(obj);
			}

			result.put("cities", cities);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		}
	}

	// 返回区县列表
	void getDis(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String id = RequestUtil.getParameter(req, "id");
		if (id == null || id.length() < 6) {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

		int code = Integer.parseInt(id.substring(0, 6));
		// 查询该code�?对应的县区信�?
		CodeInfo disinfo = admin.getDisInfo(code);
		if (disinfo == null) {
			ResponseUtil.error(resp, "参数错误1");
			return;
		}
		//String uid = RequestUtil.getParameter(req, "uid");
		//System.out.println("getdis uid:" +uid + "  code:" + disinfo.ccode);
		
		
		// 查询该code�?对应的城�?
		CodeInfo cityinfo = admin.getCityInfo(disinfo.ccode);
		if (cityinfo == null) {
			ResponseUtil.error(resp, "参数错误2");
			return;
		}

		// 查询该城市的区级列表
		ArrayList<CodeInfo> diss = new ArrayList<CodeInfo>();
		PreparedStatement stmt = admin.getStmtDiss();
		synchronized (stmt) {
			try {
				stmt.setInt(1, cityinfo.code);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					CodeInfo info = admin.getDisInfo(rs);
					diss.add(info);
				}
			} catch (SQLException e) {
			}
		}

		// 组织返回数据
		JSONObject result = new JSONObject();

		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {
			result.put("cID", new Object[] { cityinfo.code + "" });
			result.put("cName", new Object[] { cityinfo.name });
			result.put("id", new Object[] { disinfo.code + "" });
			result.put("la", new Object[] { (int) (disinfo.lat * 100000) });
			result.put("le", new Object[] { disinfo.level });
			result.put("na", new Object[] { disinfo.name });
			result.put("lo", new Object[] { (int) (disinfo.lon * 100000) });

			JSONObject dis = new JSONObject();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);
				obj.put("citiesid", new Object[] { info.ccode + "" });
				obj.put("id", new Object[] { info.code + "" });
				obj.put("la", new Object[] { (int) (info.lat * 100000) });
				obj.put("le", new Object[] { info.level });
				obj.put("na", new Object[] { info.name });
				obj.put("lo", new Object[] { (int) (info.lon * 100000) });

				dis.put(i + "", new Object[] { obj });
			}

			result.put("dis", new Object[] { dis });

			String callback = RequestUtil.getParameter(req, "callback");
			String res = result.toString();
			if (callback != null) {
				res = callback + "&&" + callback + "(" + res + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(res.getBytes("utf-8"));
		} else {
			result.put("cID", cityinfo.code);
			result.put("cName", cityinfo.name);
			result.put("id", disinfo.code);
			result.put("la", disinfo.lat);
			result.put("le", disinfo.level);
			result.put("na", disinfo.name);
			result.put("lo", disinfo.lon);

			JSONArray ids = new JSONArray();
			for (int i = 0; i < diss.size(); i++) {
				JSONObject obj = new JSONObject();

				CodeInfo info = diss.get(i);

				obj.put("citiesid", info.ccode);
				obj.put("id", info.code);
				obj.put("la", info.lat);
				obj.put("le", info.level);
				obj.put("na", info.name);
				obj.put("lo", info.lon);

				ids.put(obj);
			}

			result.put("dis", ids);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		}
	}

	// 根据位置返回三级行政区划
	void getDistrict(ChinaAdmin admin,HttpServletRequest req, HttpServletResponse resp) throws IOException {
		double lon = 0;
		double lat = 0;
		try {
			String slon = RequestUtil.getParameter(req, "lng");
			String slat = RequestUtil.getParameter(req, "lat");
			if (slon == null || slon.length() == 0 || slat == null || slat.length() == 0) {
				String[] id = RequestUtil.getParameter(req, "id").split(",");

				lon = Integer.parseInt(id[0]) * 0.00001;
				lat = Integer.parseInt(id[1]) * 0.00001;
			} else {
				lon = Double.parseDouble(slon);
				lat = Double.parseDouble(slat);
			}
		} catch (Exception e) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}
		// 查询获得该位置的rgc信息 返回
		JSONObject info = RGCPostgreSql.getAdmin(lon, lat);

		if (info == null || !info.has("district")) {
			ResponseUtil.error(resp, "RGC错误");
			return;
		}
		String code = info.getString("district").substring(0, 6);

		CodeInfo cityinfo = admin.getDisInfo(Integer.parseInt(code));
		if (cityinfo == null) {
			ResponseUtil.error(resp, "cityinfo错误");
			return;
		}
 
		JSONObject dst = new JSONObject();
		dst.put("id", cityinfo.code+"");
		dst.put("province", cityinfo.pname);
		dst.put("city", cityinfo.cname);
		dst.put("dis",  cityinfo.name);

		// 如果是兼容泰�?
		String v = RequestUtil.getParameter(req, "v");
		if ("tr".equals(v)) {

			String callback = RequestUtil.getParameter(req, "callback");
			String result = dst.toString();
			if (callback == null) {
				result = "var _OLR=" + result;
			} else {
				result = callback + "&&" + callback + "(" + result + ")";
			}

			resp.setContentType("text/javascript");
			resp.getOutputStream().write(result.getBytes("utf-8"));
		} else {
			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, dst);
		}
	}

	// 返回行政区划边界
	void getBounds(ChinaAdmin admin, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String name = RequestUtil.getParameter(req, "name");

		int code = RequestUtil.getInt(req, "code", -1);
		if (code < 0)
			code = RequestUtil.getInt(req, "admincode", -1);
		
		int level = RequestUtil.getInt(req, "level", -1);
		if(level<0)
			level = RequestUtil.getInt(req, "zoom", -1);
		//级别不能超过�?大级�?
		if(level > MapRender.MaxLevel){
			level = -1;
		}
		

		Connection con = null;
		try {
			con = DBPool.getDataInstance().getConnection();

			String sql = "";
			if(level < 0){
				if (code > 0)
					sql = "select code,name,st_astext(geom) wkt from admin_wkt where code = ? limit 1";
				else
					sql = "select code,name,st_astext(geom) wkt from admin_wkt where name like ? limit 1";
			}
			else{
				if (code > 0)
					sql = "select code,name,st_astext(ST_Simplify(geom,?)) wkt from admin_wkt where code = ? limit 1";
				else
					sql = "select code,name,st_astext(ST_Simplify(geom,?)) wkt from admin_wkt where name like ? limit 1";
			}
		
			PreparedStatement stm = con.prepareStatement(sql);

			if(level < 0){
				if (code > 0)
					stm.setInt(1, code);
				else
					stm.setString(1, "%"+name+"%");
			}
			else{
				//获取level对应的精�? 该级别一个像素对应的经纬度，这个按照经度来计算即�?
				double res = MapRender.GetLevelRes(level);
				stm.setDouble(1, res);
				if (code > 0)
				{
					stm.setInt(2, code);
				}
				else
				{
					stm.setString(2, "%"+name+"%");
				}
			}

			ResultSet rs = stm.executeQuery();

			JSONObject adminobj = null;
			if (rs.next()) {

				adminobj = new JSONObject();
				adminobj.put("name", rs.getString("name"));
				adminobj.put("code", rs.getString("code"));

				adminobj.put("geo", rs.getString("wkt"));

			}

			rs.close();

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			String encoding = RequestUtil.getParameter(req, "encoding");

			JSONObject jresult = new JSONObject();
			jresult.put("admin", adminobj);
			if(adminobj == null) 
			{
				jresult.put("error", "查询失败");
			}
			ResponseUtil.writeJson2Response(resp, output, encoding, callback, jresult);

		} catch (Exception e) {

			System.out.println("getbounds错误:" + e.getMessage());
		} finally {
			if (con != null)
				DBPool.getDataInstance().closeConnection(con);
		}

		// 这里用postgresql实现

		/*
		 * synchronized (admin.stmtName) { try {
		 * 
		 * PreparedStatement stmt = null; if (code != null) { stmt =
		 * admin.stmtCode; stmt.setInt(1, Integer.parseInt(code)); } else if
		 * (name != null) { stmt = admin.stmtName; stmt.setString(1, "%" + name
		 * + "%"); } else { ResponseUtil.error(resp, "参数错误"); return; }
		 * 
		 * ResultSet rs = stmt.executeQuery(); if (rs.next()) {
		 * adminobj.put("name", rs.getString("name")); adminobj.put("code",
		 * rs.getString("code"));
		 * 
		 * // wkb ? wkt
		 * 
		 * adminobj.put("geo", rs.getString("wkt")); }
		 * 
		 * } catch (SQLException e) { ResponseUtil.error(resp, "数据库错�?"); return;
		 * }
		 */

	}

	protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

		doGet(req, response);
	}
}