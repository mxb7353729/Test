/**
 * 
 */
package com.evialab.gisserver;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.evialab.common.FullDataReader;
import com.evialab.the3rd.GoogleProxy;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

//import com.evialab.pakserver.pakpack;

/**
 * @author Administrator
 *
 */
public class GetImage extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest  req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setHeader("Access-Control-Allow-Origin", "*");
		//�?测服务状�?
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_Mapimg")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}		 
		 
		
		
		@SuppressWarnings("unused")
		String st = RequestUtil.getParameter(req,"st");
		
		String pt = RequestUtil.getParameter(req,"pt");   //经纬�?
		String box = RequestUtil.getParameter(req,"box");  //x y
		if(pt!=null){
		String coordinate=RequestUtil.getParameter(req,"cd");//获取坐标类型
		if(coordinate!=null){
		if(coordinate.equalsIgnoreCase("wgs84")){//如果是wgs84的话就进行转化成wgs84坐标，默认gcj02坐标
			 pt=GCJCoordTrans.tranpoint(pt);
		  }
		 }
		}
		@SuppressWarnings("unused")
		String uuid = RequestUtil.getParameter(req,"uuid");
		
		int lev = 0;
		try{ lev = Integer.parseInt(RequestUtil.getParameter(req,"lev"));}catch(Exception e){} //level
		
		String type = RequestUtil.getParameter(req,"type");  //有用
		
		
		long x = 0;
		long y = 0;
		if(box != null)
		{
			try{
				String [] a = box.split(",");
				x = Long.parseLong(a[0]);
				y = Long.parseLong(a[1]);
			}
			catch(Exception e)
			{
				return;
			}
		}
		else if(pt != null)
		{
			//结合pt 转为 x，y
			try{
				String [] a = pt.split(",");
				double lon = Double.parseDouble(a[0]);
				double lat = Double.parseDouble(a[1]);
				
				//转为wlat
				double wlon = lon;
				double wlat = MapRender.BL2WebMercator(lat);
				
				x = MapRender.GetWLonIndex(wlon,lev, false);
				y = MapRender.GetWLatIndex(wlat,lev, false);
			}
			catch(Exception e)
			{
				return;
			}
		}
		else {
		 
			x = Long.parseLong(RequestUtil.getParameter(req,"x"));
			y = Long.parseLong(RequestUtil.getParameter(req,"y"));
			
		}
 /*
		//这里又添加一个判断， 默认返回的是�?张图 
		if(type == null || type.equals("vect"))
		{
			   QuJiaProxy.get(x,y,lev,resp);
			   return;
		}
	*/	
		//这里修改如果是获取地图，那么走的arcgis出图
		byte[] retimage = null;
		if(type == null || type.equals("vect") || type.equals("e_vect"))
		{
			retimage =GetImage(x,y, lev,server);
		}
		//其他类型依然走的是测试数�?  直接
		else
		{
		
			if(GoogleProxy.get(x, y, lev, type,resp))
				return;
			
		}
		if(retimage == null)
			retimage = server.blankimage;	
		
		if(retimage != null)		
		{
			resp.setContentType("image/png");
			resp.setContentLength(retimage.length);
			resp.getOutputStream().write(retimage);
		}
		else
		{
			resp.sendError(404);
		}
		 
	}
	public static byte[] GetImage(long x, long y, int lev, GisServer server){
		byte[] retimage = null;
		String cathpath = "2014win/" + CachUtil.imgPath(x, y,  lev);			
		retimage = CachUtil.readCach(cathpath);
		if(retimage == null)
		{
			if(lev <= 8)
				retimage = server.vectlayer.getImage(x, y, lev);
			else
				retimage = FullDataReader.getDefaultInstance().GetImage(x, y, lev);
			 
			
			if(retimage != null)
				CachUtil.writeCach(cathpath, retimage);
		}	
		return retimage;
	}
  
}
