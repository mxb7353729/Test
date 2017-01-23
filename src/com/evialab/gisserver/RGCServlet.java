package com.evialab.gisserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.*;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class RGCServlet extends HttpServlet {

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

		MonitorRequest.AddRequest("RGCServlet", request);

		response.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}

		boolean isOldVersion = false;

		String st = RequestUtil.getParameter(request, "st");
		String uid = RequestUtil.getParameter(request, "uid");
		String type = request.getParameter("type");
		String pt = request.getParameter("point");
		String ip = RequestUtil.getIp(request);
		if (pt == null) {
			isOldVersion = true;
			pt = request.getParameter("pt");
		}
		String coordinate = request.getParameter("cd");// 获取坐标类型
		if (coordinate != null) {
			if (coordinate.equalsIgnoreCase("wgs84")) {// 如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标

				pt = GCJCoordTrans.tranpoint(pt);
			}
		}
		String adr = request.getParameter("adr");
		if (adr==null)
			adr="null";
		String output = request.getParameter("output");
		String callback = request.getParameter("callback");
		String encoding = request.getParameter("encoding");
		if(RequestUtil.isNull(encoding))
			encoding = "utf-8";
		
		String version = request.getParameter("v");

		if (version == null)
			version = "1";
		double x = Double.parseDouble(pt.substring(0, pt.indexOf(',')));
		double y = Double.parseDouble(pt.substring(pt.indexOf(',') + 1));

		// 记录请求的参�?
		if(server.rgcrecord != null)
			server.rgcrecord.record(uid, ip, st, pt);

		
		
		
		
		// 判断是否在合理范�?
		if (x < 71 || x > 136 || y < 3 || y > 53) {
			ResponseUtil.error(response, output, encoding, callback, "不合理的请求:位置超界");
			return;
		}

		if ("rgc".equalsIgnoreCase(st)) {
			if (!server.processUID(request, "SE_RGC")) {
				ResponseUtil.error(response, output, encoding, callback, "UID无效");
				return;
			}
			
			long t0 = System.currentTimeMillis();
			JSONObject admin = RGCPostgreSql.getAdmin(x, y);
			long t = System.currentTimeMillis() - t0;
			

			// 根据admin构�?�返回的xml
			if (isOldVersion) {
				String xml = admin2011(admin, encoding, t);
				if(xml != null){
					response.setContentType("text/xml;charset="+encoding);
					response.getOutputStream().write(xml.getBytes(encoding));
				}
				else{
					ResponseUtil.error(response, output, encoding, callback, "查询出错");
				}
			}
			// 新版方式返回
			else {
				ResponseUtil.writeJson2Response(response, output, encoding, callback, admin);
			}

		} else if ("rgc2".equalsIgnoreCase(st)) {
			if (!server.processUID(request, "SE_RGC2")) {
				ResponseUtil.error(response, output, encoding, callback, "UID无效");
				return;
			}
			
			

			// 根据rgc构�?�返回的xml
			if (isOldVersion) {
				long t0 = System.currentTimeMillis();
				JSONObject rgc = RGCPostgreSql.rgcWithoutRoad(x, y,adr);
				long t = System.currentTimeMillis() - t0;
				String xml = rgc2011(rgc, encoding,t);
				if(xml != null){
					response.setContentType("text/xml;charset="+encoding);
					response.getOutputStream().write(xml.getBytes(encoding));
				}
				else{
					ResponseUtil.error(response, output, encoding, callback, "查询出错");
				}
			}
			// 新版方式返回
			else {
				int angle = -1;
				try{
					angle = Integer.parseInt(RequestUtil.getParameter(request, "angle"));
					
				}catch(Exception ee){
					
				}
				
				JSONObject rgc = null;
				if(angle > 0)
					rgc = RGCPostgreSql.rgcWithAngle(x, y,angle,adr);
				else
					rgc = RGCPostgreSql.rgc(x, y,adr);
				
				
				ResponseUtil.writeJson2Response(response, output, encoding, callback, rgc);
			}
			
		} else {
			ResponseUtil.error(response, output, encoding, callback, "st参数错误");
		}

		return;
	}

	
	String admin2011(JSONObject admin, String encoding,long t){
		
		try{
			Document doc = DocumentHelper.createDocument();
			Element root = doc.addElement("Result");
			root.addAttribute("time", Long.toString(t));
			root.addAttribute("error", "0");
			Element info = DocumentHelper.createElement("AInfoB");
			Element dis = info.addElement("Dis");
			dis.addAttribute("v", admin.getString("district"));
			Element pro = info.addElement("Pro");
			pro.addAttribute("v", admin.getString("district_text"));
		    root.add(info);
			return doc.asXML();
		}catch(Exception e){
		
			return null;
		}
	}
	
	String rgc2011(JSONObject admin, String encoding, long t){
		
		try{
			org.dom4j.Document doc = DocumentHelper.createDocument();
			Element result = DocumentHelper.createElement("Result");
			result.addAttribute("time", Long.toString(t));
			result.addAttribute("error", "0");
			Element add = result.addElement("Add");
			JSONObject poi= admin.getJSONObject("point");
			if(poi.has("number"))
			{
				add.addAttribute("v", poi.getString("name") + "(" + poi.getString("number") + ")" + poi.getString("dir") +  poi.getInt("distance") + "�?");
			}
			else
			{
				add.addAttribute("v", admin.getString("address"));
			}
			Element dis = result.addElement("Dis");
			dis.addAttribute("id", admin.getString("district"));
			dis.addAttribute("name", admin.getString("district_text"));
			Element Poi = result.addElement("Poi");
			Poi.addAttribute("name", poi.getString("name"));
			Element geo = result.addElement("Geo");
			geo.addAttribute("lo", Double.toString(poi.getDouble("lng")));
			geo.addAttribute("la", Double.toString(poi.getDouble("lat")));
			doc.add(result);
			doc.setXMLEncoding("utf-8");	        
	        return doc.asXML();   
		}catch(Exception e){
		
			return null;
		}
	}


	public static void main(String[] args) {

		double x = 116.46719;
		double y = 40.16278;

//		RGCAdminSearch.Init("D:\\四维\\roadindex-width-urban.db3");
//		RGCRoadSearch.Init("D:\\四维\\roadindex-width-urban.db3");
//		RGCPoiSearch.Init("D:\\四维\\poigrid.db3");
//		LocalSearchLucene.InitSearcher("E:\\四维\\2 数据\\02 2014年夏\\本地搜索处理\\index");
//
//		for (int i = 0; i < 10000; i++) {
//
//			JSONObject jadmin = RGCAdminSearch.getRGCAdmin(x, y - i * 0.0002);
//
//			JSONObject jroad = RGCRoadSearch.GetNearestRoad(x, y - i * 0.0002, 0);
//
//			// JSONObject jpoi = LocalSearchLucene.SearchNearestPOI(x, y, 0.01);
//			JSONObject jpoi = RGCPoiSearch.Search(x, y - i * 0.0002);
//			// JSONObject jpoi = LocalSearchLucene.SearchNearestPOI(x, y,0.01);
//			if (jpoi == null) {
//				// jpoi = LocalSearchLucene.SearchNearestPOI(x, y, 0.1);
//			}
//			// if(jpoi == null)
//			{
//				// ResponseUtil.error(response, output, encoding, callback,
//				// "无查询结�?");
//				// return;
//			}
//
//			jadmin.put("road_address", jroad.get("road_address"));
//			if (jpoi != null) {
//				jadmin.put("address", jpoi.get("address"));
//				jpoi.remove("address");
//				jadmin.put("point", jpoi);
//			}
//			jroad.remove("road_address");
//
//			jadmin.put("road", jroad);
//			if ((i % 100) == 0)
//				System.out.println(jadmin.toString());
//		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request,response);
	}
}
