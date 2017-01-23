package com.evialab.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//把数据库中的�? 创建索引
public class IndexRoadFromDB {

	static final String ConStr = "jdbc:postgresql://127.0.0.1:5432/china15q4";
	public static    String IndexPath = "c:\\out\\index";
	public static final  String Field_Name = "name";
	
	
	public static final String Field_py = "py";
	public static final String Field_route_id = "route_id";
	public static final String Field_mapid = "mapid";
	public static final String Field_ID = "id";
	public static final String Field_admincodel = "admincodel";
	public static final String Field_admincoder = "admincoder";
	public static final String Field_admincode = "admincode";
	
	
	public static void main(String[] args) {

		try {
			IndexRoadFromDB index = new IndexRoadFromDB();

			Connection conn = null;
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(ConStr,"postgres","123456");

			String allsheng = "select * from information_schema.schemata";
			PreparedStatement ps = conn.prepareStatement(allsheng);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
			{
				
				String sheng = rs.getString("schema_name");
				if (sheng.indexOf("postgis") >= 0 || sheng.indexOf("pg_") >= 0 || sheng.indexOf("b_") >= 0 || sheng.indexOf("public") >= 0 || sheng.indexOf("information_schema") >= 0)
					continue;
				
				Date d = new Date();
				System.out.println(d.toString()+ " 处理�?:" + sheng );
				
				
				index.index(conn, "select * from " + sheng + ".nameindex", 0);
				
				 
				System.out.println("耗时:" + ((new Date()).getTime() - d.getTime()) / 1000 + "�?");
			}
			rs.close();
			
		 
			

			index.finish();
			conn.close();
			
			System.out.println("处理完成");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public IndexRoadFromDB() throws IOException {
		this(true);
	}

	public void finish() throws IOException
	{
		writer.close();
	}
	public IndexRoadFromDB(boolean create) throws IOException {
		
		//使用默认的中文分词器
		Analyzer analyzer = new SmartChineseAnalyzer();
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		if (create) {
			iwc.setOpenMode(OpenMode.CREATE);
		} else {
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		}

		// Optional: for better indexing performance, if you
		// are indexing many documents, increase the RAM
		// buffer. But if you do this, increase the max heap
		// size to the JVM (eg add -Xmx512m or -Xmx1g):
		//
		// iwc.setRAMBufferSizeMB(256.0);
		
		Directory dir = FSDirectory.open(Paths.get(IndexPath));
		writer = new IndexWriter(dir, iwc);
	}

	IndexWriter writer = null;

	public void index(Connection conn, String sql, int type) throws SQLException, IOException {

		// 遍历�?有行，那�?

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			String name = rs.getString(Field_Name);
			String py = rs.getString(Field_py);
			String route_id = rs.getString(Field_route_id);
			String mapid = rs.getString(Field_mapid);
			String id = rs.getString(Field_ID);
			String adminl = rs.getString(Field_admincodel);
			String adminr = rs.getString(Field_admincoder);
			
			if(name == null)
				continue;
			 

			{
				Document doc = new Document();
				doc.add(new TextField(Field_Name, name, Field.Store.YES));
				if(py != null)
				doc.add(new TextField(Field_py, py, Field.Store.YES));
				doc.add(new LongField(Field_route_id, Long.parseLong(route_id), Field.Store.YES));
				doc.add(new LongField(Field_mapid, Long.parseLong(mapid), Field.Store.YES));
				doc.add(new LongField(Field_ID, Long.parseLong(id), Field.Store.YES));
				doc.add(new LongField(Field_admincode, Long.parseLong(adminl), Field.Store.YES));
	 
				writer.addDocument(doc);
			}
			if(!adminl.equals(adminr))
			{
				Document doc = new Document();
				 
				doc.add(new TextField(Field_Name, name, Field.Store.YES));
				if(py != null)
				doc.add(new TextField(Field_py, py, Field.Store.YES));
				doc.add(new LongField(Field_route_id, Long.parseLong(route_id), Field.Store.YES));
				doc.add(new LongField(Field_mapid, Long.parseLong(mapid), Field.Store.YES));
				doc.add(new LongField(Field_ID, Long.parseLong(id), Field.Store.YES));
				doc.add(new LongField(Field_admincode, Long.parseLong(adminr), Field.Store.YES));
	 
				writer.addDocument(doc);
			}
		
		}
		rs.close();
		ps.close();

		
		System.out.println("处理完成�?" + sql);
	}

}
