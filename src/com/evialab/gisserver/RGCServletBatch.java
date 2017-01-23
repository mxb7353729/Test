package com.evialab.gisserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import com.evialab.util.DBPool;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class RGCServletBatch extends HttpServlet {

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to
	 * post.
	 * 
	 * @param request
	 *            the request send by the client to the server
	 * @param response
	 *            the response send by the server to the client
	 * @throws ServletException
	 *             if an error occurred
	 * @throws IOException
	 *             if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}

		String st = RequestUtil.getParameter(request, "st");
		String type = request.getParameter("type");
		String adr=request.getParameter("adr");
		String points = request.getParameter("points");
		String output = request.getParameter("output");
		String callback = request.getParameter("callback");
		String encoding = request.getParameter("encoding");
		String cd = request.getParameter("cd");
		if (cd != null) {
			if (cd.equalsIgnoreCase("wgs84")) {// 如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标
				points = GCJCoordTrans.tranpbatch(points);
			}
		}
		String[] buffer = points.split(";");
		JSONObject jresults = new JSONObject();
		if (buffer.length > 500)// 点数超过限制
		{
			jresults.put("Error", "点数超过500");
			ResponseUtil.writeError2Response(response, output, encoding, jresults);
			return;
		}
		if (!server.processUID(request, "EV_RGCBatch", buffer.length)) {
			ResponseUtil.error(response, output, encoding, callback, "无效UID");
			return;
		}

		ArrayList<JSONObject> results = new ArrayList<JSONObject>(buffer.length);
		for (int i = 0; i < buffer.length; i++) {
			results.add(null);
		}
		try {
			// int threadNum = 10;
			// 初始化countDown
			CountDownLatch threadSignal = new CountDownLatch(buffer.length);
			// 创建固定长度的线程池
			// Executor executor = Executors.newFixedThreadPool(threadNum);
			// 此处不可以用接口 �?要使用Executor的实现类 ExecutorService Executor未提供shutdown等方�?
			ExecutorService executor = Executors.newFixedThreadPool(4);
			for (int i = 0; i < buffer.length; i++) {
				String[] ptbuffer = buffer[i].split(",");
				double x = Double.parseDouble(ptbuffer[0]);
				double y = Double.parseDouble(ptbuffer[1]);

				Runnable task = new RgcThread(threadSignal, x, y, results, i,adr);
				// 执行
				executor.execute(task);
			}
			threadSignal.await(); // 等待�?有子线程执行�?
			// 固定线程池执行完成后 将释放掉资源 �?出主进程
			executor.shutdown();// 并不是终止线程的运行，�?�是禁止在这个Executor中添加新的任�?
		} catch (Exception e) {

		}

		// System.out.println("results:" + results.size());

		for (int i = 0; i < results.size(); i++) {
			JSONObject item = results.get(i);
			if(item == null)
				item = new JSONObject();
			jresults.append("item", item);
		}

		jresults.put("count", results.size());

	 
		ResponseUtil.writeJson2Response(response, output, encoding, callback, jresults, "true".equals(request.getParameter("zip")));

	}

	

	private class RgcThread implements Runnable {
		private CountDownLatch threadsSignal;

		private ArrayList<JSONObject> results = null;
		private double lon = 0;
		private double lat = 0;
		private int index = 0;
		private String adr=null;

		public RgcThread(CountDownLatch th, double _lon, double _lat, ArrayList<JSONObject> res, int idx,String _adr) {
			threadsSignal = th;
			results = res;
			lon = _lon;
			lat = _lat;
			index = idx;
			adr=_adr;
		}

		public void run() {
			// System.out.println(Thread.currentThread().getName());
			// System.out.println("�?始了线程：：：：" + threadsSignal.getCount());

			// 核心处理逻辑
			JSONObject jadmin = RGCPostgreSql.rgc(lon, lat,adr);

			synchronized (results) {
				// System.out.println("index:" + index + " obj:" + jadmin);
				results.set(index, jadmin);
			}

			// 线程结束时计数器�?1
			threadsSignal.countDown();

		}
	}

	public static void main(String[] args) {
		
//		try {
//			RGCAdminSearch.Init("D:\\四维\\roadindex-612.db3");
//			RGCRoadSearch.Init("D:\\四维\\roadindex-612.db3");
//			RGCPoiSearch.Init("D:\\四维\\poigrid.db3");
//			RGCPoiSearch.InitPostgis("jdbc:postgresql://127.0.0.1:5432/cttic", "postgres", "123");
//			RGCRoadSearch.InitPg("jdbc:postgresql://127.0.0.1:5432/cttic", "postgres", "123");
//
//			double x0 = 110;
//			double y0 = 40;
//
//			long start = System.currentTimeMillis();
//
//			JSONObject jresults = new JSONObject();
//			jresults.put("count", 500);
//			for (int i = 0; i < 500; i++) {
//				double x = x0 + i * 0.01;
//				double y = y0 + i * 0.01;
//
//				JSONObject jadmin = RGCAdminSearch.getRGCAdmin(x, y);
//				JSONObject jroad = RGCRoadSearch.GetNearestRoad(x, y, 1);
//				JSONObject jpoi = RGCPoiSearch.Search(x, y);
//				jadmin.put("road_address", jroad.get("road_address"));
//				jroad.remove("road_address");
//				jadmin.put("road", jroad);
//				jadmin.put("poi", jpoi);
//				jadmin.put("x", x);
//				jadmin.put("y", y);
//				jresults.append("item", jadmin);
//			}
//			// System.out.println(jresults.toString());
//			long mid = System.currentTimeMillis();
//			System.out.println("output = " + (mid - start));
//
//			java.io.FileWriter wr = new java.io.FileWriter("D:\\result.txt");
//			wr.write(jresults.toString());
//			wr.flush();
//			wr.close();
//
//			long end = System.currentTimeMillis();
//			System.out.println("time = " + (end - start));
//
//		} catch (Exception ex) {
//			System.out.println(ex.getMessage());
//		}
	}

}
