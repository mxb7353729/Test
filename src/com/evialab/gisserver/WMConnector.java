/**
 * 
 */
package com.evialab.gisserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * @author Administrator
 * 
 */
public class WMConnector {

	Connection m_conn = null;
	String m_path = null;


	HashMap<String, PreparedStatement> w_pss = new HashMap<String, PreparedStatement>();

	PreparedStatement m_blocs = null;
	PreparedStatement m_mbtiles = null;
	
	public boolean connect(String path) {
		try {
			Class.forName("org.sqlite.JDBC");
			String connStr = "jdbc:sqlite:" + path;
			m_conn = DriverManager.getConnection(connStr);
			//m_ps = m_conn.prepareStatement("select T_BDATA.BDATA, T_DATA.x,T_DATA.y,T_DATA.z  from T_BDATA inner join T_DATA on T_BDATA.id = T_DATA.dataid where " +
			//		"T_DATA.x=? and T_DATA.y=? and T_DATA.z=? limit 1");

			m_path = path;

			if(path.substring(path.length() - 4).equalsIgnoreCase("edom"))
			{
				 
				m_blocs = m_conn.prepareStatement("select * from blocks where id=?");
				 
			}
			else if(path.substring(path.length() - 7).equalsIgnoreCase("mbtiles"))
			{
				
				m_mbtiles = m_conn.prepareStatement("select images.tile_data  from images inner join map on images.tile_id = map.tile_id where " +
			 		"map.tile_column=? and map.tile_row=? and map.zoom_level=? limit 1");
				 
			}
			
			return true;
		} catch (Exception e) {
			return false;
		}
		
		
	}

	public boolean isConnected() {
		return m_conn != null;
	}

	public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                         Math.min(original.length - from, newLength));
        return copy;
    }
	
	byte[] getImageFromBlocks(int x, int y , int lev)
	{
		if(m_blocs == null)
		{
		  return null;
		}
		long id = getTileID(x,y,lev);
		
		try{
			m_blocs.setLong(1, id);
			ResultSet rs = m_blocs.executeQuery();
			if (rs.next()) {
				byte[] result = rs.getBytes(1);
				byte[]  bres = copyOfRange(result, 4, result.length);
				return bres;
			}
			
			return null;
		}
		catch(Exception e)
		{
			return null;
		}
	}
	synchronized public byte[] getImage(long x, long y, long lev) {
		
		if(m_blocs!=null)
			return getImageFromBlocks((int)x,(int)y,(int)lev);
		
		
		PreparedStatement ps = null;
		
		if(m_mbtiles != null)
		{
		   ps = m_mbtiles;
		   //注意这个中数据y是相反的
		    y = (long) (Math.pow(2,lev) - y - 1);
		}
		else
		{
			String t = getTableName(x,y,lev);
			
			ps = w_pss.get(t);
			if(ps == null)
			{
				try {
					ps =  m_conn.prepareStatement("select T_BDATA.BDATA, "+t+".x,"+t+".y,"+t+".z  from T_BDATA inner join "+t+" on T_BDATA.id = "+t+".dataid where " +
							 	""+t+".x=? and "+t+".y=? and "+t+".z=? limit 1");
				} catch (SQLException e) {

					return null;
				} 
				
				w_pss.put(t, ps);
			}
		}

		if (ps == null)
			return null;
		try {
			ps.setLong(1, x);
			ps.setLong(2, y);
			ps.setLong(3, lev);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				byte[] result = rs.getBytes(1);
				return result;
			}
			return null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			return null;
		}

	}
	
	String getTableName(long x, long y, long lev)
	{
		if (lev < 16)
		{
			return "T_DATA";
		}
		else
		{
			long tx = x / 512;
			long ty = y / 512;

			String t = "T_DATA"+"_"+lev+"_512_"+tx+"_512_"+ty;
		    
			return t;
		}
		 
	}
	
	//web Mercator 坐标转换为经纬度
	public static double  webMercator2BL(double y)
	{
		double lat;
		lat = Math.atan(Math.pow(Math.E, y * Math.PI / 180 )) * 180 * 2 / Math.PI - 90;
		return lat;
	}
	//经纬度转换为web Mercator
	public static double  bl2WebMercator(double lat)
	{
		if(lat == 0)
			return 0;
		double y;
		lat = Math.min(lat, 85.0);
		lat = Math.max(lat, -85.0);
		y = Math.log(Math.tan(Math.PI / 4 + lat * Math.PI / 180 / 2)) * 180 / Math.PI;

		return y;
	}

	public static double  getLevelSize(int level)
	{
		return 360.0 / (1 << level);
	}
	public static int     getSizeLevel(double size)
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

	public static int  getWLonIndex(double wlon,int level,boolean east)
	{
		double sz = getLevelSize(level);
		double vv =  (wlon + 180) / sz;

		if(!east)
			return  (int) Math.floor(vv);

		if(Math.floor(vv) == Math.ceil(vv))
			return (int) (Math.floor(vv) - 1);

		return  (int) Math.floor(vv);
	}
	public static int  getWLatIndex(double wlat, int level,boolean south )
	{
		double sz = getLevelSize(level);

		double vv = (180 - wlat) / sz;

		if(!south)
			return  (int)Math.floor(vv);

		if(Math.floor(vv) == Math.ceil(vv))
			return (int)Math.floor(vv) - 1;
		return  (int)Math.floor(vv);
	}
	static long  getTileID(int x, int y, int level)
	{
		//System.out.println("x =" + Integer.toString(x) + 
		//		" y =" + Integer.toString(y) + " l = " + Integer.toString(level));
		//byte thisLevel = (byte)level;
		long base = (1 << 28) - 1;
		//System.out.println("base = " + Long.toHexString(base));
		long xl = x&base;
		//System.out.println("xl = " + Long.toHexString(xl));
		long yl = y;
		yl = (yl & base) << 28;
		//System.out.println("yl = " + Long.toHexString(yl));
		long ll = level;
		ll = ll << 56;
		//System.out.println("ll = " + Long.toHexString(ll));
		long idd = xl | yl | ll;
		//System.out.println("idd = " + Long.toHexString(idd));
		return idd;
		//return ( (x & base) | (y & (base << 28)) | (thisLevel << 56));

	}
}
