package com.evialab.gisserver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONObject;
import java.sql.Statement;

class poistr
{
    public double x;
    public double y;
    public int id;
    public String name;
    public poistr(double _x, double _y, int _id, String _name)
    {
        x = _x;
        y = _y;
        id = _id;
        name = _name;
    }
    public poistr()
    {
    
    }
}

class poi_struct
{
    public int xi;
    public int yi;
    public double x;
    public double y;
    public long id;
    public double dis;
    poi_struct(int _xi, int _yi, double _x, double _y, long _id)
    {
        xi = _xi;
        yi = _yi;
        x = _x;
        y = _y;
        id = _id;
        dis = 0.0;
    }
    poi_struct()
    {
    
    }
}

public class RGCPoiSearch {
	static String connStr = "";
	static Connection conn = null;
	static long level = 14;
	static double blkSize = 90.0 / Math.pow(2.0, level);
	static LinkedList<Connection> m_conns = new LinkedList<Connection>();
	static int errno = 0;
	static Hashtable<Long, String> poibuffer = new Hashtable<Long, String>();


	static Connection connPostgis = null;
	static PreparedStatement stmt  = null;
	static PreparedStatement stmtAddress = null;
	
	//连接数据�?

	public static void InitPostgis(String url, String user, String password)
	{
		try
		{
		//String url = "jdbc:postgresql://127.0.0.1:5432/cttic";
		//String user = "postgres";
		//String password = "123";
		Class.forName("org.postgresql.Driver");
		
		connPostgis = DriverManager.getConnection(url,user,password);
		connPostgis.setAutoCommit(false);
		String dstsql = "SELECT * ,st_distance(geom,ST_Point(?,?)) as dis   from poi where ST_Contains(st_makebox2d(ST_Point(?,?),ST_Point(?,?)), geom) order by dis limit 1";
		stmt = connPostgis.prepareStatement(dstsql);
		dstsql = "SELECT * ,st_distance(geom,ST_Point(?,?)) as dis   from poi where ST_Contains(st_makebox2d(ST_Point(?,?),ST_Point(?,?)), geom) order by dis limit 100";
		stmtAddress = connPostgis.prepareStatement(dstsql);
		}
		catch(Exception ex)
		{
			System.out.println("RGCPoiSearch Init Error:" + ex.getMessage());
		}
	}
	
	public static void Init(String path)
	{
		if(conn != null)
			return;
		try{
			//Class.forName("org.sqlite.JDBC");
			//path = "E:/四维/2 数据/本地搜索处理/poigrid.db3";
			//path = "d:/四维/poigrid.db3";
			Class.forName("org.sqlite.JDBC");
			connStr = "jdbc:sqlite:"  + path;
			
			//String connStr = "jdbc:sqlite:D:\\roadindex.db3";
			conn = DriverManager.getConnection(connStr);
			//m_conns.add(DriverManager.getConnection(connStr));
			//m_conns.add(DriverManager.getConnection(connStr1));
			//m_conns.add(DriverManager.getConnection(connStr2));
			//for(int i = 0; i < 6; i++)
			{				
			//	m_conns.add(DriverManager.getConnection(connStr));
			}
		}
		catch(Exception ex)
		{
			System.out.println("RGCPoiSearch Init Exception:" + ex.getMessage());
		}
		
	}

	synchronized Connection getConn()
	{
		if(m_conns.size()>0)
			return m_conns.removeFirst();
		return null;
	}
	synchronized void addConn(Connection conn)
	{
		m_conns.add(conn);
	}	
	synchronized static private JSONObject SearchAddress(double x, double y, int delta) throws Exception
    {
    	try
    	{
    	long tick0 = System.currentTimeMillis();
        HashSet<poi_struct> poilist = new HashSet<poi_struct>();
        
        int xi = (int)(x / blkSize);
        int yi = (int)(y / blkSize);
         
    	Statement st = conn.createStatement();
    	ResultSet rs = st.executeQuery("SELECT ids FROM poigrid WHERE x>"
    			+ (xi-delta-1) + " AND x<" + (xi+delta+1) + " AND y>" 
    			+ (yi-delta-1) + " AND y<" + (yi+delta+1));

    	//long tick1 = System.currentTimeMillis();
    	//System.out.println("tick1:" + (tick1-tick0));
        while (rs.next())
        {
        	//long tick11 = System.currentTimeMillis();
        	//System.out.println("tick11:" + (tick11-tick1));
            String ids = rs.getString(1);
            String[] idsArr = ids.split(";");
            if (idsArr.length > 1)
            {
                for (int m = 1; m < idsArr.length; m++)
                {
                    poi_struct ps = new poi_struct();
                    String[] temp = idsArr[m].split(",");
                    ps.id = Long.parseLong(temp[0]);
                    ps.x  = Double.parseDouble(temp[1]);
                    ps.y = Double.parseDouble(temp[2]);
                    ps.dis = (x - ps.x) * (x - ps.x) + (y - ps.y) * (y - ps.y);
                    poilist.add(ps);
                }
            }
        }
        rs.close();
        st.close();
       // long tick2 = System.currentTimeMillis();
        //System.out.println("tick2:" + (tick2-tick1));

       
        poi_struct nowpoi = new poi_struct();
        Vector<poi_struct> vector = new Vector<poi_struct>();//距离�?近的10个poi
        int maxStackSize = 100;	//�?多保�?100�?
        if (!poilist.isEmpty())
        {
        	Iterator<poi_struct> it = poilist.iterator();
            while (it.hasNext())
            {
                poi_struct ps = (poi_struct)it.next();                
                if(vector.isEmpty())
                {
                	vector.add(ps);
                	continue;
                }
                
            	for(int i = 0; i < vector.size(); i++)
            	{
            		if(ps.dis < vector.get(i).dis)
            		{
            			vector.add(i, ps);
            			if(vector.size() > maxStackSize)
            				vector.removeElementAt(vector.size() - 1);
            			break;
            		}
            		if( (i == vector.size() - 1) && (vector.size() < maxStackSize))
            		{
            			vector.add(ps);
            		}
            	}
            }

            JSONObject jo = new JSONObject();
            Statement subcmd = conn.createStatement();
            String sql = "SELECT name,address,number,x,y,id FROM poilist WHERE id IN(";
            for(int i = 0; i < vector.size(); i++)
            {
            	if(i > 0) sql += ",";
            	sql += vector.get(i).id;
            }
            sql += ")";
            
            ResultSet subrs = subcmd.executeQuery(sql);
            
            //long tick4 = System.currentTimeMillis();
            //System.out.println("tick4:" + (tick4-tick3));
            POIInfo poiresult = new POIInfo();
            while (subrs.next())
            {
            	POIInfo poi = new POIInfo();
    			poi.x0 = x;
    			poi.y0 = y;
    			poi.x = subrs.getDouble(4);// nowpoi.x;
    			poi.y = subrs.getDouble(5);// nowpoi.y;
    			poi.name = subrs.getString(1);
    			poi.address = subrs.getString(2);
    			poi.number = subrs.getString(3);
    			poi.CreateDistAngle();
    			
    			if(poiresult.name.length() == 0)
    			{
    				poiresult = poi;
    				continue;
    			}
    			//都有地址或�?�都没有地址，谁近谁优先
    			if(( (poi.address.length() > 0) && (poiresult.address.length() > 0))
    				|| ( (poi.address.length() == 0) && (poiresult.address.length() == 0)))
    			{
    				if(poi.dist < poiresult.dist)
    					poiresult = poi;
    				continue;
    			}
    			//有名字的替换无名�?
    			if((poiresult.address.length() == 0) && (poi.address.length() > 0))
    			{
    				poiresult = poi;
    				continue;
    			}
    	    }
	        jo.put("address", poiresult.name + poiresult.dir + Integer.toString((int)poiresult.dist) + "�?");
	        jo.put("name", poiresult.name);
	        jo.put("number", poiresult.address);
	        jo.put("lng", poiresult.x);
	        jo.put("lat", poiresult.y);
	        subrs.close();
            subcmd.close();
            //long tick5 = System.currentTimeMillis();
            //System.out.println("tick5:" + (tick5-tick4));
            //this.addConn(conn);
            return jo;
            //txtLog.Text =  txtLog.Text + "\r\n" + (nowpoi.name);
        }
        else
        {   //this.addConn(conn);
            return null;
        }    	
    	}
    	catch(Exception ex)
    	{
    		System.out.println( "RGCPoiSearch:SearchDelta:" + ex.getMessage());
    	}
    	return null;
    }
	synchronized static private JSONObject SearchPOI(double x, double y, int delta) throws Exception
    {
    	try
    	{
    	long tick0 = System.currentTimeMillis();
        HashSet<poi_struct> poilist = new HashSet<poi_struct>();
        int xi = (int)(x / blkSize);
        int yi = (int)(y / blkSize);
         
    	Statement st = conn.createStatement();
    	ResultSet rs = st.executeQuery("SELECT ids FROM poigrid WHERE x>"
    			+ (xi-delta-1) + " AND x<" + (xi+delta+1) + " AND y>" 
    			+ (yi-delta-1) + " AND y<" + (yi+delta+1));

    	//long tick1 = System.currentTimeMillis();
    	//System.out.println("tick1:" + (tick1-tick0));
        while (rs.next())
        {
        	//long tick11 = System.currentTimeMillis();
        	//System.out.println("tick11:" + (tick11-tick1));
            String ids = rs.getString(1);
            String[] idsArr = ids.split(";");
            if (idsArr.length > 1)
            {
                for (int m = 1; m < idsArr.length; m++)
                {
                    poi_struct ps = new poi_struct();
                    String[] temp = idsArr[m].split(",");
                    ps.id = Long.parseLong(temp[0]);
                    ps.x  = Double.parseDouble(temp[1]);
                    ps.y = Double.parseDouble(temp[2]);
                    poilist.add(ps);
                }
            }
        }
        rs.close();
        st.close();
       // long tick2 = System.currentTimeMillis();
        //System.out.println("tick2:" + (tick2-tick1));

        poi_struct nowpoi = new poi_struct();
        if (!poilist.isEmpty())
        {
            Iterator it = poilist.iterator();
            double dist = 99999999;
            while (it.hasNext())
            {
                poi_struct ps = (poi_struct)it.next();
                double nowdist = (x - ps.x) * (x - ps.x) + (y - ps.y) * (y - ps.y);
                if (nowdist < dist)
                {
                    dist = nowdist;
                    nowpoi = ps;
                }
            }
            //long tick3 = System.currentTimeMillis();
            //System.out.println("tick3:" + (tick3-tick2));
            //System.out.println(nowpoi.id);
            JSONObject jo = new JSONObject();
            Statement subcmd = conn.createStatement();
            ResultSet subrs = subcmd.executeQuery("SELECT name,address,number,x,y,id FROM poilist WHERE id="+ nowpoi.id);
            
            //long tick4 = System.currentTimeMillis();
            //System.out.println("tick4:" + (tick4-tick3));
            if (subrs.next())
            {
    	        
    			POIInfo poi = new POIInfo();
    			poi.x0 = x;
    			poi.y0 = y;
    			poi.x = nowpoi.x;
    			poi.y = nowpoi.y;
    			poi.name = subrs.getString(1);
    			poi.address = subrs.getString(2);
    			poi.CreateDistAngle();
    	        jo.put("address", poi.name + poi.dir + Integer.toString((int)poi.dist) + "�?");
    	        jo.put("name", poi.name);
    	        jo.put("number", poi.address);
    	        jo.put("lng", poi.x);
    	        jo.put("lat", poi.y);
    	        
    	    }
            subrs.close();
            subcmd.close();
            //long tick5 = System.currentTimeMillis();
            //System.out.println("tick5:" + (tick5-tick4));
            //this.addConn(conn);
            return jo;
            //txtLog.Text =  txtLog.Text + "\r\n" + (nowpoi.name);
        }
        else
        {   //this.addConn(conn);
        	JSONObject jo = new JSONObject();
	        jo.put("address", "未发现poi");
	        jo.put("name", "无POI");
	        jo.put("number", "");
	        jo.put("lng", -9999);
	        jo.put("lat", -9999);
            return jo;
        }    	
    	}
    	catch(Exception ex)
    	{
    		System.out.println( "RGCPoiSearch:SearchDelta:" + ex.getMessage());
    	}
    	return null;
    }
	

	synchronized static private JSONObject SearchPoiPostgis(double x, double y, double delta)
	{
		try
		{

			stmt.setDouble(1, x );
			stmt.setDouble(2, y );
			stmt.setDouble(3, x - delta);
			stmt.setDouble(4, y - delta);
			stmt.setDouble(5, x + delta);
			stmt.setDouble(6, y + delta);
			
			ResultSet rs=stmt.executeQuery();
			JSONObject jo = new JSONObject();
	        jo.put("address", "未发现poi");
	        jo.put("name", "无POI");
	        jo.put("number", "");
	        jo.put("lng", -9999);
	        jo.put("lat", -9999);
			if(rs.next())
			{
	   			POIInfo poi = new POIInfo();
    			poi.x0 = x;
    			poi.y0 = y;
    			poi.x = rs.getDouble("X");
    			poi.y = rs.getDouble("Y");
    			poi.name = rs.getString("Name");
    			poi.address = rs.getString("Address");
    			poi.CreateDistAngle();
    	        jo.put("address", poi.name + poi.dir + Integer.toString((int)poi.dist) + "�?");
    	        jo.put("name", poi.name);
    	        jo.put("number", poi.address);
    	        if(poi.address == null) jo.put("number", "");
    	        jo.put("lng", poi.x);
    	        jo.put("lat", poi.y);
			}
			rs.close();
			return jo;
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return null;
		
	}	
	

	synchronized static public String SearchPoiPostgis2011(double x, double y, double delta)
	{
		try
		{
			long t0=System.currentTimeMillis();
			stmt.setDouble(1, x );
			stmt.setDouble(2, y );
			stmt.setDouble(3, x - delta);
			stmt.setDouble(4, y - delta);
			stmt.setDouble(5, x + delta);
			stmt.setDouble(6, y + delta);
			
			ResultSet rs=stmt.executeQuery();
			JSONObject jo = new JSONObject();
	        jo.put("address", "未发现poi");
	        jo.put("name", "无POI");
	        jo.put("number", "");
	        jo.put("lng", -9999);
	        jo.put("lat", -9999);
			if(rs.next())
			{
	   			POIInfo poi = new POIInfo();
    			poi.x0 = x;
    			poi.y0 = y;
    			poi.x = rs.getDouble("X");
    			poi.y = rs.getDouble("Y");
    			poi.name = rs.getString("Name");
    			poi.address = rs.getString("Address");
    			poi.CreateDistAngle();
    			
    			
    	        jo.put("address", poi.name + poi.dir + Integer.toString((int)poi.dist) + "�?");
    	        jo.put("name", poi.name);
    	        jo.put("number", poi.address);
    	        if(poi.address == null) jo.put("number", "");
    	        jo.put("lng", poi.x);
    	        jo.put("lat", poi.y);
    	        
    			JSONObject jadmin = RGCAdminSearch.getRGCAdmin(x, y);
    	        //JSONObject jadmin = new JSONObject();
    	        //jadmin.put("district", "110111000");
    	        //jadmin.put("district_text", "北京�?>北京�?>房山�?");


    			long t1 = System.currentTimeMillis();
    			
    			org.dom4j.Document doc = DocumentHelper.createDocument();
    			Element result = DocumentHelper.createElement("Result");
    			result.addAttribute("time", Long.toString(t1-t0));
    			result.addAttribute("error", "0");
    			Element add = result.addElement("Add");
    			//if(poi.address.length() > 0)
    			if(poi.address != null)
    			{
    				add.addAttribute("v", poi.name + "(" + poi.address + ")" + poi.dir + Integer.toString((int)poi.dist) + "�?");
    			}
    			else
    			{
    				add.addAttribute("v", poi.name  + poi.dir + Integer.toString((int)poi.dist) + "�?");
    			}
    			Element dis = result.addElement("Dis");
    			dis.addAttribute("id", jadmin.getString("district"));
    			dis.addAttribute("name", jadmin.getString("district_text"));
    			Element Poi = result.addElement("Poi");
    			Poi.addAttribute("name", poi.name);
    			Element geo = result.addElement("Geo");
    			geo.addAttribute("lo", Double.toString(poi.x));
    			geo.addAttribute("la", Double.toString(poi.y));
    			doc.add(result);
    			doc.setXMLEncoding("utf-8");	        
    	        return doc.asXML();    	        
			}
			rs.close();
			return "";
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return null;
		
	}	
		
	synchronized static private JSONObject SearchAddressPostgis(double x, double y, double delta)
	{
		try
		{
			stmtAddress.setDouble(1, x );
			stmtAddress.setDouble(2, y );
			stmtAddress.setDouble(3, x - delta);
			stmtAddress.setDouble(4, y - delta);
			stmtAddress.setDouble(5, x + delta);
			stmtAddress.setDouble(6, y + delta);
			
			ResultSet rs=stmtAddress.executeQuery();
			JSONObject jo = new JSONObject();
	        jo.put("address", "");
	        jo.put("name", "");
	        jo.put("number", "");
	        jo.put("lng", -9999);
	        jo.put("lat", -9999);
	        POIInfo poi = new POIInfo();
	        if(rs.next())
	        {
	   			poi.x0 = x;
    			poi.y0 = y;
    			poi.x = rs.getDouble("X");
    			poi.y = rs.getDouble("Y");
    			poi.name = rs.getString("Name");
    			poi.address = rs.getString("Address");	        	
	        }
	        if(poi.address == null)
	        {
				while(rs.next())
				{
		   			if(rs.getString("Address") != null)
		   			{
			   			poi.x0 = x;
		    			poi.y0 = y;
		    			poi.x = rs.getDouble("X");
		    			poi.y = rs.getDouble("Y");
		    			poi.name = rs.getString("Name");
		    			poi.address = rs.getString("Address");	  
		    			break;
		   			}
				}
	        }
	        
	        if(poi.name != "")
	        {
	 			poi.CreateDistAngle();
		        jo.put("address", poi.name + poi.dir + Integer.toString((int)poi.dist) + "�?");
		        jo.put("name", poi.name);
		        jo.put("number", poi.address);
		        if(poi.address == null) jo.put("number", "");
		        jo.put("lng", poi.x);
		        jo.put("lat", poi.y);
				rs.close();
				return jo;
	        }
	        else
	        {
		        jo.put("address", "未发现poi");
		        jo.put("name", "无POI");
		        jo.put("number", "");
		        jo.put("lng", -9999);
		        jo.put("lat", -9999);
		        return jo;
	        }
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		return null;
		
	}			
    public static  JSONObject Search(double x, double y)
    {
    	try{
    		JSONObject jo = SearchPoiPostgis(x,y,0.01);
    		//if(jo.get)
    		if(jo.get("name") == "")
    		{
    			jo = SearchPoiPostgis(x,y,0.03);
    		}
    		return jo;
    	}
    	catch(Exception ex)
    	{
    		System.out.println(ex.getMessage());
    	}
        
        return null;
    }
    public static  JSONObject SearchAddress(double x, double y)
    {
    	try{
    		JSONObject jo = SearchAddressPostgis(x,y,0.01);
    		//if(jo.get)
    		try
    		{
    		if(!jo.has("number"))
    		{
    			jo = SearchAddressPostgis(x,y,0.03);
    		}
    		else if(jo.get("number") == null)
    		{
    			jo = SearchAddressPostgis(x,y,0.03);
    		}
    		return jo;
    		}
    		catch(Exception test)
    		{
    			System.out.println(test.getMessage());
    		}
    	}
    	catch(Exception ex)
    	{
    		System.out.println(ex.getMessage());
    	}
        
        return null;
    }	

	public static void main(String[] args) {
		//TestSQLiteSearch();
		TestOldPOISearch();
		//TestPostGisSearch();
	}
	
	public static void TestOldPOISearch()
	{
		RGCPoiSearch.InitPostgis("jdbc:postgresql://127.0.0.1:5432/cttic", "postgres", "123");
		RGCAdminSearch.Init("D:\\四维\\roadindex.db3");
		for(int j = 0; j  < 10; j++)
		for(int i = 0; i < 10000; i++)
		{
			String xmlresult = SearchPoiPostgis2011(116, 40 - i * 0.001, 0.01);
			if(i %100 == 0)
				System.out.println("i = " + i + "--" + xmlresult.length());
		}
	}
	public static void TestSQLiteSearch()
	{
		// TODO Auto-generated method stub
		RGCPoiSearch r = new RGCPoiSearch();
		///RGCPoiSearch.Init("d:\\四维\\poigrid.db3");
		RGCPoiSearch.InitPostgis("jdbc:postgresql://127.0.0.1:5432/cttic", "postgres", "123");
		//RGCPoiSearch.Init("b:\\poigrid.db3");
		long tick0 = System.currentTimeMillis();
		
		for(int j = 0; j < 100; j++)
		for(int i = 0;i< 100000; i++)
		{
			JSONObject jo = r.Search(116.33074, 040.05599-0.0001*(i));
			if(i%100==0)
			{
				if(jo != null)
					System.out.println(i + "," + jo.toString());
				else
					System.out.println(i + "," + "null");
			}
		}

		long tick1 = System.currentTimeMillis();
		System.out.println("Over:" + (tick1-tick0));
	}
	public static void TestPostGisSearch()
	{
		long tick0 = System.currentTimeMillis();
		try
		{
		String url = "jdbc:postgresql://127.0.0.1:5432/cttic";
		String user = "postgres";
		String password = "123";
		Class.forName("org.postgresql.Driver");
		Connection conn = null;
		PreparedStatement stmt  = null;
		
		//连接数据�?
		conn = DriverManager.getConnection(url,user,password);
		conn.setAutoCommit(false);
		//String dstsql = "SELECT * ,st_distance(geom,ST_Point(?,?)) as dis   from poi where ST_Contains(st_makebox2d(ST_Point(?,?),ST_Point(?,?)), geom) order by dis limit 1";
		String dstsql = "SELECT * ,st_distance(geom,ST_Point(?,?)) as dis   from poi where st_point_inside_circle(geom, ?, ?, 0.01) order by dis limit 1";
		stmt = conn.prepareStatement(dstsql);
		
		double x0 = 116.33074;
		double y0 = 40.05599;
		for(int i = 0; i < 10000; i++)
		{
			double x = x0-i*0.0001;
			double y = y0-i*0.0001;
			
			stmt.setDouble(1, x );
			stmt.setDouble(2, y );
			stmt.setDouble(3, x);
			stmt.setDouble(4, y);
			//stmt.setDouble(3, x - 0.01000);
			//stmt.setDouble(4, y - 0.01000);
			//stmt.setDouble(5, x + 0.01000);
			//stmt.setDouble(6, y + 0.01000);
			
			ResultSet rs=stmt.executeQuery();
			String address = "";
			if(rs.next())
			{
				if((i%100) == 0)
					System.out.println(i + "," + rs.getString("Name"));
			}
			//while(rs.next())
			{
			//	if(rs.getString("Address") != null)
			//	{
			//		address = rs.getString("Address");
			//		break;
			//	}
			}
			
			rs.close();
		}

		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		long tick1 = System.currentTimeMillis();
		System.out.println("Over:" + (tick1-tick0));
		
	}	

}
