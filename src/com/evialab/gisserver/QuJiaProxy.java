package com.evialab.gisserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class QuJiaProxy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

  
	public static boolean get(long x, long y, int z, HttpServletResponse resp) {

		try {

			// 尝试从缓存加�?
			String path = CachUtil.imgPath(x, y, z);	
			String cathpath = "qujia/" + path;			
			byte[] data = CachUtil.readCach(cathpath);
			
			if (data != null) {
				resp.setContentType("image/png");

				resp.getOutputStream().write(data);

				return true;
			}
			// �?要下�?
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			String baseurl = "http://image.fundrive.com.cn/vi/";
			if (getURL(baseurl + path, null, output)) 
			{
				data = output.toByteArray(); 
				resp.setContentType("image/png");
				resp.getOutputStream().write(data);
				//记录缓存
				CachUtil.writeCach(cathpath,data);
				
				return true;
			}
			else
				return false;

		
		} catch (IOException e) {

			return false;
		}
	}
	
	public static byte[] getData(long x, long y, int z) {

			// 尝试从缓存加�?
			String path = CachUtil.imgPath(x, y, z);
			String cathpath = "qujia/" + path;			
			byte[] data = CachUtil.readCach(cathpath);

			if(data != null)
				return data;
			// �?要下�?
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			String baseurl = "http://image.fundrive.com.cn/vi/";
			if (getURL(baseurl + path, null, output)) 
			{
				data = output.toByteArray(); 
 
				//记录缓存
				CachUtil.writeCach(cathpath,data);
				
				return data;
			}
			return null; 
	}

	

	public static boolean getURL(String url, HttpServletResponse resp) {
		return getURL(url, resp, null);
	}

	public static boolean getURL(String url, HttpServletResponse resp,
			OutputStream stream) {

		// CloseableHttpClient httpclient = HttpClients.createDefault();

		HttpClientBuilder builder = HttpClients.custom();
		builder.disableContentCompression();
		CloseableHttpClient httpclient = builder.build();
		try {
			try {
				
				HttpGet httpget = new HttpGet(url);

				CloseableHttpResponse response = httpclient.execute(httpget);

				try {
					HttpEntity entity = response.getEntity();
					Header h = entity.getContentType();
					// Header en = entity.getContentEncoding();
					String t = h.getValue();
					if (entity != null) {
						InputStream instream = entity.getContent();

						try {
							if (resp != null)
								resp.setContentType(t);

							byte[] buf = new byte[1024 * 20];

							int nread = 0;

							if (stream == null && resp != null)
								stream = resp.getOutputStream();

							while ((nread = instream.read(buf)) != -1) {

								stream.write(buf, 0, nread);
							}

							return true;
						} catch (IOException ex) {

							throw ex;
						} finally {

							instream.close();
						}
					}
				} finally {
					response.close();
				}
			} finally {
				httpclient.close();
			}
		} catch (ClientProtocolException e) {

			// e.printStackTrace();

			//return false;
		} catch (IOException e) {

			// e.printStackTrace();
			//return false;
		}
		return false;

	}

}
