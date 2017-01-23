package com.evialab.unittest;

import java.util.Date;

import org.json.JSONObject;

import com.evialab.gisserver.RGCPostgreSql;
import com.evialab.gisserver.RGCRoadSearch;
import com.evialab.util.DBPool;

public class RGCRoadTest {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DBPool.initDataInstance("jdbc:postgresql://127.0.0.1:5432/road", "postgres", "123");
		RGCRoadSearch.Init("E:\\data\\四维测试数据\\roadindex.db3");
		 
		testRoad();
	}

	public final static int TESTCOUNT = 100;

	// 测试道路
	public static void testRoad() {

		RandomLngLat[] lnglats = RandomLngLat.CreateRandomPoints(TESTCOUNT);

		int debugprint = 33;

		int count1 = 0;
		double distance1 = 0;
		int maxdis1 = 0;
		// 测试原来的查询�?�度
		System.out.println("随机点数:" + lnglats.length);
		Date s = new Date();
		for (int i = 0; i < lnglats.length; i++) {

			JSONObject obj = RGCRoadSearch.GetNearestRoad(lnglats[i].lon, lnglats[i].lat, 1);
			// if (i == debugprint)
			System.out.println(i + "\t" + obj);
			if (!"未发现道�?".equals(obj.getString("name"))) {
				count1++;
				int dis = obj.getInt("distance");
				if (distance1 < dis) {
					maxdis1 = i;
					distance1 = dis;
				}

			}
		}
		long t1 = (new Date()).getTime() - s.getTime();

		// 测试数据库查询�?�度 ，并且每次都重新连接
		// Connection con = DBPool.getRGCInstance().getConnection();

		int count2 = 0;
		double distance2 = 0;
		int maxdis2 = 0;
		s = new Date();
		for (int i = 0; i < lnglats.length; i++) {

			Date ts = new Date();

			// JSONObject obj = RGCPostgreSql.getNearestRoad(lnglats[i].lon,
			// lnglats[i].lat);
			JSONObject obj = RGCPostgreSql.getNearestRoadWithAngle(lnglats[i].lon, lnglats[i].lat, 0);

			long t = (new Date()).getTime() - ts.getTime();

			System.out.println("t:" + t + "\tpoint:" + lnglats[i].lon + "," + lnglats[i].lat);
			System.out.println(i + "\t" + obj);
			if (obj != null) {
				count2++;
				int dis = obj.getInt("distance");
				if (distance2 < dis) {
					maxdis2 = i;
					distance2 = dis;
				}
			}

		}
		long t2 = (new Date()).getTime() - s.getTime();

		System.out.println("count:" + count1 + "\t maxdis:" + distance1 + "\tRGCAdminSearch耗时:" + t1);
		System.out.println("maxdis1:" + maxdis1 + "\t point:" + lnglats[maxdis1].lon + "," + lnglats[maxdis1].lat);

		System.out.println("count:" + count2 + "\t maxdis:" + distance2 + "\tRGCPostgreSql耗时:" + t2);
		System.out.println("maxdis2:" + maxdis2 + "\t point:" + lnglats[maxdis2].lon + "," + lnglats[maxdis2].lat);

	}
 
}
