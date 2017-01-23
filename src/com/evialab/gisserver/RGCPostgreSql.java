package com.evialab.gisserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.zip.InflaterInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.evialab.util.DBPool;

public class RGCPostgreSql {

	static String getPosMeshid(double lon, double lat) {
		// String a = meshid.substring(0, 2);
		// String b = meshid.substring(2, 4);
		// String c = meshid.substring(4, 5);
		// String d = meshid.substring(5);

		// p.lon = Double.parseDouble(b) + 60.0 + Double.parseDouble(d) / 8.0;

		// p.lat = (Double.parseDouble(a) + Double.parseDouble(c) / 8.0 ) * 2 /
		// 3.0;
		lat = lat * 1.5; // lat * 3 / 2

		int a = (int) Math.floor(lat);
		int c = (int) Math.floor((lat - a) * 8);

		lon = lon - 60;
		int b = (int) Math.floor(lon);
		int d = (int) Math.floor((lon - b) * 8);

		return a + "" + b + "" + c + "" + d;
	}

	// 查询行政区划
	public static JSONObject getAdmin(double lon, double lat) {

		return getAdmin(null, lon, lat);
	}

	public static JSONObject getAdmin(Connection con, double lon, double lat) {

		JSONObject admin = getAdminMeshid(con, lon, lat);
		// 发现某些特殊点上，meshid不存�? 那么此时不计算meshid，直接从数据库里查询
		if (admin == null)
			admin = getAdminNoMeshid(con, lon, lat);

		if (admin == null)
			admin = getAdminNearest(con, lon, lat);

		if (admin == null) {
			admin = new JSONObject();
			admin.put("district", "00000000");
			admin.put("district_text", "行政区划未知，请联系�?术支�?");
		}

		return admin;
	}

	static JSONObject getAdminNoMeshid(Connection con, double lon, double lat) {
		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			String sql = "select admincode||'000'  , province||'>'||city||'>'||name  from admin_rgc where st_contains(geom,st_makepoint(?,?))   limit 1";
			PreparedStatement stm = con.prepareStatement(sql);

			stm.setObject(1, lon);
			stm.setObject(2, lat);

			JSONObject o = null;
			ResultSet rs = stm.executeQuery();
			if (rs.next()) {

				o = new JSONObject();
				o.put("district", rs.getObject(1));
				o.put("district_text", rs.getObject(2));

			}

			rs.close();

			return o;

		} catch (Exception e) {

			System.out.println("getAdmin错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	static JSONObject getAdminNearest(Connection con, double lon, double lat) {
		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			String sql = "select admincode||'000'  , province||'>'||city||'>'||name  from admin_rgc where st_dwithin(geom,st_makepoint(?,?), 0.00002)   limit 1";
			PreparedStatement stm = con.prepareStatement(sql);

			stm.setObject(1, lon);
			stm.setObject(2, lat);

			JSONObject o = null;
			ResultSet rs = stm.executeQuery();
			if (rs.next()) {

				o = new JSONObject();
				o.put("district", rs.getObject(1));
				o.put("district_text", rs.getObject(2));

			}

			rs.close();

			return o;

		} catch (Exception e) {

			System.out.println("getAdmin错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	static JSONObject getAdminMeshid(Connection con, double lon, double lat) {
		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			String sql = "select admincode||'000'  , province||'>'||city||'>'||name  from admin_rgc where st_contains(geom,st_makepoint(?,?))  limit 1";
			PreparedStatement stm = con.prepareStatement(sql);

			//String meshid = getPosMeshid(lon, lat);

			stm.setObject(1, lon);
			stm.setObject(2, lat);
			//stm.setObject(3, meshid);

			JSONObject o = null;
			ResultSet rs = stm.executeQuery();
			if (rs.next()) {

				o = new JSONObject();
				o.put("district", rs.getObject(1));
				o.put("district_text", rs.getObject(2));

			}

			rs.close();

			return o;

		} catch (Exception e) {

			System.out.println("getAdmin错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	// 查询�?近poi
	public static JSONObject getNearestPOI(double lon, double lat,String adr) {

		return getNearestPOI(null, lon, lat,adr);
	}

	public static JSONObject getNearestPOI(Connection con, double lon, double lat,String adr) {
		double boxwidth = 0.002;
		while (boxwidth < 10) {
			
			JSONObject obj = getNearestPOI(con, lon, lat, boxwidth,adr);
			if (obj != null)
				return obj;
			boxwidth *= 2;
		}
		return null;
	}
	static String getNearestPOISql(String adr) {
		String resql="v_poi";
		if(adr==null)
			adr = "";
		if (adr.equals("hamlet"))
			resql= "v_poi_hamlet";
		else if (adr.equals("address")) 
			resql= "v_poi_address";
		//return "select ST_Distance_Sphere(geom,p) dis ,degrees(ST_Azimuth(geom,p) ) azimuth, * from " + " (select ST_Point(?,?) p,? as os , * from "+resql+") t0  " + " where ST_Contains(st_makebox2d(ST_Point(st_x(p)-os,st_y(p)-os),ST_Point(st_x(p)+os,st_y(p)+os)), geom) order by dis limit 1";
		return "select ST_Distance_Sphere(geom,p) dis ,degrees(ST_Azimuth(geom,p) ) azimuth, * from  (select st_setsrid(ST_Point(?,?),4326) p,? as os , * from "+resql+") t0   where ST_Contains(st_setsrid(st_makebox2d(ST_Point(st_x(p)-os,st_y(p)-os),ST_Point(st_x(p)+os,st_y(p)+os)),4326), geom) order by dis limit 1";
	}

	public static JSONObject getNearestPOI(Connection con, double lon, double lat, double delta,String adr) {

		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}
			String sql = getNearestPOISql(adr);
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setDouble(1, lon);
			stmt.setDouble(2, lat);
			stmt.setDouble(3, delta);

			ResultSet rs = stmt.executeQuery();

			JSONObject jo = null;
			if (rs.next()) {
				jo = new JSONObject();

				String name = rs.getString("Name");
				int dis = (int) rs.getDouble("dis");
				String address = rs.getString("Address");
				double azimuth = rs.getDouble("azimuth");
				String dir = getDir(azimuth);
				jo.put("address", name + dir + dis + "�?");
				jo.put("dir", dir);
				jo.put("name", name);
				jo.put("number", address);
				jo.put("lng", rs.getDouble("X"));
				jo.put("lat", rs.getDouble("Y"));
				jo.put("distance", dis);
			}
			rs.close();
			stmt.close();
			return jo;

		} catch (Exception e) {

			System.out.println("getNearestPOI错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	// 查询地址
	public static JSONObject getAddress(double lon, double lat, double range) {

		return getAddress(null, lon, lat, range);
	}

	public static JSONObject getAddress(Connection con, double x, double y, double delta) {

		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			String sql = "SELECT * ,st_distance(geom,ST_Point(?,?)) as dis   from poilist_geom where ST_Contains(st_makebox2d(ST_Point(?,?),ST_Point(?,?)), geom) order by dis limit 100";
			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setDouble(1, x);
			stmt.setDouble(2, y);
			stmt.setDouble(3, x - delta);
			stmt.setDouble(4, y - delta);
			stmt.setDouble(5, x + delta);
			stmt.setDouble(6, y + delta);

			ResultSet rs = stmt.executeQuery();
			JSONObject jo = new JSONObject();
			jo.put("address", "");
			jo.put("name", "");
			jo.put("number", "");
			jo.put("lng", -9999);
			jo.put("lat", -9999);
			POIInfo poi = new POIInfo();
			if (rs.next()) {
				poi.x0 = x;
				poi.y0 = y;
				poi.x = rs.getDouble("X");
				poi.y = rs.getDouble("Y");
				poi.name = rs.getString("Name");
				poi.address = rs.getString("Address");
			}
			if (poi.address == null) {
				while (rs.next()) {
					if (rs.getString("Address") != null) {
						poi.x0 = x;
						poi.y0 = y;
						poi.x = rs.getDouble("X");
						poi.y = rs.getDouble("Y");
						poi.name = rs.getString("Name");
						poi.address = rs.getString("Address");
						break;
					}
				}
			}

			if (poi.name != "") {
				poi.CreateDistAngle();
				jo.put("address", poi.name + poi.dir + Integer.toString((int) poi.dist) + "�?");
				jo.put("name", poi.name);
				jo.put("number", poi.address);
				if (poi.address == null)
					jo.put("number", "");
				jo.put("lng", poi.x);
				jo.put("lat", poi.y);
				rs.close();
				return jo;
			} else {
				jo.put("address", "未发现poi");
				jo.put("name", "无POI");
				jo.put("number", "");
				jo.put("lng", -9999);
				jo.put("lat", -9999);
				return jo;
			}

		} catch (Exception e) {

			System.out.println("getAddress错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	// 查询�?近道�? 不�?�虑方向
	public static JSONObject getNearestRoad(double lon, double lat) {

		return getNearestRoad(null, lon, lat);
	}

	public static JSONObject getNearestRoad(Connection con, double lon, double lat) {

		double boxwidth = 0.0001;
		while (boxwidth < 1) {
			JSONObject obj = getNearestRoad(con, lon, lat, boxwidth);
			if (obj != null)
				return obj;
			boxwidth *= 2;
		}
		return null;
	}

	static String getNearestRoadSql() {
		return "select   distance, st_x(cp) x, st_y(cp) y, degrees(ST_Azimuth(cp,p) ) azimuth ,* from " + " (select  ST_ClosestPoint(geom,p) cp,* from " + " (select  ST_Distance_Sphere(geom,p) distance, * from  " + " (select st_makepoint(?,?) p,? os, * from r ) s0  " + " where st_intersects(st_makebox2d(st_makepoint(st_x(p)-os,st_y(p)-os),st_makepoint(st_x(p)+os,st_y(p)+os)), geom) " + " order by distance  limit ?) s1) s2";
	}

	static JSONObject getNearestRoad(Connection con, double lon, double lat, double boxratio) {

		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			// 1, 直接取距离最近的道路
			String sql = getNearestRoadSql();

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setDouble(1, lon);
			stmt.setDouble(2, lat);
			stmt.setDouble(3, boxratio);
			stmt.setDouble(4, 1);

			ResultSet rs = stmt.executeQuery();

			JSONObject jo = null;
			if (rs.next()) {
				jo = roadInfo(rs);
			}

			rs.close();
			stmt.close();

			return jo;

		} catch (Exception e) {

			System.out.println("getNearestRoad错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	// 查询�?近道�? 不�?�虑方向
	public static JSONObject getNearestRoadWithAngle(double lon, double lat, double angle) {

		return getNearestRoadWithAngle(null, lon, lat, angle);
	}

	public static JSONObject getNearestRoadWithAngle(Connection con, double lon, double lat, double angle) {
		double boxwidth = 0.0002;
		while (boxwidth < 1) {
			JSONObject obj = getNearestRoadWithAngle(con, lon, lat, angle, boxwidth);
			if (obj != null)
				return obj;
			boxwidth *= 2;
		}
		return null;
	}

	// 考虑角度参数的最小距离范�?
	final static double AngleDistanceThresh = 20;

	static JSONObject getNearestRoadWithAngle(Connection con, double lon, double lat, double angle, double boxratio) {

		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			// 1, 首先查询距离�?近的4条道�?
			String sql = getNearestRoadSql();

			PreparedStatement stmt = con.prepareStatement(sql);

			stmt.setDouble(1, lon);
			stmt.setDouble(2, lat);
			stmt.setDouble(3, boxratio);
			stmt.setDouble(4, 4);

			ResultSet rs = stmt.executeQuery();

			ArrayList<JSONObject> jos = new ArrayList<JSONObject>();
			ArrayList<String> ids = new ArrayList<String>();
			double mindis = 0;
			while (rs.next()) {
				JSONObject j = roadInfo(rs);
				double dis = rs.getDouble("distance");
				if (jos.isEmpty()) {
					jos.add(j);
					ids.add(rs.getString("id"));
					mindis = dis;
				}
				// 这个20是距离阈�? 超过20米的距离，不考虑方向
				else if (dis - mindis <= AngleDistanceThresh) {
					jos.add(j);
					ids.add(rs.getString("id"));
				} else
					break;
			}

			rs.close();
			// stmt.close();

			if (jos.size() == 0)
				return null;
			else if (jos.size() == 1)
				return jos.get(0);

			// 有多条道路，那么�?要该点在各条道路上的方向
			sql = "select st_point_lineangle(" + lon + "," + lat + ",geom) angle,id from r ";
			for (int i = 0; i < ids.size(); i++) {
				if (i == 0)
					sql += " where ";
				else
					sql += " or ";

				sql += " id = '" + ids.get(i) + "' ";
			}
			PreparedStatement stmt2 = con.prepareStatement(sql);

			rs = stmt2.executeQuery();
			double minanglesub = 0;
			JSONObject obj = null;
			// 在上面已经筛选了距离，这里不在�?�虑距离阈�??
			while (rs.next()) {
				String id = rs.getString("id");
				int idx = ids.indexOf(id);

				double anglesub = Math.abs(rs.getDouble("angle") - angle);
				if (obj == null || anglesub < minanglesub) {
					obj = jos.get(idx);
					minanglesub = anglesub;
				}
			}
			rs.close();
			stmt2.close();

			return obj;

		} catch (Exception e) {

			System.out.println("getNearestRoadWithAngle错误:" + e.getMessage());
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
		return null;
	}

	// 从数据库提取道路信息
	public static JSONObject roadInfo(ResultSet rs) throws JSONException, SQLException {

		JSONObject jo = new JSONObject();

		DecimalFormat df = new DecimalFormat("#.00000");

		String name = rs.getString("pathname");
		if (name == null)
			name = "";

		jo.put("name", name);
		jo.put("road_level", getMainType(rs.getString("kind")));

		double spdlmts2s = 0;
		double spdlmts2e = 0;
		try {
			spdlmts2s = Double.parseDouble(rs.getString("spdlmte2s")) * 0.1;
		} catch (Exception e) {

		}
		try {
			spdlmts2e = Double.parseDouble(rs.getString("spdlmts2e")) * 0.1;
		} catch (Exception e) {

		}
		double limit = 0;
		if (spdlmts2e > 0 && spdlmts2s > 0) {
			limit = Math.min(spdlmts2e, spdlmts2s);
		} else if (spdlmts2e > 0)
			limit = spdlmts2e;
		else if (spdlmts2s > 0)
			limit = spdlmts2s;

		jo.put("limit_speed", new DecimalFormat("#.00000").format(limit));
		jo.put("lng", df.format(rs.getDouble("x")));
		jo.put("lat", df.format(rs.getDouble("y")));

		/*
		 * if(bangle) { Object angle1 = rs.getObject("angle1"); Object angle2 =
		 * rs.getObject("angle2");
		 * 
		 * if(angle1 != null) { jo.put("angle",
		 * Double.parseDouble(angle1.toString())); } else if(angle2 != null) {
		 * jo.put("angle", Double.parseDouble(angle2.toString())); } }
		 */

		double azimuth = rs.getDouble("azimuth");
		// jo.put("azimuth", azimuth);

		int dis = (int) rs.getDouble("distance");
		jo.put("distance", dis);
		jo.put("road_address", name + getDir(azimuth) + dis + "�?");

		jo.put("urban", rs.getString("uflag"));
		jo.put("width", GetRoadWidth(rs.getString("width")));

		jo.put("lanenumber", laneNumber(rs.getInt("lanenum")));

		jo.put("roadtype", getSubType(rs.getString("kind")));
		jo.put("linkid", rs.getString("id"));
		return jo;
	}

	static String getDir(double angle) {
		String dir = "�?";
		if ((angle > 20) & (angle < 70)) {
			dir = "东北";
		} else if ((angle >= 70) && (angle <= 110)) {
			dir = "�?";
		} else if ((angle > 110) && (angle < 160)) {
			dir = "东南";
		} else if ((angle >= 160) && (angle <= 200)) {
			dir = "�?";
		} else if ((angle > 200) && (angle < 250)) {
			dir = "西南";
		} else if ((angle >= 250) && (angle <= 290)) {
			dir = "�?";
		} else if ((angle > 290) && (angle < 340)) {
			dir = "西北";
		}
		return dir;
	}

	static String laneNumber(int ll) {
		if (ll == 1)
			return "�?条车�?";
		else if (ll == 2)
			return "两条或三条车�?";
		else if (ll == 3)
			return "多于四条车道";
		return "未知";
	}

	static String GetRoadLevel(String kind) {
		if (kind.compareTo("00") == 0)
			return "高�?�路";
		if (kind.compareTo("01") == 0)
			return "都市高�?�路";
		if (kind.compareTo("02") == 0)
			return "国道";
		if (kind.compareTo("03") == 0)
			return "省道";
		if (kind.compareTo("04") == 0)
			return "县道";
		if (kind.compareTo("06") == 0)
			return "乡镇村道";
		if (kind.compareTo("08") == 0)
			return "其他道路";
		if (kind.compareTo("09") == 0)
			return "九级�?";
		if (kind.compareTo("0b") == 0)
			return "行人道路";
		return "其他道路";
	}

	static HashMap<String, String> MainKinds = new HashMap<String, String>();
	static HashMap<String, String> SubKinds = new HashMap<String, String>();

	static {
		// 初始化映射关�?
		MainKinds.put("00", "高�?�路");
		MainKinds.put("01", "都市高�?�路");
		MainKinds.put("02", "国道");
		MainKinds.put("03", "省道");
		MainKinds.put("04", "县道");
		// MainKinds.put("05", "高�?�路");
		MainKinds.put("06", "乡镇村道");
		// MainKinds.put("07", "高�?�路");
		MainKinds.put("08", "其他道路");
		MainKinds.put("09", "九级�?");
		MainKinds.put("0a", "轮渡");
		MainKinds.put("0b", "行人道路");

		SubKinds.put("00", "环岛");
		SubKinds.put("01", "无属�?");
		SubKinds.put("02", "上下线分�?");
		SubKinds.put("03", "JCT");
		SubKinds.put("04", "交叉点内Link");
		SubKinds.put("05", "IC");
		SubKinds.put("06", "停车�?");
		SubKinds.put("07", "服务�?");
		SubKinds.put("08", "�?");
		SubKinds.put("09", "步行�?");
		SubKinds.put("0a", "辅路");
		SubKinds.put("0b", "匝道");
		SubKinds.put("0c", "全封闭道�?");
		SubKinds.put("0d", "未定义交通区�?");
		SubKinds.put("0e", "POI连接�?");
		SubKinds.put("0f", "隧道");
		SubKinds.put("11", "公交专用�?");
		SubKinds.put("12", "提前右转");
		SubKinds.put("13", "风景路线");
		SubKinds.put("14", "区域内道�?");
		SubKinds.put("15", "提前左转");
		SubKinds.put("16", "调头�?");
		SubKinds.put("17", "主辅路出入口");
		SubKinds.put("18", "停车位引导路");
		SubKinds.put("19", "虚拟链接�?");
	}

	static String getMainType(String kind) {
		String maink = kind.substring(0, 2);

		String s = MainKinds.get(maink);
		if (s == null)
			return "其他道路";
		return s;
	}

	static String getSubType(String kind) {
		String maink = kind.substring(2, 4);

		String s = SubKinds.get(maink);
		if (s == null)
			return "其他";
		return s;
	}

	static String GetRoadWidth(String w) {
		if (w.compareTo("15") == 0)
			return "<=3�?";
		if (w.compareTo("30") == 0)
			return "(3米~5.5米]";
		if (w.compareTo("55") == 0)
			return "(5.5米~13米]";
		if (w.compareTo("130") == 0)
			return ">13�?";
		return "宽度未知";
	}

	public static JSONObject rgc(double lon, double lat,String adr) {

		return rgc(null, lon, lat,adr);

	}

	public static JSONObject rgc(Connection con, double lon, double lat,String adr) {
		boolean innercon = false;
		try {
			if (con == null) {
				con = DBPool.getDataInstance().getConnection();
				innercon = true;
			}

			JSONObject jadmin = getAdmin(con, lon, lat);

			JSONObject jroad = getNearestRoad(con, lon, lat);

			JSONObject jpoi = getNearestPOI(con, lon, lat,adr);

			if (jpoi != null) {
				jadmin.put("address", jpoi.remove("address"));

			}
			if (jroad != null) {
				jadmin.put("road_address", jroad.remove("road_address"));
			}
			jadmin.put("point", jpoi);
			jadmin.put("road", jroad);

			return jadmin;
		} catch (Exception e) {
			return null;
		} finally {
			if (con != null && innercon)
				DBPool.getDataInstance().closeConnection(con);
		}
	}

	public static JSONObject rgcWithAngle(double lon, double lat, int angle,String adr) {
		Connection con = null;
		try {
			JSONObject jadmin = getAdmin(lon, lat);

			JSONObject jroad = getNearestRoadWithAngle(lon, lat, angle);

			JSONObject jpoi = getNearestPOI(lon, lat,adr);

			if (jpoi != null) {
				jadmin.put("address", jpoi.remove("address"));

			}
			if (jroad != null) {
				jadmin.put("road_address", jroad.remove("road_address"));
			}

			jadmin.put("point", jpoi);
			jadmin.put("road", jroad);

			return jadmin;
		} catch (Exception e) {
			return null;
		} finally {
			if (con != null)
				DBPool.getDataInstance().closeConnection(con);
		}
	}

	public static JSONObject rgcWithoutRoad(double lon, double lat,String adr) {
		Connection con = null;
		try {
			JSONObject jadmin = getAdmin(lon, lat);

			JSONObject jpoi = getNearestPOI(lon, lat,adr);

			if (jpoi != null) {
				jadmin.put("address", jpoi.remove("address"));

			}
			jadmin.put("point", jpoi);

			return jadmin;
		} catch (Exception e) {
			return null;
		} finally {
			if (con != null)
				DBPool.getDataInstance().closeConnection(con);
		}
	}

}
