package com.evialab.gisserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.evialab.util.DBPool;

public class ChinaAdmin {

	
	public GCCach gcCach = null;
	private Connection connection = null;

 
	private PreparedStatement stmtName = null;
	private PreparedStatement stmtCode = null;
	private PreparedStatement stmtDis = null;
	private PreparedStatement stmtCity = null;
	private PreparedStatement stmtProvince = null;

	private PreparedStatement stmtDiss = null;
	private PreparedStatement stmtCities = null;
	private PreparedStatement stmtProvinces = null;

	 
	private void testConnection(){
		try {
			if(connection == null || connection.isClosed()){
				init();
			}
		} catch (SQLException e) {
			System.out.println("ChinaAdmin错误:" + e.getLocalizedMessage());
		}
	}
	public PreparedStatement getStmtName() {
	 
		testConnection();
		return stmtName;
	}
 
	public PreparedStatement getStmtCode() {
		testConnection();
		return stmtCode;
	}
 
	public PreparedStatement getStmtDis() {
		testConnection();
		return stmtDis;
	}
 
	public PreparedStatement getStmtCity() {
		testConnection();
		return stmtCity;
	}
 

	public PreparedStatement getStmtProvince() {
		testConnection();
		return stmtProvince;
	}
 
	public PreparedStatement getStmtDiss() {
		testConnection();
		return stmtDiss;
	}
 
	public PreparedStatement getStmtCities() {
		testConnection();
		return stmtCities;
	}
 

	public PreparedStatement getStmtProvinces() {
		testConnection();
		return stmtProvinces;
	}
 
	public boolean init() {
		 

		try {
			//Class.forName("org.sqlite.JDBC");

			//String connStr = "jdbc:sqlite:" + path;
			connection = DBPool.getDataInstance().getConnection();

			stmtCode = connection
					.prepareStatement("select * from admin_wkt where code =  ? limit 1");
			stmtName = connection
					.prepareStatement("select * from admin_wkt where name like ? limit 1");

			stmtDis = connection
					.prepareStatement("select * from admin_dis where dcode =  ? limit 1");
			stmtCity = connection
					.prepareStatement("select * from admin_city where ccode =  ? limit 1");
			stmtProvince = connection
					.prepareStatement("select * from admin_province where pcode =  ? limit 1");

			stmtDiss = connection
					.prepareStatement("select * from admin_dis where ccode =  ?");
			stmtCities = connection
					.prepareStatement("select * from admin_city where pcode =  ? ");
			stmtProvinces = connection
					.prepareStatement("select * from admin_province");

			gcCach = new GCCach(connection);

			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

 

	public void destory() {
		try {
			if (stmtName != null)
				stmtName.close();

			if (stmtCode != null)
				stmtCode.close();
			
			
			if (stmtDis != null)
				stmtDis.close();

			if (stmtCity != null)
				stmtCity.close();
			
			if (stmtProvince != null)
				stmtProvince.close();

			if (stmtDiss != null)
				stmtDiss.close();
			
			if (stmtCities != null)
				stmtCities.close();

			if (stmtProvinces != null)
				stmtProvinces.close();
			
			if(connection != null)
				 DBPool.getDataInstance().closeConnection(connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public CodeInfo getProvinceInfo(ResultSet rs) throws SQLException
	{
		CodeInfo info = new CodeInfo();
		info.code = rs.getInt("pcode");
		info.ccode = rs.getInt("pcode");
		info.pcode = rs.getInt("pcode");

		info.pname = info.name = rs.getString("pname");
		info.lon = rs.getDouble("lon");
		info.lat = rs.getDouble("lat");
		info.level = Math.max(rs.getInt("wlevel"),
				rs.getInt("hlevel")) + 1;

		return info;
	}
	// 根据编码返回省级信息
	public CodeInfo getProvinceInfo( int code) {
		synchronized (stmtProvince) {
			try {
				stmtProvince.setInt(1, code);
				ResultSet rs = stmtProvince.executeQuery();
				if (rs.next()) {
					return getProvinceInfo(rs);
				}
			} catch (SQLException e) {
			}
			return null;
		}
	}

	public CodeInfo getDisInfo(ResultSet rs) throws SQLException
	{
		CodeInfo info = new CodeInfo();
		info.code = rs.getInt("dcode");
		info.ccode = rs.getInt("ccode");
		info.pcode = rs.getInt("pcode");

		info.name = rs.getString("dname");
		info.pname = rs.getString("pname");
		info.cname = rs.getString("cname");
		info.lon = rs.getDouble("lon");
		info.lat = rs.getDouble("lat");
		info.level = Math.max(rs.getInt("wlevel"),
				rs.getInt("hlevel")) + 1;

		return info;
	}
	// 根据编码返回区级信息
	public CodeInfo getDisInfo(int code) {
		testConnection();
		synchronized (stmtDis) {
			try {
				stmtDis.setInt(1, code);
				ResultSet rs =stmtDis.executeQuery();
				System.out.println("根据编码返回区级信息");
				if (rs.next()) {
					return getDisInfo(rs);
				}
			} catch (SQLException e) {
				System.out.println("getDisInfo错误:" + e.getLocalizedMessage());
			}
			return null;
		}
	}

	public CodeInfo getCityInfo(ResultSet rs) throws SQLException
	{
		CodeInfo info = new CodeInfo();
		info.code = rs.getInt("ccode");
		info.ccode = rs.getInt("ccode");
		info.pcode = rs.getInt("pcode");

		info.cname = info.name = rs.getString("cname");
		info.pname = rs.getString("pname");
		info.lon = rs.getDouble("lon");
		info.lat = rs.getDouble("lat");
		info.level = Math.max(rs.getInt("wlevel"),
				rs.getInt("hlevel")) + 1;

		return info;
	}
	
	// 根据编码返回市级信息
	public CodeInfo getCityInfo(int code) {
		synchronized (stmtCity) {
			try {
				stmtCity.setInt(1, code);
				ResultSet rs = stmtCity.executeQuery();
				if (rs.next()) {
					return getCityInfo(rs);
				}
			} catch (SQLException e) {
			}
			return null;
		}
	}

	public static class CodeInfo {
		public int code;
		public int ccode;
		public int pcode;
		public String name;
		public String cname;
		public String pname;
		public double lon;
		public double lat;
		public int level;

	}
}
