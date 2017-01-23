package com.evialab.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url="http://localhost:8080/siweiserver/SE_LS";
		String cityname="北京";
		String word="商场";  
		try {
			//cityname=URLEncoder.encode(cityname,"GBK");
			word=URLEncoder.encode(word,"UTF-8");
			cityname=URLEncoder.encode(cityname,"UTF-8");
			//word=URLDecoder.decode(word,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(cityname);
		System.out.println(word);
		String param="st=LocalSearch&uid=yiweihang&city="+cityname+"&words="+word;	
		String rs=test.SendGET(url,param);
		System.out.print(rs);
        
	}
	public static String SendGET(String url,String param){
		   String result="";
		   BufferedReader read=null;
		    
		   try {
		   
			
		    URL realurl=new URL(url+"?"+param);
		   
		    URLConnection connection=realurl.openConnection();
		     
		    connection.setRequestProperty("accept", "*/*");
		             connection.setRequestProperty("connection", "Keep-Alive");
		           //  connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; InfoPath.3)");
		             connection.connect();
	                  Map<String, List<String>> map = connection.getHeaderFields();
		            for (String key : map.keySet()) {
		                 System.out.println(key + "--->" + map.get(key));
		             }
		            
		             read = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
		             String line;
		             while ((line = read.readLine()) != null) {
		                 result += line;
		             }
		   } catch (IOException e) {
		    e.printStackTrace();
		   }finally{
		    if(read!=null){
		     try {
		      read.close();
		     } catch (IOException e) {
		      e.printStackTrace();
		     }
		    }
		   }
		     
		   return result; 
		 }
}
