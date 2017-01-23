package com.evialab.util;

 


import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBPool {
	private static DBPool data; //rgc相关查询，包括ip，以后甚至包含admin�? 数据服务�?
	private static DBPool service; // 服务支撑库，比如用户数据库，以及用户访问记录数据�?
	private ComboPooledDataSource ds;

	
	//初始�? �? 获取 数据服务器的连接
	public static void initDataInstance(String conn, String user, String pass){
		data = new DBPool(conn,user,pass);
	}
	public final static DBPool getDataInstance() {
		return data;
	}
 
	//初始化和获取支撑服务器的连接
	public static void initServiceInstance(String conn, String user, String pass){
		service = new DBPool(conn,user,pass,"ctticmap");
	}
	public final static DBPool getServiceInstance() {
		return service;
	}
	
	private DBPool(String conn, String user, String pass) {
		try {
			ds = new ComboPooledDataSource();
			ds.setDriverClass("org.postgresql.Driver");
			ds.setJdbcUrl(conn);
			ds.setUser(user);
			ds.setPassword(pass);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}  
	}
	private DBPool(String conn, String user, String pass,String config) {
		try {
			ds = new ComboPooledDataSource(config);
			ds.setDriverClass("org.postgresql.Driver");
			ds.setJdbcUrl(conn);
			ds.setUser(user);
			ds.setPassword(pass);
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}  
	}
	


	public final Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
		
			 
			System.out.println("获取连接出错" + ":" + e.getLocalizedMessage());
			
			return null;
		}
	}
	public final boolean closeConnection(Connection con)
	{
		try {
			if(con != null)
				con.close();
			return true;
		} catch (SQLException e) {
 
			System.out.println("关闭连接出错" + ":" + e.getLocalizedMessage());
			return false;
		}
	}
	

	public static void main(String[] args) throws SQLException {
		 
		 
	}
}
