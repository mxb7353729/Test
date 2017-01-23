package com.evialab.unittest;

import java.sql.Connection;
import java.util.Date;

import org.json.JSONObject;

import com.evialab.gisserver.RGCAdminSearch;
import com.evialab.gisserver.RGCPostgreSql;
import com.evialab.gisserver.RGCRoadSearch;
import com.evialab.util.DBPool;

public class RGCAdminTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBPool.initDataInstance("jdbc:postgresql://127.0.0.1:5432/road", "postgres", "123");
		RGCAdminSearch.Init("E:\\data\\四维测试数据\\roadindex.db3");
	 
		testAdmin();
	}

	public final static int TESTCOUNT = 10000;

	// 测试行政区划查询
	public static void testAdmin() {

		

		RandomLngLat[] lnglats = RandomLngLat.CreateRandomPoints(TESTCOUNT);

		int debugprint = 33;

		// 测试原来的查询�?�度
		System.out.println("随机点数:" + lnglats.length);
		Date s = new Date();
		for (int i = 0; i < lnglats.length; i++) {
			JSONObject obj = RGCAdminSearch.getRGCAdmin(lnglats[i].lon, lnglats[i].lat);
			if (i == debugprint)
				System.out.println(obj);
		}
		long t1 = (new Date()).getTime() - s.getTime();

		// 测试数据库查询�?�度 ，并且每次都重新连接
		s = new Date();
		for (int i = 0; i < lnglats.length; i++) {
			JSONObject obj = RGCPostgreSql.getAdmin(lnglats[i].lon, lnglats[i].lat);
			if (i == debugprint)
				System.out.println(obj);
		}
		long t2 = (new Date()).getTime() - s.getTime();

		// 测试数据库查询�?�度 �? 不需要重新连�?
		Connection con = DBPool.getDataInstance().getConnection();
		s = new Date();
		for (int i = 0; i < lnglats.length; i++) {
			JSONObject obj = RGCPostgreSql.getAdmin(con, lnglats[i].lon, lnglats[i].lat);
			if (i == debugprint)
				System.out.println(obj);
		}
		DBPool.getDataInstance().closeConnection(con);
		long t3 = (new Date()).getTime() - s.getTime();

		System.out.println("RGCAdminSearch耗时:" + t1);

		System.out.println("RGCPostgreSql耗时:" + t2);

		System.out.println("RGCPostgreSql耗时(不重�?):" + t3);

	}

	
}
