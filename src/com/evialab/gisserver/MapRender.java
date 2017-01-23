package com.evialab.gisserver;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.JSONObject;

import com.evialab.util.ArcGisBundleReader;
 

 
public class MapRender {

	/**
	 * @param args
	 */
	GisServer server = null; 
	public MapRender(GisServer s){
		server = s;
	}
	 
	private Graphics2D g = null;
	private int width  = 400;
	private int height = 400;
	private double centerlon = 0;
	private double centerlat = 0;
	private int  level = 0;
	private BufferedImage bufferimg = null;
	private String        rootpath = "";
	private String        basepath = "";
	private BufferedImage defaultIcon = null;
	
	public void setGraphic(Graphics2D graphic){
		g = graphic;
	}
    public void setRoot(String p, String b)
    {
    	basepath = b;
    	rootpath = p;
    }
    public void setDefaultIcon(BufferedImage img)
    {
    	defaultIcon = img;
    }
	public void createGraphic(int w, int h){
		width = w;
		height = h;
		bufferimg = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		g = bufferimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	public void zoomToBox(double west, double east ,double north, double south){
		
		//注意绘制地图的中心点，不�? south �? north 的中心，而是转换后的中心
		south = BL2WebMercator(south);
		north = BL2WebMercator(north);
		
		centerlon = (west + east)* 0.5;
		centerlat = (south + north) * 0.5;
		
		//计算当前的级�?
		
		//计算当前的精�?
		double res = (east  - west) / width;
		
		//根据精度计算级别
		level = GetResLevel(res);
	}
	public void createGraphicWithBox(double west, double east ,double south, double north, int zoom)
	{
		south = BL2WebMercator(south);
		north = BL2WebMercator(north);
		
		centerlon = (west + east)* 0.5;
		centerlat = (south + north) * 0.5;
		
		
		if(resolutions.isEmpty()){
			InitRes();
		}
		
		level = zoom;
		double res = resolutions.get(level);
		

		
		int w = (int)Math.ceil(((east  - west) / res));
		int h = (int)Math.ceil((north  - south) / res) ;
		
		createGraphic(w,h);
	}
	
	public void zoomTo(double lon, double lat, int zoom)
	{
		centerlon = lon;
		centerlat = BL2WebMercator(lat);
		level = zoom;
	}
	
	//绘制底图
	public void render()
	{
		if(resolutions.isEmpty()){
			InitRes();
		}
		
		// res 表示每像素代表多少地理宽�?
		double res = resolutions.get(level);
		//根据中心点，计算当前的显示范�?
		double west = centerlon - width * 0.5 * res;
		double east = centerlon + width * 0.5 * res;
		double south = centerlat - height * 0.5 * res;
		double north = centerlat + height * 0.5 * res;
		
		int minx = GetWLonIndex(west, level);
		int maxx = GetWLonIndex(east, level);
		
		int miny = GetWLatIndex(north, level);
		int maxy = GetWLatIndex(south, level);
		
		for(int i = minx; i<= maxx; i++){
			for(int j = miny; j<= maxy; j++){
				
				renderTile(i,j);
				
			}
		}
	}
	//绘制�?个块
	private void renderTile(int x, int y){
		//byte[] retimage = ArcGisCacheReader.GetImage(x, y, level);
		//byte[] retimage =  QuJiaProxy.getData(x,y,level);
		
		//这里记录�?下，以后数据全了，必须修改为backlayer获取
		byte[] retimage = GetImage.GetImage(x, y, level, server);
		
		
		if(retimage== null)
			return;
		
		//获得这个这个�?要的地理范围
		double size = GetLevelSize(level);
		
		
		double west = x * size - 180;
		double north = 180 - y * size;
			
		double res = resolutions.get(level);
		
		
		int left = (int)Math.round((west - centerlon) / res + width * 0.5);
		int top = (int)Math.round((centerlat - north) / res + height * 0.5);
		
		//在该位置绘图
		BufferedImage img;
		try {
			img = ImageIO.read(new ByteArrayInputStream(retimage));
			
			
		} catch (IOException e) {
			 
			return;
		}
		//g.drawImage( img, left,top,null);	
		g.drawImage( img, left,top,null);
	}
	
	//绘制标注�?
	public void renderMarker(double xc, double yc, boolean isBL, String path)
	{
		Point p = XYtoImg(xc,yc, isBL);
		try {
			
			URL url = new URL(path);
			
            //载入图片到输入流
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            
            BufferedImage img = ImageIO.read(bis);
			//BufferedImage img;
			//img = ImageIO.read(new File(path));
 
			g.drawImage( img, p.x,p.y,null);
			
		} catch (IOException e) {
			 
			return;
		}		
	}
	
	//绘制文字
	public void renderText(double xc, double yc, boolean isBL, String[] content, int fontSize, Color color, Color bgColor, int bgWidth, int bgHeight)
	{
		Point p = XYtoImg(xc,yc, isBL);
		
	      Font f = new Font(null, Font.PLAIN, fontSize);
 
          g.setFont(f);
          
          
         FontMetrics fm = g.getFontMetrics();  
          
        // Rectangle2D rec=fm.getStringBounds(content, g);  
          
         
         // bgWidth = Math.max((int)rec.getWidth() + 4, bgWidth);
        //  bgHeight = Math.max((int)rec.getHeight(), bgHeight);
         int w = 0;
         int h = 0;
         for(int i = 0; i < content.length; i++){
        	String ci = content[i];
 			Rectangle2D rec=fm.getStringBounds(ci, g); 
 			if(i == 0)
 				{
 				  w = (int) (rec.getWidth() + 3);
 				  h = (int) (rec.getHeight()) + 2;
 				}
 			else
 			{
 				  w = Math.max(w, (int) (rec.getWidth() + 3));
				  h = h + (int) (rec.getHeight()) + 2;
 			}
 			  
         }
         
		g.setColor(bgColor);
		//g.fillRect(p.x - 2,p.y - bgHeight + 5 , bgWidth,bgHeight);
		g.fillRect(p.x,p.y , w,h);
		g.setColor(color);
		int sy = p.y ;
		for(int i = 0; i < content.length; i++){
			String ci = content[i];
			Rectangle2D rec=fm.getStringBounds(ci, g);  
			
			g.drawString(ci,p.x + 2,sy + (int)rec.getHeight());
			
			sy += rec.getHeight() + 2;
		}
		
		
	}
	
	//绘制折线
	public void renderLine(double [] xc, double [] yc, boolean isBL,
			Color color, float weight, int lineType)
	{
		int num = xc.length;
		
		int [] px = new int[num];
		int [] py = new int[num];
		for(int i = 0; i < num; i++){
			Point p = XYtoImg(xc[i],yc[i],isBL);
			px[i] = p.x;
			py[i] = p.y;
		}
		
		BasicStroke bs = null;
		
		if(lineType == 2)
			bs = new BasicStroke(weight,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
		else
		    bs = new BasicStroke(weight,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
				3.5f,new float[]{5.0f,10.0f,},0f);
		
	 
		((Graphics2D)g).setStroke(bs);
		g.setColor(color);
		
		g.drawPolyline(px,py, num);
	}
	//绘制多边�?
	public void renderPoly(double [] xc, double [] yc, boolean isBL,
			Color fillColor, Color color, float weight)
	{
		int num = xc.length;
		
		int [] px = new int[num];
		int [] py = new int[num];
		for(int i = 0; i < num; i++){
			Point p = XYtoImg(xc[i],yc[i],isBL);
			px[i] = p.x;
			py[i] = p.y;
		}
		
		
		//绘制�?
		g.setColor(fillColor);
		g.fillPolygon(px,py,num);
		//绘制边线
		BasicStroke bs = new BasicStroke(weight,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
		((Graphics2D)g).setStroke(bs);
		g.setColor(color);
		g.drawPolygon(px,py, num);
	}
	
	Point2D parsePoint(String g){
		String[] ss = g.split(",");
		double lon = Float.parseFloat(ss[0]);
		double lat = Float.parseFloat(ss[1]);
		
		
		return this.XYtoImg(lon,lat);
	}
	Path2D parsePolygon(String g){
		String[] ss = g.split(",");
		Path2D p = new Path2D.Double();
		for( int i =0; i < ss.length / 2; i++){
			
			double lon = Double.parseDouble(ss[i * 2]);
			double lat = Double.parseDouble(ss[i * 2 + 1]);
			
			Point2D pp = this.XYtoImg(lon,lat);
			if(i == 0)
				p.moveTo(pp.getX(),pp.getY());
			else
				p.lineTo(pp.getX(),pp.getY());
		}
		
		return p;
	}
	
	Rectangle2D parseRect(String g){
		
		String[] ss = g.split(",");
		
		double w = Float.parseFloat(ss[0]);
		double s = Float.parseFloat(ss[1]);
		
		double e = Float.parseFloat(ss[2]);
		double n = Float.parseFloat(ss[3]);
		
		Point2D ws = this.XYtoImg(w,s);
		Point2D en = this.XYtoImg(e,n);
 
		Rectangle2D rect  = new Rectangle2D.Double(ws.getX(),en.getY(),en.getX() - ws.getX(),ws.getY() - en.getY());
		return rect;
	}
	//绘制�?个overlay
	public void renderOverlay(JSONObject o){
		String t = o.get("t").toString();
		String geo = o.get("g").toString();
		JSONObject s =o.getJSONObject("s");
		//绘制图标
		if("m".equals(t))
		{
			Point2D p = parsePoint(geo);
			//该位置绘�?
			 /* externalGraphic - {String} Url to an external graphic that will be used for rendering points.
			 * graphicWidth - {Number} Pixel width for sizing an external graphic.
			 * graphicHeight - {Number} Pixel height for sizing an external graphic.
			 * graphicOpacity - {Number} Opacity (0-1) for an external graphic.
			 * graphicXOffset - {Number} Pixel offset along the positive x axis for displacing an external graphic.
			 * graphicYOffset - {Number} Pixel offset along the positive y axis for displacing an external graphic.
			 * */
			double w = safeGet(s, "graphicWidth", 0);
			double h = safeGet(s, "graphicHeight", 0);
			double x = safeGet(s, "graphicXOffset", 0);
			double y = safeGet(s, "graphicYOffset", 0);
			
			String url = safeGet(s, "externalGraphic", "");
			url  =  url.replace(basepath, rootpath);
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(url));
			} catch (IOException e) {
				 
				//return;
			}
			if(img==null)
			{
				img = defaultIcon;
			}
			
			if(img!=null)
			{
				if(w == 0)
					w = img.getWidth();
				if(h == 0)
					h = img.getHeight();
			 
				int px = (int)(p.getX() + x - w * 0.5);
				int py = (int)(p.getY() + y);
				g.drawImage( img, px, py,(int)w, (int)h,   null);
			}
		
			
			//绘制文字
			/*
			label - {String} The text for an optional label. For browsers that use the canvas renderer, this requires either
			 *     fillText or mozDrawText to be available.
			 * labelAlign - {String} Label alignment. This specifies the insertion point relative to the text. It is a string
			 *     composed of two characters. The first character is for the horizontal alignment, the second for the vertical
			 *     alignment. Valid values for horizontal alignment: "l"=left, "c"=center, "r"=right. Valid values for vertical
			 *     alignment: "t"=top, "m"=middle, "b"=bottom. Example values: "lt", "cm", "rb". Default is "cm".
			 * labelXOffset - {Number} Pixel offset along the positive x axis for displacing the label. Not supported by the canvas renderer.
			 * labelYOffset - {Number} Pixel offset along the positive y axis for displacing the label. Not supported by the canvas renderer.
			 * labelOutlineColor - {String} The color of the label outline. Default is 'white'. Only supported by the canvas & SVG renderers.
			 * labelOutlineWidth - {Number} The width of the label outline. Default is 3, set to 0 or null to disable. Only supported by the  SVG renderers.
			 * labelOutlineOpacity - {Number} The opacity (0-1) of the label outline. Default is fontOpacity. Only supported by the canvas & SVG renderers.
			 */
			
			String label = safeGet(s, "label", "");
			if(label.equals(""))
				return;
			double lx = safeGet(s, "labelXOffset", 0);
			double ly = safeGet(s, "labelYOffset", 0);
			
			 useFont(s);
			
			
			FontMetrics fm = g.getFontMetrics();  
	        Rectangle2D rec = fm.getStringBounds(label, g);  
 
	        int plx = (int)(p.getX() + lx - rec.getWidth() * 0.5);
			int ply = (int)(p.getY() - ly);
			
			g.drawString(label,plx,ply);
		}
		else if("l".equals(t))
		{
			Path2D  p = parsePolygon(geo);
			
			useStroke(s);
			g.draw(p);
		}
		else if("p".equals(t))
		{
			Path2D  p = parsePolygon(geo);
			
			if(isfill(s))
			{
				useFill(s);
				g.fill(p);
			}
			if(isStroke(s))
			{
				useStroke(s);
				g.draw(p);
			}
		}
		else if("r".equals(t))
		{
			Rectangle2D  r = parseRect(geo);
			
			if(isfill(s))
			{
				useFill(s);
				g.fill(r);
			}
			if(isStroke(s))
			{
				useStroke(s);
				g.draw(r);
			}
		}
		else if("e".equals(t))
		{
			Rectangle2D  r = parseRect(geo);
			
			Ellipse2D.Double e = new Ellipse2D.Double();
			e.setFrame(r);
			if(isfill(s))
			{
				useFill(s);
				g.fill(e);
			}
			if(isStroke(s))
			{
				useStroke(s);
				g.draw(e);
			}
		}
		else if("c".equals(t))
		{
			Point2D p = parsePoint(geo);
			double radius = o.getDouble("r");
			double R = 6378137;
			
			double res = resolutions.get(level);
			
			radius = radius * 360 / (2 * Math.PI * R * res);
			
			Ellipse2D.Double e = new Ellipse2D.Double(p.getX() - radius,p.getY() -radius,2*radius,2*radius);
			
			
			
			if(isfill(s))
			{
				useFill(s);
				g.fill(e);
			}
			
			if(isStroke(s))
			{
				useStroke(s);
				g.draw(e);
			}

		}
	}
	
	private boolean isfill(JSONObject s){
		if(!s.has("fill"))
			return true;
		return s.getBoolean("fill");
	}
	private boolean isStroke(JSONObject s){
		if(!s.has("stroke"))
			return true;
		return s.getBoolean("stroke");
	}
	
	
	private  String safeGet(JSONObject s, String key, String def){
		if(s.has(key))
			return s.getString(key);
		return def;
	}
	private Double safeGet(JSONObject s, String key, double def){
		if(s.has(key))
			return s.getDouble(key);
		return def;
	}
	private Color getColor(String c, double a)
	{
		Color cl = Color.getColor(c);
		if(cl == null)
		{
			int r = Integer.parseInt(c.substring(1,3),16);
			int g = Integer.parseInt(c.substring(3,5),16);
			int b = Integer.parseInt(c.substring(5,7),16);
			
			cl = new Color(r,g,b);
		}
		
		return new Color(cl.getRed(),cl.getGreen(),cl.getBlue(),(int)(a * 255));
	}
	
	int getCap(String c){
		if(c.equals("butt"))
			return BasicStroke.CAP_BUTT ;
		else if(c.equals("square"))
			return BasicStroke. CAP_SQUARE ;
		else
			return BasicStroke.CAP_ROUND ;
		 
	}
	
	private void useStroke( JSONObject s)
	{
      /*
          "strokeColor",  
          "strokeOpacity", 
          "strokeWidth",  
          "strokeLinecap",  
          "strokeDashstyle",  
          */
		String sc = safeGet(s,"strokeColor","#ee9900");
		double so = safeGet(s, "strokeOpacity", 1);
		Color c = getColor(sc, so);
		g.setColor(c);
		
		float sw = (float)safeGet(s, "strokeWidth", 1).doubleValue();
		
		String sl = safeGet(s, "strokeLinecap", "round");
		String sd = safeGet(s, "strokeDashstyle", "solid");
		
		
		BasicStroke bs = null;
		int cap = getCap(sl);
		if("solid".equals(sd))
			bs = new BasicStroke(sw,cap,cap);
		else
		    bs = new BasicStroke(sw,cap,cap,
				3.5f,new float[]{15.0f,5.0f,},0f);
		
	 
		((Graphics2D)g).setStroke(bs);
	}
	
	private void useFill(JSONObject s)
	{
		/*
		 *   "fillColor", 
             "fillOpacity",  
		 */
		String fc = safeGet(s,"fillColor","#ee9900");
		double fo = safeGet(s, "fillOpacity", 0.4);
		Color c = getColor(fc, fo);
		g.setColor(c);
	}
	private void useFont(JSONObject s)
	{
		 /* fontColor - {String} The font color for the label, to be provided like CSS.
		 * fontOpacity - {Number} Opacity (0-1) for the label
		 * fontFamily - {String} The font family for the label, to be provided like in CSS.
		 * fontSize - {String} The font size for the label, to be provided like in CSS.
		 * fontStyle - {String} The font style for the label, to be provided like in CSS.
		 * fontWeight - {String} The font weight for the label, to be provided like in CSS.
		 * */
		String fontFamily = safeGet(s, "fontFamily", "serif");
		String fontWeight  = safeGet(s, "fontWeight", "normal");

		//Font f = new Font(fontFamily, Font.PLAIN, 13);		
		//g.setFont(f);
		
		String fc = safeGet(s,"fontColor","#000000");
		double fo = safeGet(s, "fontOpacity", 1);
		Color c = getColor(fc, fo);
		g.setColor(c);
	}
	//经纬度转屏幕图像坐标
	public Point  BLtoImg(double lon, double lat){
		Point p = new Point();
		lat = BL2WebMercator(lat);
		double res = resolutions.get(level);
		p.x = (int)Math.round((lon - centerlon) / res + width * 0.5);
		p.y = (int)Math.round((centerlat - lat) / res + height * 0.5);
		
		return p;
	}
	
	
	private Point2D XYtoImg(double lon, double lat)
	{
		lat = BL2WebMercator(lat);
		double res = resolutions.get(level);
		double x =  (lon - centerlon) / res + width * 0.5;
		double y =  (centerlat - lat) / res + height * 0.5;
		
		return new Point2D.Double(x,y);
	}
	
	public Point XYtoImg(double xc, double yc, boolean isBL){
		Point p = null;
		if(isBL){
			p = BLtoImg(xc, yc);
		}
		else
			p = new Point((int)xc,(int)yc);
		
		return p;
	}
	
	//返回数据
	public byte[] getByte(){
		if(bufferimg == null)
			return null;
		try {
			ByteArrayOutputStream ss = new ByteArrayOutputStream();
			ImageIO.write(bufferimg,"png",ss);
			return ss.toByteArray();
		} catch (IOException e) {

			return null;
		}
	}
	
	//经纬度和魔卡托互�?
	//web Mercator 坐标转换为经纬度
	static double  WebMercator2BL(double y)
	{
		double lat;
		lat = Math.atan(Math.pow(Math.E, y * Math.PI / 180 )) * 180 * 2 / Math.PI - 90;
		return lat;
	}
	//经纬度转换为web Mercator
	static double  BL2WebMercator(double lat)
	{
		if(lat == 0)
			return 0;
		double y;
		lat = Math.min(lat, 85.0);
		lat = Math.max(lat, -85.0);
		y = Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 180 / 2)) * 180 / Math.PI;

		return y;
	}

	static double  GetLevelSize(int level)
	{
 		return 360.0 / (1 << level);
	}
	static int   GetResLevel(double res){
		if(resolutions.isEmpty()){
			InitRes();
		}
		
		for(int i = 0; i < MaxLevel; i++)
		{
			if(res > resolutions.get(i))
				 return (i == 0) ? 0 : (i-1);
		}
		return 0;
	}
	static void InitRes(){
		for(int i = 0; i < MaxLevel; i++){
			resolutions.add(GetLevelSize(i) / 256);
		}
	}
	
	public static double GetLevelRes(int level){
		if(resolutions.isEmpty()){
			InitRes();
		}
		if(level > MaxLevel)
			return 0;
		
		return resolutions.get(level -1);
	}
	static List<Double> resolutions = new ArrayList<Double>();
	
	static int   GetSizeLevel(double size)
	{
		int level = 1;
		double csize = 360;
		while (csize > size)
		{
			csize *= 0.5;
			level++;
		}

		return level;
	}

	static int  GetWLonIndex(double wlon,int level)
	{
		return GetWLonIndex(wlon,level,false);
	}
	static int  GetWLonIndex(double wlon,int level,boolean east)
	{
		double sz = GetLevelSize(level);
		double vv =  (wlon + 180) / sz;

		if(!east)
			return  (int)Math.floor(vv);

		if(Math.floor(vv) == Math.ceil(vv))
			return (int)Math.floor(vv) - 1;

		return   (int)Math.floor(vv);
	}
	static int  GetWLatIndex(double wlat, int level)
	{
		return GetWLatIndex(wlat, level,false);
	}
	static int  GetWLatIndex(double wlat, int level,boolean south)
	{
		double sz = GetLevelSize(level);

		double vv = (180 - wlat) / sz;

		if(!south)
			return  (int)Math.floor(vv);

		if(Math.floor(vv) == Math.ceil(vv))
			return (int)Math.floor(vv) - 1;
		return  (int)Math.floor(vv);
	}
	public static int   MaxLevel = 22;
}
