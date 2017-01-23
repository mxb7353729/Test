package com.evialab.gisserver;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

public class ArcGisMiniBundleReader {

	static byte[] blankimg = null;
	static private ArcGisCacheReader g_reader = null;
	static private String m_cachePath = "arcgisserver/arcgiscache/2013年云南_L9-L18(6-15)/Layers/_alllayers";
	public static void SetTilePath(String path)
	{
		m_cachePath = path;
	}
	public static void LoadBlankImage(String path)
	{
		String p = path + "WebRoot/pureopaque.png";
		File f = new File(path + "img/pureopaque.png");
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
	
	public static String GetImagePath(long x, long y, long level)
	{
		String temp;
		String l = Long.toString(level);
		if(l.length() == 1) l = "0" + l;
		
		//查找目录
		long lx = x / 128 * 128;
		long ly = y / 128 * 128;
		String strx = Long.toHexString(lx);
		while(strx.length() < 4)
		{
			strx = "0" + strx;
		}
		strx = "C" + strx;
		String stry = Long.toHexString(ly);
		while(stry.length() < 4)
		{
			stry = "0" + stry;
		}
		stry = "R" + stry;
		
		//查找文件�?
		long sx = x / 16 * 16;
		long sy = y / 16 * 16;
		String strSub = "R" + Long.toHexString(sy) + "_C" + Long.toHexString(sx);
		
		temp = m_cachePath + "L" + l + java.io.File.separator + "S_" + 
				stry  + strx + java.io.File.separator + strSub;
		return temp;

	}
	
	public static byte[] GetImage(long x, long y, long level)
	{
		
		String cathpath = "our/" + CachUtil.imgPath(x, y, (int)level);			
		byte[] data = CachUtil.readCach(cathpath);
		if(data != null)
			return data;
		
		String miniBundle = null;
		String l = Long.toString(level);
		if(l.length() == 1) l = "0" + l;
		
		//查找目录
		long lx = x / 128 * 128;
		long ly = y / 128 * 128;
		String strx = Long.toHexString(lx);
		while(strx.length() < 4)
		{
			strx = "0" + strx;
		}
		strx = "C" + strx;
		String stry = Long.toHexString(ly);
		while(stry.length() < 4)
		{
			stry = "0" + stry;
		}
		stry = "R" + stry;
		
		//查找文件�?
		long sx = x / 16 * 16;
		long sy = y / 16 * 16;
		String strSub = "R" + Long.toHexString(sy) + "_C" + Long.toHexString(sx);
		
		miniBundle = m_cachePath + "L" + l + java.io.File.separator + "S_" + 
				stry  + strx + java.io.File.separator + strSub;		
		try
		{
			File f = new File(miniBundle + ".b");
			if(!f.exists())
			{
				return blankimg;
			}
			f = new File(miniBundle + ".x");
			if(!f.exists())
			{
				System.out.println("数据完整性错误，.x文件缺失�?" + miniBundle);
				return blankimg;
			}
			
			byte[] buffer = GetOneImageFromBundle(y-sy, x-sx, miniBundle);
			
			
			//记录缓存
			CachUtil.writeCach(cathpath,buffer);
			
			return buffer;

		}
		catch(Exception ex)
		{
			System.out.println("ArcGisMiniBundleReader: GetImage: " + ex.getMessage());
		}
		return blankimg;
	}
	
	public static byte[] GetOneImageFromBundle(long r, long c, String bundle)
	{
				
		try{
			File fx = new File(bundle + ".x");
			DataInputStream indexFile = new DataInputStream(new FileInputStream(fx ));
			
			byte[]indexBuf = new byte[2048];
			indexFile.read(indexBuf);
			
			int iid = (int)(c * 16 + r);//image序号
			int index = (GetUnByte(indexBuf[iid*4]) << 24) + (GetUnByte(indexBuf[iid*4+1]) << 16) +
			(GetUnByte(indexBuf[iid*4+2]) << 8) + (GetUnByte(indexBuf[iid*4+3]));
			int len = (GetUnByte(indexBuf[1024+iid*4]) << 24) + (GetUnByte(indexBuf[1024+iid*4+1]) << 16) +
			(GetUnByte(indexBuf[1024+iid*4+2]) << 8) + (GetUnByte(indexBuf[1024+iid*4+3]));
			
			 
			if(len < 100)
			{
				indexFile.close();
				return blankimg;
			}
			File fb = new File(bundle + ".b");
			RandomAccessFile randomFile = new RandomAccessFile(fb, "r");			
			byte[] fileBuf = new byte[len];
			randomFile.seek(index);
			randomFile.read(fileBuf);

			indexFile.close();
			randomFile.close();
			return fileBuf;
		}
		catch(Exception e){
			System.out.println("ArcGisMiniBundleReader: GetOneImageFromBundle: " + e.getMessage());
		}
		return blankimg;
	}		
	public static int GetUnByte(byte b)
	{
		if(b >= 0)
			return b;
		int r= b & 0x7f + 128;
		return r;
	}
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
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//String temp = ArcGisMiniBundleReader.GetImagePath(1605, 877, 11);
		//for(int i = 0; i < 1000; i++)
		{
			byte[] temp = ArcGisMiniBundleReader.GetImage(404,221, 9);
		}

	}

}
