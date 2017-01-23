package com.evialab.the3rd;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 


import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.gisserver.GCJCoordTrans;
import com.evialab.gisserver.GisServer;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;
import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * Servlet implementation class GetWeather
 */
public class GetWeather extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String weatherServer = "http://114.251.147.82:8000/api/weather/";
	private static final String positionServlet = "forecastByLocation.do";
	private static final String adminServlet = "forecastByAdminCode.do";
	private static final String regionServlet = "getWeatherByRegion.do";
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetWeather() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


		resp.setHeader("Access-Control-Allow-Origin", "*");
		
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_Weather")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}	
		
		//分几种类�?
		String t = RequestUtil.getParameter(req,"t");
		
		//按行政区划查�?
		if("admin".equals(t))
		{
			queryAdmin(req, resp);
		}
		//按照范围查询
		else if("region".equals(t))
		{
			queryRegion(req, resp);
		}
		//按照位置查询
		else  // "position"
		{
			queryPosition(req, resp);
		}
	}
	//位置查询
	void queryPosition(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{ 
		//lng lat   output callback  language   periodtype  starttime
		 //经纬度参数必须有
		try
		{
		  String lng = RequestUtil.getParameter(req,"lng");
		  String lat = RequestUtil.getParameter(req,"lat");
		  String coordinate=RequestUtil.getParameter(req,"cd");//获取坐标类型
		  if(coordinate!=null){
				if(coordinate.equalsIgnoreCase("wgs84")){//如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标
					 String pt=lng+","+lat;
					 pt=GCJCoordTrans.tranpoint(pt);
					 String[] pos = pt.split(",");
					 lng = pos[0];
					 lat =pos[1];
				}
				 }
		  String periodtype = RequestUtil.getParameter(req,"periodtype");
		  String starttime = RequestUtil.getParameter(req,"starttime");
		  
		  //从第三方服务器上获取都是json，获得结果后转为我们的result
		  String url = "lng=" + lng + "&lat=" + lat;
		  if(periodtype != null  && periodtype.length() > 0)
		  {
			  url += "&periodType=" + periodtype;
		  }
		  if(starttime != null  && starttime.length() > 0)
		  {
			  url += "&targetTime=" + starttime;
		  }
		  
		  JSONObject result = getWeather(positionServlet,url);
		  
		  String output = RequestUtil.getParameter(req, "output");
		  String callback = RequestUtil.getParameter(req, "callback");
		  ResponseUtil.writeJson2Response(resp, output, "utf-8", callback,result);
		}
		catch(Exception e)
		{
			ResponseUtil.error(resp, e.getMessage());
			System.out.println(e.getMessage());
			return;
		}
		
	}
	//行政区划查询
	void queryAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		try
		{
			  String admincode = RequestUtil.getParameter(req,"admincode");
 
			  String url = "adminCode=" + admincode;

			  JSONObject result = getWeather(adminServlet,url);
			  
			  String output = RequestUtil.getParameter(req, "output");
			  String callback = RequestUtil.getParameter(req, "callback");
			  ResponseUtil.writeJson2Response(resp, output, "utf-8", callback,result);
		}
		catch(Exception e)
		{
			ResponseUtil.error(resp, e.getMessage());
			System.out.println(e.getMessage());
			return;
		}
	}
	//范围查询
	void queryRegion(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		try
		{
			  String west = RequestUtil.getParameter(req,"west");
			  String east = RequestUtil.getParameter(req,"east");
			  String south = RequestUtil.getParameter(req,"south");
			  String north = RequestUtil.getParameter(req,"north");
			  String level = RequestUtil.getParameter(req,"level");
			  
			  //从第三方服务器上获取都是json，获得结果后转为我们的result
			  String url = "LLon=" + west 
					  + "&RLon=" + east 
					  + "&RLat=" + south
					  + "&LLat=" + north
					  + "&level=" + level;
 
			  JSONObject result = getWeather(regionServlet,url);
			  
			  String output = RequestUtil.getParameter(req, "output");
			  String callback = RequestUtil.getParameter(req, "callback");
			  ResponseUtil.writeJson2Response(resp, output, "utf-8", callback,result);
		}
		catch(Exception e)
		{
			ResponseUtil.error(resp, e.getMessage());
			System.out.println(e.getMessage());
			return;
		}
	}
	
	//访问服务器获得json结果
	static JSONObject  getWeather(String type, String param) throws UnsupportedEncodingException
	{
		//附加language cdata
		String url = weatherServer + type + "?";
		param = "language=zh_CN&cata=json&userid=cttic&" + param; 
		
	 		//正式服务器需要提交key
		
		try {
			String key= getKey(param);
			param = param+"&key=" + key;
			
		} catch (Exception e) {
			 System.out.println("getkey error:" + e.getMessage());
		} 
		url+=param;
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (getURL(url, output)) 
		{
 
		   String str =  output.toString("utf8");
		//   System.out.println(str);
		   JSONObject json = new JSONObject(str);
		   
		   return json;
		}
		else
		{
			System.out.println(url);
			return null;
		}
	}
	
	private static String userkey = "71ef8f782a95a017";
	static String getKey(String data) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException
	{
		//data = URLEncoder.encode(data, "UTF-8");
		
		SecretKeySpec signingKey = new SecretKeySpec(userkey.getBytes(), "HmacSHA1");
		//key 为提供给用户的密匙为 byte[]数组类型(通过 Base64 解码算法解码用户密匙获得)
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);
		byte[] rawHmac = mac.doFinal(data.getBytes());
		//date 为需要加密的字符�?
		String result = Base64.encode(rawHmac);
		
		result = result.replace('+', '-');
		result = result.replace('/', '_');
		
		return result;
	}
	
	 
	static boolean getURL(String url, OutputStream stream) {
		try {
			URL getUrl;

			getUrl = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) getUrl
					.openConnection();

			connection.connect();

			byte[] buf = new byte[1024 * 20];

			InputStream instream = connection.getInputStream();

			int nread = 0;

			while ((nread = instream.read(buf)) != -1) {

				stream.write(buf, 0, nread);
			}

			instream.close();

			connection.disconnect();
		} catch (Exception e) {

			return false;
		}
		return true;

	}
	public   static void main(String[] argv){
		
		 String west ="72.004";
		  String east = "137.8347";
		  String south = "0.8293";
		  String north = "55.8271";
		  String level = "10";
		  
		  //从第三方服务器上获取都是json，获得结果后转为我们的result
		  String url = "LLon=" + west 
				  + "&RLon=" + east 
				  + "&RLat=" + south
				  + "&LLat=" + north
				  + "&level=" + level;

		  try {
			JSONObject result = getWeather(regionServlet,url);
		   JSONArray arr = 	result.getJSONArray("data");
			System.out.println(arr.length());
			System.out.print(result.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
