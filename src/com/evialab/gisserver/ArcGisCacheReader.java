package com.evialab.gisserver;
import java.io.*;
import java.io.BufferedInputStream;  

public class ArcGisCacheReader {
 	static long lpow(long base, long exp)
	{
		if(exp == 1) return base;
		if(exp == 0) return 0;
		long temp = base;
		for(long i = 0; i < (exp-1); i++)
		{
			temp *= base;
		}
		return temp;
	}
 	static byte[] blankimg = null;
	static private ArcGisCacheReader g_reader = null;
	static private String m_cachePath = "D:\\arcgisserver\\arcgiscache\\exp_china\\Layers\\_alllayers\\";
	public static ArcGisCacheReader getInstance()
	{
		if(g_reader == null) 
		{
			g_reader = new ArcGisCacheReader();
		}
		return g_reader;
	}
	public static void LoadBlankImage(String path)
	{
		//String p = path + "WebRoot\\pureopaque.png";
		File f = new File(path + "img\\pureopaque.png");
		if(f.exists())
		{
			try{
			 blankimg = new byte[(int)f.length()];
			BufferedInputStream is = new  BufferedInputStream( new FileInputStream(f));
			is.read(blankimg);
			is.close();
			}
			catch(Exception ex)
			{
				System.out.println("ArcGISCacheReader:SetTilePath:Exception:" + ex.getMessage());
			}
		}
		
	}
	public static void SetTilePath(String path)
	{
		m_cachePath = path;
	}
	public static String GetImagePath(long x, long y, long level)
	{
		String temp;
		if(level > 7)
		{
			String l = Long.toString(level);
			if(l.length() == 1) l = "0" + l;
			long lSub = lpow(2L, (level-7));//级别�?
			long diry = y / lSub;
			long dirx = x / lSub;
			temp = m_cachePath + "L" + l + "\\" + "bundle_" + 
					diry + "_" + dirx + "\\" + y + "_" + x + ".png";
			return temp;
		}
		else
		{
			String l = Long.toString(level);
			if(l.length() == 1) l = "0" + l;
			temp = m_cachePath + "L" + l + "\\"  + y + "_" + x + ".png";
			return temp;
		}
	}
	
	public static byte[] GetImage(long x, long y, long level)
	{
		String temp;
		if(level > 7)
		{
			String l = Long.toString(level);
			if(l.length() == 1) l = "0" + l;
			long lSub = lpow(2L, (level-7));//级别�?
			long diry = y / lSub;
			long dirx = x / lSub;
			temp = m_cachePath + "L" + l + "\\" + "bundle_" + 
					diry + "_" + dirx + "\\" + y + "_" + x + ".png";
		}
		else
		{
			String l = Long.toString(level);
			if(l.length() == 1) l = "0" + l;
			temp = m_cachePath + "L" + l + "\\"  + y + "_" + x + ".png";
		}
		
		try
		{
			File f = new File(temp);
			if(f.exists())
			{
				byte[] buffer = new byte[(int)f.length()];
				BufferedInputStream is = new  BufferedInputStream( new FileInputStream(f));
				is.read(buffer);
				is.close();
				
				return buffer;
			}
			else
				return blankimg;
		}
		catch(IOException ex)
		{
			System.out.println("ArcGisCacheReader: GetImage: " + ex.getMessage());
		}
		return null;
	}
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String path = ArcGisCacheReader.GetImagePath(172, 68, 8);
		System.out.println(path);
	}
	

}
