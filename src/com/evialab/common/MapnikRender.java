package com.evialab.common;

import java.io.File;
import java.io.FileOutputStream;
 

public class MapnikRender {

	public native  synchronized boolean init(String mapfile, String plugindir, String fontsdir);
	public native  synchronized byte[] render(int x, int y, int z);
	public native  synchronized void  release();
	public static  synchronized void loadJNI(String jnipath)
	 {
		jnipath = jnipath.trim();
		  System.out.println("load jni render from :" + jnipath);
		  
		  System.load(jnipath + java.io.File.separator+"libpng16.dll");
		  System.load(jnipath + java.io.File.separator+"cairo.dll");
		  System.load(jnipath + java.io.File.separator+"icudt53.dll");
		  System.load(jnipath + java.io.File.separator+"icuuc53.dll");
		  System.load(jnipath + java.io.File.separator+"icuin53.dll");
		  System.load(jnipath + java.io.File.separator+"libwebp.dll");
		  System.load(jnipath + java.io.File.separator+"libtiff.dll");
		  System.load(jnipath + java.io.File.separator+"mapnik.dll");
		  
		 // System.load(jnipath + java.io.File.separator+"libeay32.dll");
		//  System.load(jnipath + java.io.File.separator+"libintl.dll");
		//  System.load(jnipath + java.io.File.separator+"libpq.dll");
		  
		  System.load(jnipath + java.io.File.separator+"MapnikRender.dll");
		  System.out.println("load render jni success");
	 }
 
 
	 public static void main(String[] args) {
			
		 //这里进行�?个测�?
		 MapnikRender.loadJNI("E:\\opensource\\mapnik\\sdk\\mapnik-sdk-x64-14.0\\libs\\");
		 
		 MapnikRender render = new MapnikRender();
		 render.init("d:\\traffic.xml", "E:\\opensource\\mapnik\\sdk\\mapnik-sdk-x64-14.0\\libs\\mapnik\\input", "E:\\opensource\\mapnik\\sdk\\mapnik-sdk-x64-14.0\\libs\\mapnik\\fonts");
		 
		 byte[] data = render.render(106, 52, 7);
	
		 
		 render.release();
		 
		 
		 //保存出图
		try
		{
			String filepath = "d:\\2.png";		
			File f = new File(filepath);
			FileOutputStream fis = new FileOutputStream(f);
			fis.write(data);
			fis.close();
		}
		catch(Exception e)
		{
			
		}
     }
	 
	 private long  mapptr = 0;
	 private long  bufptr = 0;
}
