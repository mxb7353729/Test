package com.evialab.the3rd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import sun.misc.BASE64Decoder;

public class Traffic extends TimerTask{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String url = "jdbc:postgresql://127.0.0.1:5432/jiaxing";
		String user = "postgres";
		String password = "123";
		
		Traffic tu = new Traffic(url,user, password);
		
	
	}
	
	Connection conn = null;
	PreparedStatement stmt  = null;
	private Timer timer = null;
	
	public Traffic(String dburl, String dbuser, String dbpass)
	{
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("postgresql");
			
			//连接数据�?
			conn = DriverManager.getConnection(dburl,dbuser,dbpass);
			conn.setAutoCommit(false);
			
			String dstsql = "update rtic set status = ?  where mapid= ? and rtickind=? and rticid=?";
			stmt = conn.prepareStatement(dstsql);
			
			timer = new Timer();
			//设置定时器，启动更新
			timer.schedule(this, 3000, 60000);
 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			timer.cancel();
			stmt.close();
			conn.commit();
		}
		catch(Exception e)
		{
			
		}
	}
	
	boolean update()
	{
		//请求数据
		String url = "http://newte.sh.1251225243.clb.myqcloud.com/TEGateway/6e9ad52ff6229a3c90840b51731c1571/RTICTraffic.xml?bizcode=06049f184f6340f536102cb8bb57ea98&adcode=330400&mesh=466015&format=0";
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if(!getURL(url,output))
			return false;
		
		try {
			//xml解析
			String xmlcontent = output.toString("utf-8");
			StringReader reader = new StringReader(xmlcontent);
		 
			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(reader);
	 

			@SuppressWarnings("unchecked")
			List<Node> cities = doc.selectNodes("response/result/cities/city");
			
			for(Node city : cities)
			{
				String adcode = city.selectSingleNode("adcode").getText();
				String updatetime = city.selectSingleNode("updatetime").getText();
				
				//System.out.println("city:" + adcode + "," + updatetime);
				
				@SuppressWarnings("unchecked")
				List<Node> meshs = 	city.selectNodes("mesh");
				
				for(Node mesh : meshs)
				{
					String code = mesh.selectSingleNode("code").getText();
					String flow = mesh.selectSingleNode("flow").getText();
					
					//System.out.println("mesh:"+code);
					
					processMesh(code, flow);
 
				}
			}
		
		
			//提交数据�?
			stmt.executeBatch();
			conn.commit();

			 
			return true;
			//数据更新
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	boolean processMesh(String mesh, String flow)
	{
		try {
			int meshid = Integer.parseInt(mesh);
			
		    BASE64Decoder decoder = new BASE64Decoder();
 
			byte[] data=  decoder.decodeBuffer(flow);
			 
			DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
			  
			//解析二次网格数据 3字节
			byte[] mesh2 = new byte[3];
			stream.read(mesh2);
 
			//解析路链�?2字节
			 int rticcnt = 	stream.readShort();
			 for(int i = 0; i < rticcnt; i++)
			 {
				  //路链 分层，分类，序号
				 short rtic = stream.readShort();
				 /*
				    1	狭域
					2	中域
					3	广域
				  */
				 int  rticlayer = (rtic & 0xC000) >> 14; //
				 
			    /*
			        0	高�??
					1	快�??
					2	�?�?
					3	其他
			     */
			     int  rtickind = (rtic & 0x3000) >> 12;   
				 
				 int  rticid = (rtic & 0xfff);  
				 
				 //System.out.println("rtic\t  kind:" + rtickind + "  id:" + rticid);
				 //旅行时间
				 short  time = stream.readShort();
				 //拥堵路段�?
				 int   hcount =  stream.readByte();
				 //拥堵信息
				 byte   h2 = stream.readByte();
				 /*
				  	0	不明
					1	通畅
					2	缓慢
					3	拥堵
				  */
				 int  htype = (h2 & 0x18) >> 3;   //拥堵程度
				 int  htime = (h2 & 0x4) >> 2;    //有无提供旅行时间  0:未提供�??1:提供
				 int  htimetype = (h2 & 0x2) >> 1;//旅行时间的类�?  0:实时�?1:预测
				 
			 
				 for(int j = 0; j < hcount; j++)
				 {
					 int t1 = stream.readShort();
					 int h = (t1 & 0xc000) >> 14;   //拥堵程度
				     int dis = (t1 & 0x3fff);       //距离链路终点距离
					 int len = stream.readShort();  //拥堵长度
				 }
				 if(hcount != 1)
				 {
					 System.out.println("mesh:" + meshid + "\t rtickind:" + rtickind + "\t rticid:" + rticid + "\t拥堵路段=" + hcount);
				 }

				// System.out.println("mesh:" + meshid + "\t rtickind:" + rtickind + "\t rticid:" + rticid + "\t H=" + htype);
				 //更新数据�?
				 stmt.setInt(1, htype);
				 stmt.setInt(2, meshid);
				 stmt.setInt(3, rtickind);
				 stmt.setInt(4, rticid);
				 
				 stmt.addBatch();
			 }
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
	}
	
	static boolean getURL(String url, OutputStream stream) {
		try {
			URL getUrl;

			getUrl = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) getUrl
					.openConnection();

			connection.connect();

			byte[] buf = new byte[1024 * 20];

			InputStream instream = connection.getInputStream();

			int nread = 0;

			while ((nread = instream.read(buf)) != -1) {

				stream.write(buf, 0, nread);
			}

			instream.close();

			connection.disconnect();
		} catch (Exception e) {

			return false;
		}
		return true;

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("traffic begin:" + new Date());
		update();
		System.out.println("traffic end:" + new Date());
	}

}
