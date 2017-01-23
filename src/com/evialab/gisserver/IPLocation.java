/**
 * 
 */
package com.evialab.gisserver;

import java.io.IOException;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.evialab.util.DBPool;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;
 

/**
 * @author Administrator
 *
 */
public class IPLocation extends HttpServlet{

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(response, "内部错误");
			return;
		}
		
		String output = RequestUtil.getParameter(req,"output");
		String callback = RequestUtil.getParameter(req,"callback");
		String encoding = RequestUtil.getParameter(req,"encoding");
		

		if (!server.processUID(req, "SE_IP")) {
			ResponseUtil.error(response, output, encoding, callback, "UID无效");
			return;
		}
		 
		//获取查询条件
		@SuppressWarnings("unused")
		String st = RequestUtil.getParameter(req,"st");
		
		//解析ip地址
		long ipnumber = 0;
		try 
		{
			String ip = RequestUtil.getParameter(req,"ip");
			if(ip==null){
				 ip = RequestUtil.getIp(req);
			}
		    String [] ips = ip.split("\\.");
			for(String s: ips)
			{
				ipnumber = ipnumber<<8;
				ipnumber += Integer.parseInt(s);
			}	
		}
	    catch(Exception e)
	    {
	    	ResponseUtil.error(response, output, encoding, callback, "解析ip参数出错");
			return;
	    }
		
	 
		
	
		
		//�?始查�?
		JSONObject jresult = new JSONObject();
	 
		Connection con = null;
		try{
			con = DBPool.getDataInstance().getConnection();
			
			PreparedStatement stm = con.prepareStatement("select * from ip where numbegin <= ? and numend >= ? limit 1");
			
			stm.setLong(1, ipnumber);
			stm.setLong(2, ipnumber);
			ResultSet rs = stm.executeQuery();
			if (rs.next()) {
				// 行政区划编码
				String admincode = rs.getString("admincode");
				jresult.put("district", admincode);

				String province = rs.getString("province");
				String city = rs.getString("city");
				String district = rs.getString("district");
				jresult.put("district_text", province + ">" + city + ">" + district);
			}
			
			stm.close();
			
			//返回查询结果
			ResponseUtil.writeJson2Response(response,output,encoding,callback,jresult);
			
		}catch(Exception e)
		{
			ResponseUtil.error(response,output,encoding,callback,"查询出错");
		}
		finally{
			if(con != null)
				DBPool.getDataInstance().closeConnection(con);
		}
	 
		
	
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		
		doGet(req,response);
	}
}
