package com.evialab.app;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class IndexAdminFromDB {

	static final String ConStr = "jdbc:postgresql://127.0.0.1:5432/china15q4";
	public static   String IndexPath = "c:\\out\\adminindex";
	public static final String Field_name = "name";
	public static final String Field_code = "code";
	public static final String Field_type = "type";
	public static final String Field_fullname = "fullname";
	public static final String Field_pcode = "pcode";
	public static void main(String[] args) {

		try {
			IndexAdminFromDB index = new IndexAdminFromDB();

			Connection conn = null;
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(ConStr, "postgres", "123456");

			// 处理省级区划
			index.index(conn,"select distinct proadcode code,proname as name,100000::integer pcode,proname as fullname from admin",1);
			index.index(conn,"select distinct proadcode code,proname2 as name,100000::integer pcode,proname as fullname from admin",1);
		 
			//处理省市
			index.index(conn,"select distinct cityadcode code,proname||cityname as name ,proadcode as pcode,proname||cityname as fullname from admin",2);
			index.index(conn,"select distinct cityadcode code,proname||cityname2 as name,proadcode as pcode,proname||cityname as fullname from admin",2);
			index.index(conn,"select distinct cityadcode code,proname2||cityname as name,proadcode as pcode,proname||cityname as fullname from admin",2);
			index.index(conn,"select distinct cityadcode code,proname2||cityname2 as name,proadcode as pcode,proname||cityname as fullname from admin",2);
			//处理省市�?
			index.index(conn,"select distinct admincode code,proname||cityname||adminname as name,cityadcode as pcode,proname||cityname||adminname as fullname from admin",3);
			index.index(conn,"select distinct admincode code,proname||cityname||adminname2 as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname||cityname2||adminname as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname||cityname2||adminname2 as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname2||cityname||adminname as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname2||cityname||adminname2 as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname2||cityname2||adminname as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			index.index(conn,"select distinct admincode code,proname2||cityname2||adminname2 as name,cityadcode as pcode,proname||cityname||adminname as fullname  from admin",3);
			
			index.finish();
			conn.close();

			System.out.println("所有处理完成");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public IndexAdminFromDB() throws IOException {
		this(true);
	}

	public void finish() throws IOException {
		writer.close();
	}

	public IndexAdminFromDB(boolean create) throws IOException {

		// 使用默认的中文分词器
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
 
			int code = rs.getInt(Field_code);
			String name = rs.getString(Field_name);
			
			int pcode = rs.getInt(Field_pcode);
			String fullname = rs.getString(Field_fullname);
			
			 
			Document doc = new Document();
			doc.add(new TextField(Field_name, name, Field.Store.YES));
			doc.add(new LongField(Field_code, code, Field.Store.YES));
			doc.add(new TextField(Field_fullname, fullname, Field.Store.YES));
			doc.add(new LongField(Field_pcode, pcode, Field.Store.YES));
			doc.add(new LongField(Field_type, type, Field.Store.YES));
			writer.addDocument(doc);
		}
		rs.close();
		ps.close();

		System.out.println("处理完成�?" + sql);
	}
	/*
	public void index(Connection conn, String sql, int type) throws SQLException, IOException {

		// 遍历�?有行，那�?

		PreparedStatement ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			
			 
			int admincode = rs.getInt("admincode");
			String [] adminname = new String[]{ rs.getString("adminname"),  rs.getString("adminname2")};
			 
			int cityadcode = rs.getInt("cityadcode");
			String []cityname = new String[]{ rs.getString("cityname"),  rs.getString("cityname2")};
		 
			
			int proadcode = rs.getInt("proadcode");
			String []proname = new String[]{ rs.getString("proname"),  rs.getString("proname2")};
			
			String name = "";
			
			for(int i = 0; i < proname.length; i++)
			{
				if(i > 0 && proname[i].equals(proname[i-1]))
					continue;
 
				//�?
				name  = proname[i];
						
				Document doc = new Document();
				doc.add(new TextField(Field_name, name, Field.Store.YES));
				doc.add(new LongField(Field_code, proadcode, Field.Store.YES));
				doc.add(new LongField(Field_type, 1, Field.Store.YES));
				writer.updateDocument(new Term(Field_name,name), doc);
				
				for(int j = 0; j < cityname.length; j++)
				{
					if(j > 0 && cityname[j].equals(cityname[j-1]))
						continue;
					
					//省市
					name  = proname[i]+cityname[j];
					doc = new Document();
					doc.add(new TextField(Field_name, name, Field.Store.YES));
					doc.add(new LongField(Field_code, cityadcode, Field.Store.YES));
					doc.add(new LongField(Field_type, 2, Field.Store.YES));
					writer.updateDocument(new Term(Field_name,name), doc);
					
					for(int k = 0; k < adminname.length; k++)
					{
						if(k > 0 && adminname[k].equals(adminname[k-1]))
							continue;
						
						//省市�?
						name  = proname[i]+cityname[j]+adminname[k];
						doc = new Document();
						doc.add(new TextField(Field_name,name, Field.Store.YES));
						doc.add(new LongField(Field_code, admincode, Field.Store.YES));
						doc.add(new LongField(Field_type, 3, Field.Store.YES));
					 
						writer.updateDocument(new Term(Field_name,name), doc);
					}
				}
			}
		}
		rs.close();
		ps.close();

		System.out.println("处理完成�?" + sql);
	}*/
}
