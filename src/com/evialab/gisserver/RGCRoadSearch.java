package com.evialab.gisserver;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;
import org.json.JSONObject;
import java.io.*;




 
public class RGCRoadSearch {
	
	static Connection connPg = null;
	static PreparedStatement psPg = null;
	

    static String connStr = "";
    static Connection conn = null;
    static PreparedStatement  ps = null;	        
	
	static double R = 6378137;
	static double ELat = R * Math.PI / 180.0;
	static long level = 12;
	static double blkSize = 90.0 / Math.pow(2, level); 

	/**
	 * @param args
	 */
	
	static HashMap<String,String> MainKinds = new HashMap<String,String>();
	static HashMap<String,String> SubKinds = new HashMap<String,String>();
	
	public static void Init(String path)
	{
		try{
			connStr ="jdbc:sqlite:"  + path;
			Class.forName("org.sqlite.JDBC");
        	conn = DriverManager.getConnection(connStr);
        	 ps = conn.prepareStatement("select X,Y,Shape from roadindex where X<? and X>? and Y<? and Y>?");
		}
		catch(Exception ex)
		{
			System.out.println("RGCRoadSearch Init Exception:" + ex.getMessage());
		}
		
		
		//初始化映射关�?
		MainKinds.put("00", "高�?�路");
		MainKinds.put("01", "都市高�?�路");
		MainKinds.put("02", "国道");
		MainKinds.put("03", "省道");
		MainKinds.put("04", "县道");
		//MainKinds.put("05", "高�?�路");
		MainKinds.put("06", "乡镇村道");	
		//MainKinds.put("07", "高�?�路");
		MainKinds.put("08", "其他道路");
		MainKinds.put("09", "九级�?");
		MainKinds.put("0a", "轮渡");
		MainKinds.put("0b", "行人道路");
		 
		 
		 
		SubKinds.put("00", "环岛");
		SubKinds.put("01", "无属�?");
		SubKinds.put("02", "上下线分�?");
		SubKinds.put("03", "JCT");
		SubKinds.put("04", "交叉点内Link");
		SubKinds.put("05", "IC");
		SubKinds.put("06", "停车�?");
		SubKinds.put("07", "服务�?");
		SubKinds.put("08", "�?");
		SubKinds.put("09", "步行�?");
		SubKinds.put("0a", "辅路");
		SubKinds.put("0b", "匝道");
		SubKinds.put("0c", "全封闭道�?");
		SubKinds.put("0d", "未定义交通区�?");
		SubKinds.put("0e", "POI连接�?");
		SubKinds.put("0f", "隧道");
		SubKinds.put("11", "公交专用�?");
		SubKinds.put("12", "提前右转");
		SubKinds.put("13", "风景路线");
		SubKinds.put("14", "区域内道�?");
		SubKinds.put("15", "提前左转");
		SubKinds.put("16", "调头�?");
		SubKinds.put("17", "主辅路出入口");
		SubKinds.put("18", "停车位引导路");
		SubKinds.put("19", "虚拟链接�?");
 
		
	}
	
	public static void InitPg(String url, String user, String password)
	{
		try{
			
			
			Class.forName("org.postgresql.Driver");
			
			connPg = DriverManager.getConnection(url,user,password);
			connPg.setAutoCommit(false);
			String dstsql = "select X,Y,Shape from roadindex where X<? and X>? and Y<? and Y>?";
			psPg = connPg.prepareStatement(dstsql);
			}
			catch(Exception ex)
			{
				System.out.println("RGCRoadSearch Init Error:" + ex.getMessage());
			}
			
		
		
		//初始化映射关�?
		MainKinds.put("00", "高�?�路");
		MainKinds.put("01", "都市高�?�路");
		MainKinds.put("02", "国道");
		MainKinds.put("03", "省道");
		MainKinds.put("04", "县道");
		//MainKinds.put("05", "高�?�路");
		MainKinds.put("06", "乡镇村道");	
		//MainKinds.put("07", "高�?�路");
		MainKinds.put("08", "其他道路");
		MainKinds.put("09", "九级�?");
		MainKinds.put("0a", "轮渡");
		MainKinds.put("0b", "行人道路");
		 
		 
		 
		SubKinds.put("00", "环岛");
		SubKinds.put("01", "无属�?");
		SubKinds.put("02", "上下线分�?");
		SubKinds.put("03", "JCT");
		SubKinds.put("04", "交叉点内Link");
		SubKinds.put("05", "IC");
		SubKinds.put("06", "停车�?");
		SubKinds.put("07", "服务�?");
		SubKinds.put("08", "�?");
		SubKinds.put("09", "步行�?");
		SubKinds.put("0a", "辅路");
		SubKinds.put("0b", "匝道");
		SubKinds.put("0c", "全封闭道�?");
		SubKinds.put("0d", "未定义交通区�?");
		SubKinds.put("0e", "POI连接�?");
		SubKinds.put("0f", "隧道");
		SubKinds.put("11", "公交专用�?");
		SubKinds.put("12", "提前右转");
		SubKinds.put("13", "风景路线");
		SubKinds.put("14", "区域内道�?");
		SubKinds.put("15", "提前左转");
		SubKinds.put("16", "调头�?");
		SubKinds.put("17", "主辅路出入口");
		SubKinds.put("18", "停车位引导路");
		SubKinds.put("19", "虚拟链接�?");
 
		
	}	
	synchronized public static void SearchRoadTile(int xid, int yid, int bound, HashSet<String> ret)
	{
		try
		{
			psPg.setInt(1, (int)xid + bound);
			psPg.setInt(2, (int)xid - bound);
			psPg.setInt(3, (int)yid + bound);
			psPg.setInt(4, (int)yid - bound);
	        ResultSet rs = psPg.executeQuery();
	        while(rs.next())
	        {
	        	ret.add(rs.getString(3));
	        }
	        rs.close();

		}
		catch(Exception ex)
		{
			System.out.println( "RGCRoadSearch:SearchRoadTile: DBError:" + ex.getMessage());
			System.out.println( "x=" + xid + " y=" + yid + "b=" + bound);
		}
		
	}
		
	 public static JSONObject GetNearestRoad(double x, double y, int version) {
		// TODO Auto-generated method stub
		try
		{
			JSONObject jo = new JSONObject();
			int xid = (int)(x / blkSize);
			int yid = (int)(y / blkSize);
	        double minIndexDis = 9999999;	        
	        HashSet<String> shapes = new HashSet<String>();

	        SearchRoadTile(xid, yid, 1, shapes);
	        if(shapes.isEmpty())
	        {
	        	SearchRoadTile(xid, yid, 5, shapes);
	        }
	        if(shapes.isEmpty())
	        {
	        	SearchRoadTile(xid, yid, 15, shapes);
	        }
	        
	        
	        
	        RoadResult rr = new RoadResult();
	        for(String shape : shapes)
	        {

	        	RoadResult rt = FindNearestLineInTile(shape, x, y);
	        	if(rt.dis < minIndexDis)
	        	{
	        		minIndexDis = rt.dis;
	        		rr = rt;
	        	}
	        }
	        
	        POIInfo info = new POIInfo();
	        info.x = rr.x;
	        info.y = rr.y;
	        info.x0 = x;
	        info.y0 = y;
	        info.CreateDistAngle();
	        DecimalFormat df = new DecimalFormat("#.00000");
	        
	        
	        jo.put("name", rr.name);
	        jo.put("road_level", getMainType( rr.kind));
	        jo.put("limit_speed", rr.limit);
	        jo.put("lng", df.format(rr.x));
	        jo.put("lat", df.format(rr.y));
	        jo.put("road_address", rr.name + info.dir + Integer.toString((int)rr.dis) + "�?");
	        if(version > 0)
	        {
	        	jo.put("urban", rr.isCity);
	        	jo.put("width", GetRoadWidth(rr.width));
	        }
	        jo.put("lanenumber", laneNumber(rr.lanenumber));
	        jo.put("distance", (int)rr.dis);
	        jo.put("roadtype", getSubType( rr.kind));
	        
	        return jo;
	        //System.out.println("Name = " + rr.name + " dist = " +  rr.dis + " Kind=" + rr.kind + " Limit = " + rr.limit + " " + rr.x + "," + rr.y);
	        //long endMili=System.currentTimeMillis();
	        //System.out.println("Over " + (endMili - startMili));	        
		}
		catch(Exception ex)
		{
			//System.out.println("RGCRoadSearch:GetNearestRoad:");
			//System.out.println(ex.getMessage());
		}
		return null;

	}	
	 public static JSONObject GetNearestRoad(double x, double y, double angle, int version) {
			// TODO Auto-generated method stub
			try
			{
				JSONObject jo = new JSONObject();
				int xid = (int)(x / blkSize);
				int yid = (int)(y / blkSize);
		        double minIndexDis = 9999999;	        
		        HashSet<String> shapes = new HashSet<String>();

		        SearchRoadTile(xid, yid, 1, shapes);
		        if(shapes.isEmpty())
		        {
		        	SearchRoadTile(xid, yid, 5, shapes);
		        }
		        
		        RoadResult rr = new RoadResult();
		        for(String shape : shapes)
		        {

		        	RoadResult rt = FindNearestLineInTile(shape, x, y, angle);
		        	if(rt == null)
		        		return null;
		        	if(rt.dis < minIndexDis)
		        	{
		        		minIndexDis = rt.dis;
		        		rr = rt;
		        	}
		        }
		        
		        POIInfo info = new POIInfo();
		        info.x = rr.x;
		        info.y = rr.y;
		        info.x0 = x;
		        info.y0 = y;
		        info.CreateDistAngle();
		        DecimalFormat df = new DecimalFormat("#.00000");
		        
		        
		        jo.put("name", rr.name);
		        jo.put("road_level", getMainType( rr.kind));
		        jo.put("limit_speed", rr.limit);
		        jo.put("lng", df.format(rr.x));
		        jo.put("lat", df.format(rr.y));
		        jo.put("road_address", rr.name + info.dir + Integer.toString((int)rr.dis) + "�?");
		        if(version > 0)
		        {
		        	jo.put("urban", rr.isCity);
		        	jo.put("width", GetRoadWidth(rr.width));
		        }
		        jo.put("lanenumber", laneNumber(rr.lanenumber));
		        jo.put("distance", (int)rr.dis);
		        jo.put("roadtype", getSubType( rr.kind));
		        jo.put("linkid", rr.linkid);
		        
		        return jo;
		        //System.out.println("Name = " + rr.name + " dist = " +  rr.dis + " Kind=" + rr.kind + " Limit = " + rr.limit + " " + rr.x + "," + rr.y);
		        //long endMili=System.currentTimeMillis();
		        //System.out.println("Over " + (endMili - startMili));	        
			}
			catch(Exception ex)
			{
				//System.out.println("RGCRoadSearch:GetNearestRoad:");
				//System.out.println(ex.getMessage());
			}
			return null;

		}	 
	 
	public static String laneNumber(int ll){
		if(ll == 1)
			return "�?条车�?";
		else if(ll==2)
			return "两条或三条车�?";
		else if(ll ==3)
			return "多于四条车道";
		return "未知";
	}
	public static String GetRoadLevel(String kind)
	{
		if(kind.compareTo("00") == 0) return "高�?�路";
		if(kind.compareTo("01") == 0) return "都市高�?�路";
		if(kind.compareTo("02") == 0) return "国道";
		if(kind.compareTo("03") == 0) return "省道";
		if(kind.compareTo("04") == 0) return "县道";
		if(kind.compareTo("06") == 0) return "乡镇村道";
		if(kind.compareTo("08") == 0) return "其他道路";
		if(kind.compareTo("09") == 0) return "九级�?";
		if(kind.compareTo("0b") == 0) return "行人道路";
		return "其他道路";
	}
	
	public static String getMainType(String kind)
	{
		String maink = kind.substring(0, 2);
		
		String s = MainKinds.get(maink);
		if(s == null)
			return "其他道路";
		return s;
	}
	public static String getSubType(String kind)
	{
		String maink = kind.substring(2, 4);
		
		String s = SubKinds.get(maink);
		if(s == null)
			return "其他";
		return s;
	}
	
	public static String GetRoadWidth(String w)
	{
		if(w.compareTo("15") == 0) return "<=3�?";
		if(w.compareTo("30") == 0) return "(3米~5.5米]";
		if(w.compareTo("55") == 0) return "(5.5米~13米]";
		if(w.compareTo("130") == 0) return ">13�?";
		return "宽度未知";
	}
	
	public static void test()
	{
		try{
			//JSONObject jo = RGCRoadSearch.GetNearestRoad(116, 40);
			//for
			RGCRoadSearch.Init("D:\\四维\\roadindex-width-urban.db3");
			RGCRoadSearch.InitPg("jdbc:postgresql://127.0.0.1:5432/cttic", "postgres", "123");
			for(int i = 0; i < 10000; i++)
			{
				JSONObject jo = RGCRoadSearch.GetNearestRoad(116 - i * 0.001, 40 - i * 0.001, 0);
				//System.out.print(jo.toString().length() + ",");
				if((i % 100) == 0)
					System.out.println(jo.toString());
			}
			
		}
		catch(Exception ex)
		{	
			System.out.println(ex.getMessage());
		}		
	}
	public static void main(String[] args) {
		try{
			test();
		}
		catch(Exception ex)
		{	
			System.out.println(ex.getMessage());
		}
	}
	public static void TestFindRoadWithAngle()
	{
		RGCRoadSearch.Init("D:\\四维\\roadindex-width-urban.db3");
		System.out.println("begin");
		//for(int i = 0; i < 1000; i++)
		{
			JSONObject jo = RGCRoadSearch.GetNearestRoad(116.107344,  39.960766, 340, 1);
			System.out.println(jo.toString());
		}
		//System.out.println(jo.toString());
		System.out.println("Over");
	}
	public static void testAngle()
	{
		
		System.out.println(RGCRoadSearch.GetSegAngle(0,0,-1,-1));
		
	}
	public static void test2() {
		try{
			RGCRoadSearch.Init("E:\\data\\四维测试数据\\roadindex.db3");
			//JSONObject jo = RGCRoadSearch.GetNearestRoad(116.07552,40.29199,1);
			//JSONObject jo = RGCRoadSearch.GetNearestRoad(100.54,30.54,1);
			//JSONObject jo = RGCRoadSearch.GetNearestRoad(100.57,30.57,1);
			JSONObject jo = RGCRoadSearch.GetNearestRoad(116.488968,  40.393983, 1);
			System.out.println(jo.toString());
			//test();
		}
		catch(Exception ex)
		{	
			System.out.println(ex.getMessage());
		}
	}
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		try
//		{
//			double x = 116.42028;
//			double y = 39.91845;
//			//String shape = "校尉胡同;06;;500:116.41384,39.9131;116.41385,39.91339>校尉胡同;06;;500:116.41385,39.91339;116.41389,39.9142>校尉胡同;06;;500:116.41397,39.91404;116.41394,39.91339>校尉胡同;06;;500:116.41394,39.91339;116.41393,39.91309>校尉胡同;06;;500:116.41391,39.91284;116.41388,39.91211>校尉胡同;06;;500:116.41393,39.91309;116.41391,39.91284>校尉胡同;06;;500:116.41383,39.91285;116.41384,39.9131>校尉胡同;06;;500:116.41378,39.91211;116.41383,39.91285>校尉胡同;06;;500:116.41393,39.91546;116.41393,39.91552>校尉胡同;06;;500:116.41401,39.91552;116.41401,39.91547>校尉胡同;06;;500:116.41401,39.91547;116.41401,39.91456>校尉胡同;06;500;500:116.41393,39.91546;116.41401,39.91547>校尉胡同;06;;500:116.41378,39.91058;116.41372,39.91074;116.41372,39.91078>校尉胡同;06;;500:116.41372,39.91078;116.41378,39.91211>校尉胡同;06;;500:116.41388,39.91211;116.41385,39.91078>校尉胡同;06;;500:116.41385,39.91078;116.41385,39.91074;116.41378,39.91058>校尉胡同;06;;500:116.41401,39.91456;116.414,39.91434>校尉胡同;06;;500:116.414,39.91434;116.41398,39.91413>校尉胡同;06;;500:116.41391,39.91448;116.41392,39.91456>校尉胡同;06;;500:116.41398,39.91413;116.41397,39.91404>校尉胡同;06;;500:116.41389,39.9142;116.41391,39.91448>校尉胡同;06;;500:116.41392,39.91456;116.41392,39.91496>校尉胡同;06;;500:116.41392,39.91496;116.41393,39.91546>史家胡同;06;300;300:116.41773,39.91837;116.41784,39.91837;116.41791,39.91837>史家胡同;06;300;300:116.41791,39.91837;116.41892,39.91839>史家胡同;06;300;300:116.41892,39.91839;116.41941,39.9184;116.41989,39.91841>史家胡同;06;400;:116.42431,39.91844;116.4255,39.91845>史家胡同;06;400;:116.4255,39.91845;116.42644,39.91845>史家胡同;06;400;:116.41989,39.91841;116.42009,39.91841;116.42144,39.91842;116.42167,39.91842;116.42207,39.91842>史家胡同;06;400;:116.42207,39.91842;116.42431,39.91844";
//			File filename = new File("D:\\test.txt");
//			InputStreamReader reader = new InputStreamReader(
//					new FileInputStream(filename)); // 建立�?个输入流对象reader
//			BufferedReader br = new BufferedReader(reader); // 建立�?个对象，它把文件内容转成计算机能读懂的语�?
//			String line = "";
//			line = br.readLine();
//			String shape = line;
//			//String shape = "校尉胡同;06;;500:116.41392,39.91456;116.41392,39.91496>校尉胡同;06;;500:116.41392,39.91496;116.41393,39.91546";
//			
//			RoadResult rr = RGCRoadSearch.FindNearestLineInTile(shape, x, y);
//			System.out.println(rr.name);
//		}
//		catch(Exception ex)
//		{
//			System.out.println(ex.getMessage());
//		}
//
//	}
	
	//忽略道路角度
	static RoadResult  FindNearestLineInTile(String shape, double x, double y)
	{
		double ELon = ELat * Math.cos(y * Math.PI / 180);
		double ratio = ELon / ELat;
		String[] feats = shape.split(">");
		double mindis = 9999999;
		double xnear = 0;
		double ynear = 0;
		double limit0 = 0;
		double limit1 = 0;
		String kind = "";
		String Name = "";
		String iscity = "";
		String width = "";
		int  laneNum = 1;
		
		for(int i = 0; i < feats.length; i++)
		{
			try
			{
			String prop = feats[i].substring(0, feats[i].indexOf(':'));
			if(prop.endsWith(";"))
			{
				prop = prop + "9999";
			}

			String[]props = prop.split(";");
			String tempName = props[0];
			//System.out.println(tempName);
			String strKind = props[1];
			String strLimit0 = props[2];
			String strLimit1 = props[3];
			String strIsCity = "";
			String strWidth = "";
			if(props.length > 4)
				strIsCity = props[4];
			if(props.length > 5)
				strWidth = props[5];
			String strLaneNum = "1";
			
			if(props.length > 6)
				strLaneNum = props[6];
			
			
			String value = feats[i].substring(feats[i].indexOf(':') + 1);
			String[] parts = value.split("[+]");
			
			for(int j = 0; j < parts.length; j++)
			{
				String[]pts = parts[j].split(";");
				double []xs = new double[pts.length];
				double []ys = new double[pts.length];
				if(pts.length < 2)
				{
					//System.out.println("RGCRoadSearch FindNearestLineInTile Exception:" + "Pt Error");
					//System.out.println("i = " + i + ",x=" + x + ",y=" + y);
				}
				else
				{
					for(int m = 0; m < pts.length; m++)
					{
						String[]pt = pts[m].split(",");
						if(pt.length != 2)
						{
							//System.out.println("Error pt");
						}
						else
						{
							xs[m] = Double.parseDouble(pt[0]) * ratio * ELat;
							ys[m] = Double.parseDouble(pt[1]) * ELat;
						}
					}
					for(int m = 0; m < pts.length -1; m++)
					{
						//double dis = PointToSegDist(x, y, xs[m], ys[m], xs[m+1], ys[m+1]);
						HashMap<String, Double> result = PointToSegDist2(
								x * ratio * ELat, y * ELat, xs[m], ys[m], xs[m+1], ys[m+1]);						
						if(result.get("dist") < mindis)
						{
							xnear = result.get("x") / (ratio * ELat);
							ynear = result.get("y") / ELat;
							if(strLimit0.length() == 0)
							{
								limit0 = 9999;
							}
							else
							{
								limit0 = Double.parseDouble(strLimit0);
							}
							if(strLimit1.length() == 0)
							{
								limit1 = 9999;
							}
							else
							{
								limit1 = Double.parseDouble(strLimit1);
							}
							kind = strKind;
							Name = tempName;
							mindis = result.get("dist");
							iscity = strIsCity;
							width = strWidth;
							laneNum = Integer.parseInt(strLaneNum);
							//System.out.println("name = " + Name);
						}
					}
				}
			}
			}
			catch(Exception ex)
			{
				//System.out.println("RGCRoadSearch FindNearestLineInTile Exception:" + ex.getMessage());
				//System.out.println("i = " + i + ",x=" + x + ",y=" + y);
				//System.out.println("shape=" + shape);
			}
		}
		
		RoadResult rr = new RoadResult();
		rr.dis = mindis;
		rr.name = Name;
		rr.x = xnear;
		rr.y = ynear;
		rr.kind = kind;
		rr.isCity = iscity;
		rr.width = width;
		rr.lanenumber = laneNum;
		double l = Math.min(limit0, limit1)/10.0;
		if(l > 990) l = 0;
		rr.limit = Double.toString(l);
		return rr;
	}
	//结合道路角度
	static RoadResult  FindNearestLineInTile(String shape, double x, double y, double angle)
	{
		double ELon = ELat * Math.cos(y * Math.PI / 180);
		double ratio = ELon / ELat;
		String[] feats = shape.split(">");
		double mindis = 9999999;

		Set<RoadResult> rrs = new HashSet<RoadResult>();	
		
		for(int i = 0; i < feats.length; i++)
		{
			try
			{
			String prop = feats[i].substring(0, feats[i].indexOf(':'));
			if(prop.endsWith(";"))
			{
				prop = prop + "9999";
			}

			String[]props = prop.split(";");
			String tempName = props[0];
			//System.out.println(tempName);
			String strKind = props[1];
			String strLimit0 = props[2];
			String strLimit1 = props[3];
			String strIsCity = "";
			String strWidth = "";
			if(props.length > 4)
				strIsCity = props[4];
			if(props.length > 5)
				strWidth = props[5];
			String strLaneNum = "1";
			
			if(props.length > 6)
				strLaneNum = props[6];
			
			String strDirection = "0";
			int direction = 0;
			if(props.length > 7)
			{
				strDirection = props[7];
				direction = Integer.parseInt(strDirection);
			}
			String strlinkid = "";
			if(props.length > 8)
			{	
				strlinkid = props[8];
			}
			
			String value = feats[i].substring(feats[i].indexOf(':') + 1);
			String[] parts = value.split("[+]");
			
			for(int j = 0; j < parts.length; j++)
			{
				String[]pts = parts[j].split(";");
				double []xs = new double[pts.length];
				double []ys = new double[pts.length];
				if(pts.length < 2)
				{
					//System.out.println("RGCRoadSearch FindNearestLineInTile Exception:" + "Pt Error");
					//System.out.println("i = " + i + ",x=" + x + ",y=" + y);
				}
				else
				{
					for(int m = 0; m < pts.length; m++)
					{
						String[]pt = pts[m].split(",");
						if(pt.length != 2)
						{
							//System.out.println("Error pt");
						}
						else
						{
							xs[m] = Double.parseDouble(pt[0]) * ratio * ELat;
							ys[m] = Double.parseDouble(pt[1]) * ELat;
						}
					}
					for(int m = 0; m < pts.length -1; m++)
					{
						//double dis = PointToSegDist(x, y, xs[m], ys[m], xs[m+1], ys[m+1]);
						HashMap<String, Double> result = PointToSegDist2(
								x * ratio * ELat, y * ELat, xs[m], ys[m], xs[m+1], ys[m+1]);
						RoadResult rrtemp = new RoadResult();
						
						rrtemp.angle = 360 - GetSegAngle(xs[m], ys[m], xs[m+1], ys[m+1]) + 90;
						if(rrtemp.angle >= 360) rrtemp.angle -= 360; 
						rrtemp.angle = GetMinIntersectAngle(angle, rrtemp.angle,direction );
						if(rrtemp.angle < 30)
						{
							rrtemp.dis = result.get("dist");
							rrtemp.direction = direction;							
							
							if(rrtemp.dis  < 100)
							{
								rrtemp.x = result.get("x") / (ratio * ELat);
								rrtemp.y = result.get("y") / ELat;
								if(strLimit0.length() == 0)
								{
									rrtemp.limit0 = 9999;
								}
								else
								{
									rrtemp.limit0 = Double.parseDouble(strLimit0);
								}
								if(strLimit1.length() == 0)
								{
									rrtemp.limit1 = 9999;
								}
								else
								{
									rrtemp.limit1 = Double.parseDouble(strLimit1);
								}
								//rrtemp.limit = min(rrtemp.limit0, rrtemp.limit1);
								rrtemp.kind = strKind;
								rrtemp.name = tempName;
								rrtemp.isCity = strIsCity;
								rrtemp.width = strWidth;
								rrtemp.lanenumber = Integer.parseInt(strLaneNum);
								rrtemp.linkid = strlinkid;
								rrs.add(rrtemp);
							}
						}
					}
				}
			}
			}
			catch(Exception ex)
			{
				//System.out.println("RGCRoadSearch FindNearestLineInTile Exception:" + ex.getMessage());
				//System.out.println("i = " + i + ",x=" + x + ",y=" + y);
				//System.out.println("shape=" + shape);
			}
		}
		
		if(rrs.size()==0)
			return null;
		mindis = 100;
		RoadResult rrmin = new RoadResult();
		for(RoadResult rr : rrs)
		{
			if(rr.dis < mindis)
			{
				mindis = rr.dis;
				rrmin = rr;
			}
		}

		double l = Math.min(rrmin.limit0, rrmin.limit1)/10.0;
		if(l > 990) l = 0;
		rrmin.limit = Double.toString(l);
		return rrmin;
	}	
	public static double PointToSegDist(double x, double y, double x1, double y1, double x2, double y2)
	{
		double cross = (x2 - x1) * (x - x1) + (y2 - y1) * (y - y1);
		if (cross <= 0) return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
	
		double d2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		if (cross >= d2) return Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2));
		 
		double r = cross / d2;
		double px = x1 + (x2 - x1) * r;
		double py = y1 + (y2 - y1) * r;
		return Math.sqrt((x - px) * (x - px) + (py - y) * (py - y));
	}
	public static HashMap<String, Double> PointToSegDist2(double x, double y, double x1, double y1, double x2, double y2)
	{
		HashMap<String, Double> result = new HashMap<String, Double>();
		
		double cross = (x2 - x1) * (x - x1) + (y2 - y1) * (y - y1);
		if (cross <= 0) 
		{
			result.put("dist", Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1)));
			result.put("x", x1);
			result.put("y", y1);
			return result;
		}
	
		double d2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
		if (cross >= d2)
		{
			result.put("dist", Math.sqrt((x - x2) * (x - x2) + (y - y2) * (y - y2)));
			result.put("x", x2);
			result.put("y", y2);
			return result;
		}
		 
		double r = cross / d2;
		double px = x1 + (x2 - x1) * r;
		double py = y1 + (y2 - y1) * r;
		result.put("dist", Math.sqrt((x - px) * (x - px) + (py - y) * (py - y)));
		result.put("x", px);
		result.put("y", py);
		return result;
	}
	
	public static double GetSegAngle(double x1, double y1, double x2, double y2)
	{
		double dy = y2-y1;
		double dx = x2-x1;
		double angle = Math.atan2(dy, dx) * 180 / Math.PI;
		if(angle < 0)
			angle += 360;
		return angle;
	}
	
	public static double GetMinIntersectAngle(double autoAngle, double roadAngle, int direction)
	{
		double sangle = 0;
		//逆向道路
		if(direction == 3)
		{
			roadAngle += 180;
			if(roadAngle >= 360) roadAngle-=360;
		}
		sangle = Math.abs(autoAngle-roadAngle);
		if(sangle > 180) sangle =360-sangle;
		if(direction < 2)
		{
			if(sangle > 90) sangle = 180-sangle;
		}
		
		return sangle;
	}

}
