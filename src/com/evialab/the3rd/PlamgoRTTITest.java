package com.evialab.the3rd;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.evialab.the3rd.PlamgoRTTI.LinkTrafficInfo;
import com.evialab.the3rd.PlamgoRTTI.MeshTrafficInfo;
import com.evialab.the3rd.PlamgoRTTI.TrafficInfo;
 
public class PlamgoRTTITest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = "http://city.palmcity.cn/portal/main.action?user=YWHKJDTIPLUS&password=1095004bf975881d9c1ddaa7222d82103f1feead&citycode=1100&type=1&extension=";
  
		//parseData(url);

		try
		{
			File fs =new File("D:\\项目\\掌行通接口\\1100_BASE_DOWNTOWN_5.0_1960_2011_DTIPLUS_20150601.dat");
			
			FileInputStream fis = new FileInputStream(fs);
			
			parseDataStream(fis);
			
			fis.close();
		}
		catch(Exception e)
		{
			
		}
		
		
		
	}

	
	static boolean parseDataStream(InputStream in)
	{
		try {
 
			GZIPInputStream zip = new GZIPInputStream(in);
			
			TrafficInfo info = TrafficInfo.parseFrom(zip);

			System.out.println(info.getHead());
			
			int cnt = info.getMeshTrafficInfoCount();
			
		    for(int i = 0; i < cnt; i++){
		    	
		      MeshTrafficInfo mesh =	info.getMeshTrafficInfo(i);
		    
		      int meshid = mesh.getMeshid();
		      System.out.println("\tmeshid:" + meshid);
		      
		      int lcnt  =	mesh.getLinkTrafficInfoCount();
		      for(int j = 0; j < lcnt; j++)
		      {
		    	  LinkTrafficInfo link = mesh.getLinkTrafficInfo(j);
		    	  
		    	  int linkid = link.getLinkid();
		    	  System.out.println("\t\tlinkid:" + linkid);
		    	  
		    	  int traffic = link.getTrafficInfo();
		    	  
		    	  System.out.println("\t\t\ttraffic:"+ traffic);
		    	  //
		    	  int f_speed = (traffic & 0xff800000)>>23;
		    	  int f_jam = (traffic & 0x00700000)>>20;
		    	  int r_speed = (traffic & 0x0000ff80)>>7;
		    	  int r_jam = (traffic & 0x00000070)>>4;
		    	  
		    	  System.out.println("\t\t\tf_speed:" + f_speed);
		    	  System.out.println("\t\t\tf_jam:" + f_jam);
		    	  System.out.println("\t\t\tr_speed:" + r_speed);
		    	  System.out.println("\t\t\tr_jam:" + r_jam);
		      }
		    }
 
 
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	static boolean parseData(String url) {
		try {
			URL getUrl;

			getUrl = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();

			connection.connect();
 
			parseDataStream(connection.getInputStream());

			connection.disconnect();
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}
		return true;

	}

}
