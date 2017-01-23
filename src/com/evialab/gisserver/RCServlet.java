package com.evialab.gisserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.RCLogUtil;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;
import com.jetmap.route.RouteApi;

public class RCServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
		for(int i = 0 ; i < 100; i++)
		{
			System.out.println(transition(i));
		}
		
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}
		if (!server.processUID(req, "SE_RC")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}

		String st = RequestUtil.getParameter(req, "st");

		if ("RC".equalsIgnoreCase(st)) {
			RCquery(req, resp);
			return;
		} else if ("Nearest".equalsIgnoreCase(st)) {

			String point = RequestUtil.getParameter(req, "point");
			String coordinate=RequestUtil.getParameter(req,"cd");//获取坐标类型
			if(coordinate!=null){
			if(coordinate.equalsIgnoreCase("wgs84")){//如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标
				point=GCJCoordTrans.tranpoint(point);
			}
			}
			@SuppressWarnings("unused")
			String course = RequestUtil.getParameter(req, "course");

			try {
				String[] aaa = point.split(",");
				double lon = Double.parseDouble(aaa[0]);
				double lat = Double.parseDouble(aaa[1]);

				String output = RequestUtil.getParameter(req, "output");
				String callback = RequestUtil.getParameter(req, "callback");

				JSONObject jresult = new JSONObject();

				//JSONObject jo = RGCRoadSearch.GetNearestRoad(lon, lat, 1);
				JSONObject jo = RGCPostgreSql.getNearestRoad(lon, lat);

				JSONObject j2 = new JSONObject();
				j2.put("roadname", jo.get("name"));
				j2.put("lng", jo.get("lng"));
				j2.put("lat", jo.get("lat"));
				// j2.put("length", 0);
				j2.put("roadtype", jo.get("roadtype"));
				j2.put("roadlevel", jo.get("road_level"));
				j2.put("lanewidth", jo.get("width"));
				j2.put("roadlimit", jo.get("limit_speed"));
				j2.put("lanenumber", jo.get("lanenumber"));
				j2.put("distance", jo.get("distance"));

				jresult.put("route", j2);

				ResponseUtil.writeJson2Response(resp, output, "utf-8",
						callback, jresult);

			} catch (Exception e) {
				ResponseUtil.error(resp, "错误");
			}

			return;
		} else {
			ResponseUtil.error(resp, "参数错误");
			return;
		}

	}

	String mul10000(String src) {
		double d = Double.parseDouble(src.trim());

		return String.format("%.0f", d * 100000);
	}

	int imul10000(String src) {
		double d = Double.parseDouble(src.trim());
		return (int) (d * 100000);
	}

	String getLimitSpeed(int l) {
		/*
		 * 1 >130 km/h 2 (100 km/h, 130 km/h] 3 (90 km/h, 100 km/h] 4 (70 km/h,
		 * 90 km/h] 5 (50 km/h, 70 km/h] 6 (30 km/h, 50 km/h] 7 [11 km/h, 30
		 * km/h] 8 <11 km/h
		 */
		if (l <= 1)
			return ">130km/h";
		else if (l == 2)
			return "100km/h - 130km/h";
		else if (l == 3)
			return "90km/h - 100km/h";
		else if (l == 4)
			return "70km/h - 90km/h";
		else if (l == 5)
			return "50km/h - 70km/h";
		else if (l == 6)
			return "30km/h - 50km/h";
		else if (l == 7)
			return "11km/h - 30km/h";
		else if (l == 8)
			return "<11km/h";

		return "";
	}

	public static String transition(int v) {
		if(v==0)
			return "�?";
		String si = v+"";
		String[] aa = { "", "�?", "�?", "�?", "�?", "十万", "百万", "千万", "�?", "十亿" };
		String[] bb = { "�?", "�?", "�?", "�?", "�?", "�?", "�?", "�?", "�?" };
		char[] ch = si.toCharArray();
		int maxindex = ch.length;
		String res = "";
		
		// 字符的转�?
		// 两位数的特殊转换
		if (maxindex == 2) {
			for (int i = maxindex - 1, j = 0; i >= 0; i--, j++) {
				if (ch[j] != 48) {
					if (j == 0 && ch[j] == 49) {
						res +=aa[i];
					} else {
						res += (bb[ch[j] - 49] + aa[i]);
					}
				}
			}
			// 其他位数的特殊转换，使用的是int类型�?大的位数为十�?
		} else {
			for (int i = maxindex - 1, j = 0; i >= 0; i--, j++) {
				if (ch[j] != 48) {
					res += (bb[ch[j] - 49] + aa[i]);
				}
			}
		}
		return res;
	}

	void RCquery(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		// 根据参数构�?�URL
		String start = RequestUtil.getParameter(req, "start");
		String end = RequestUtil.getParameter(req, "end");	
		String waypoint = RequestUtil.getParameter(req, "waypoint");
		String coordinate=RequestUtil.getParameter(req,"cd");//获取坐标类型
		if(coordinate!=null){
		if(coordinate.equalsIgnoreCase("wgs84")){//如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标
			start=GCJCoordTrans.tranpoint(start);
			end=GCJCoordTrans.tranpoint(end);
			if(waypoint!=null){
			waypoint=GCJCoordTrans.tranpbatch(waypoint);
			}
		}
		}
		String type = RequestUtil.getParameter(req, "type");

		
		
		// 1, 试用代理模式访问获取查询结果
		byte[] xmlresult = null;
		ServletContext context = getServletContext();
		@SuppressWarnings("unused")
		GisServer server = (GisServer) context.getAttribute("GisServer");

		// 如果本地初始化了导航引擎，那么使用本地JNI调用
		if (RouteApi.nativeInited) {
			
			//请求前记�?
			RCLogUtil.getInstance().Log(RequestUtil.getParameter(req, "uid"), start, end, type);
			
			xmlresult = getFromNative(start, end, waypoint, type);
			if (xmlresult == null) {
				ResponseUtil.error(resp, "本地导航查询出错");
				return;
			}
			//记录错误代码
			if(xmlresult.length < 10)
				RCLogUtil.getInstance().LogNativeRes(new String(xmlresult,"utf8"));
			else
				RCLogUtil.getInstance().LogNativeRes("success");
		}
		// 如果配置了网络代理，那么使用网络代理
		else if (RouteApi.proxyserver != "") {
			QuJiaProxy.getURL(
					RouteApi.proxyserver + "/SE_RC?" + req.getQueryString(),
					resp, null);
			return;
		}
		// �?后尝试使用本地代理方�?
		else {
			xmlresult = getFromServer(req.getServerPort(), start, end,
					waypoint, type);
			if (xmlresult == null) {
				ResponseUtil.error(resp, "代理查询错误");
				return;
			}
		}

		// 返回结果
		resultFromXml(req, resp, xmlresult);
	}

	private byte[] getFromNative(String start, String end, String waypoint,
			String type) throws UnsupportedEncodingException {
		// 起止�?
		String[] ss = start.split(",");
		String[] ee = end.split(",");
		int x0 = imul10000(ss[0]);
		int y0 = imul10000(ss[1]);
		int x1 = imul10000(ee[0]);
		int y1 = imul10000(ee[1]);

		// 途经�?
		int maxcnt = 32;
		int[] xv = new int[maxcnt * 2];
		int xvcnt = 0;
		try {
			String[] wp = waypoint.split(";");
			for (int i = 0; i < wp.length; i++) {
				String[] dd = wp[i].split(",");
				if (dd == null || dd.length != 2)
					break;
				xv[xvcnt * 2] = imul10000(dd[0]);
				xv[xvcnt * 2 + 1] = imul10000(dd[1]);
				xvcnt++;
			}
		} catch (Exception e) {

		}
		// 查询类型
		int priority = -1;
		try {
			priority = Integer.parseInt(type.trim());
		} catch (Exception ee1) {

		}

		String out = RouteApi.Plan(x0, y0, x1, y1, xvcnt, xv, priority);

		// System.out.println(out);

		return out.getBytes("utf8");
	}

	// 使用代理查询的方式从其他服务�? 获得查询结果
	private byte[] getFromServer(int port, String start, String end,
			String waypoint, String type) {
		String routeServer = "http://localhost:" + port + "/";

		String url = "route/maproute?";

		String[] ss = start.split(",");
		url += "x0=" + mul10000(ss[0]) + "&y0=" + mul10000(ss[1]);

		String[] ee = end.split(",");
		url += "&x1=" + mul10000(ee[0]) + "&y1=" + mul10000(ee[1]);

		try {
			String[] wp = waypoint.split(";");
			for (int i = 0; i < wp.length; i++) {
				String[] dd = wp[i].split(",");
				url += "&xv" + i + "=" + mul10000(dd[0]) + "&yv" + i + "="
						+ mul10000(dd[1]);
			}
		} catch (Exception e) {

		}

		if (!RequestUtil.isNull(type)) {
			url += "&priority=" + type;
		}

		// System.out.println(routeServer + url);

		// 查询返回结果
		ByteArrayOutputStream rsout = new ByteArrayOutputStream();
		if (!QuJiaProxy.getURL(routeServer + url, null, rsout)) {

			return null;
		}

		return rsout.toByteArray();
	}

	// 把导航引擎的xml结果整理成为我们�?要的结果格式
	private void resultFromXml(HttpServletRequest req,
			HttpServletResponse resp, byte[] xmldata) throws IOException {
		String output = RequestUtil.getParameter(req, "output");
		String callback = RequestUtil.getParameter(req, "callback");
		// 重新组织结果
		try {
			// String jsonstr = new String(, "utf8");

			SAXReader reader = new SAXReader();
			Document document = reader.read(new ByteArrayInputStream(xmldata));

			Element root = document.getRootElement();

			// 总计数据
			Element sum = root.element("sum");
			double dist = Double.parseDouble(sum.attribute("dist").getText());
			int duration = Integer
					.parseInt(sum.attribute("duration").getText());

			// 组织关键点数�?
			boolean xml = "xml".equalsIgnoreCase(output) || output == null;
			Object events = null;

			if (xml) {
				events = new JSONObject();
			} else {
				events = new JSONArray();
			}
			@SuppressWarnings("unchecked")
			List<Element> steps = root.element("steps").elements("step");
			String lastroad = null;
			String roadlevel = "";
			int speedlevel = 7;

			for (int i = 0; i < steps.size(); i++) {
				Element step = steps.get(i);

				JSONObject event = new JSONObject();

				double lon = Double.parseDouble(step.attribute("x").getText()) * 0.00001;
				double lat = Double.parseDouble(step.attribute("y").getText()) * 0.00001;

				event.put("lng", lon);
				event.put("lat", lat);
				event.put("nextdistance", step.attribute("dist").getText());

				// 到达类型
				if (i == 0)
					event.put("reachtype", "出发");
				else
					event.put("reachtype", "继续行驶");

				String roadType = step.attribute("roadType").getText();
				String[] tt = roadType.split("-");
				roadlevel = tt[0];
				speedlevel = Integer.parseInt(tt[1]);

				event.put("turntype", step.attribute("turn").getText());
				event.put("roadlevel", roadlevel);
				event.put("limitspeed", getLimitSpeed(speedlevel));
				event.put("eventprompt", step.attribute("desc").getText());
				event.put("realtraffic", "无数�?");

				// 道路和改变类�?
				String thisroad = step.attribute("roadName").getText();

				event.put("roadname", thisroad);
				if (lastroad == null || lastroad.equals(thisroad))
					event.put("changename", "没有改变");
				else if (thisroad.equals(""))
					event.put("changename", "路名改变，但无路�?");
				else
					event.put("changename", "道路名改�?");
				lastroad = thisroad;

				if (events instanceof JSONObject)
					((JSONObject) events).append("event", event);
				else if (events instanceof JSONArray)
					((JSONArray) events).put(event);
			}
			// 添加�?个终点位�?
			{
				double dstlon = Double.parseDouble(root.attribute("x1")
						.getText()) * 0.00001;
				double dstlat = Double.parseDouble(root.attribute("y1")
						.getText()) * 0.00001;
				JSONObject event = new JSONObject();

				event.put("lng", dstlon);
				event.put("lat", dstlat);
				event.put("nextdistance", "0");
				event.put("reachtype", "到达目的地附�?");

				event.put("turntype", "");

				event.put("roadname", "");
				event.put("roadlevel", roadlevel);
				event.put("limitspeed", getLimitSpeed(speedlevel));

				event.put("eventprompt", "到达目的地附�?");
				event.put("realtraffic", "无数�?");
				event.put("changename", "没有改变");

				if (events instanceof JSONObject)
					((JSONObject) events).append("event", event);
				else if (events instanceof JSONArray)
					((JSONArray) events).put(event);
			}

			// 组织坐标点数�?
			@SuppressWarnings("unchecked")
			List<Element> pnts = root.element("path").elements("pnt");
			String routetxt = "";
			for (int i = 0; i < pnts.size(); i++) {
				Element pnt = pnts.get(i);
				int lon = Integer.parseInt(pnt.attribute("x").getText());
				int lat = Integer.parseInt(pnt.attribute("y").getText());

				// 转成36进制
				routetxt += Integer.toString(lon, 36);
				routetxt += Integer.toString(lat, 36);
			}
			JSONObject route = new JSONObject();
			route.put("pointnum", pnts.size());
			route.put("routetxt", routetxt);

			// 返回结果
			JSONObject jresult = new JSONObject();

			jresult.put("distance", String.format("%.1f", dist * 0.001) + "公里");
			jresult.put("time", String.format("%d", duration / 60) + "分钟");
			jresult.put("events", events);
			jresult.put("route", route);
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback,
					jresult);

		} catch (Exception e) {

			ResponseUtil.error(resp, "路径规划失败");
			return;
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request,response);
	}
}
