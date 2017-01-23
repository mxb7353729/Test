package com.evialab.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;

import com.evialab.util.QuanjiaoUtil;

public class IndexPOIFromCSV {
	
	public static String IndexPath = "D:\\china15q4\\localsearch";//--输出路径
	public static String CSVPath = "D:\\china15q4\\hamletcsv";//--源文件路�? hamletcsv   poicsv

	public static void main(String[] args) {

		// 创建索引
		try {
			Analyzer analyzer = new SmartChineseAnalyzer();

			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);//三个参数：APPEND CREATE CREATE_ORAPPEND

			// iwc.setRAMBufferSizeMB(256.0);
			IndexWriter writer = new IndexWriter(FSDirectory.open(Paths.get(IndexPath)), iwc);
			// 遍历目录处理�?有csv

			File[] fs = new File(CSVPath).listFiles();
			for (File f : fs) {
				// �?始读文件
				System.out.println("�?始处理：" + f.getAbsolutePath());

				BufferedReader br = new BufferedReader(
						new InputStreamReader(new FileInputStream(f.getAbsoluteFile()), "GBK"));
				String line = br.readLine();
				String[] fields = line.split(",", -1);

				line = br.readLine();
				for (; line != null; line = br.readLine()) {

					String[] values = line.split(",", -1);
					if (values.length != fields.length)
						continue;
					Document doc = new Document();

					for (int i = 0; i < fields.length; i++) {
						String v = values[i];
						if (v == null || v.isEmpty())
							continue;
						String k = fields[i];
						if ("Display_X".equals(k))
							//k = "x";
							doc.add(new DoubleField("x", Double.parseDouble(v), Field.Store.YES));
						else if ("Display_Y".equals(k))
							//k = "y";
							doc.add(new DoubleField("y", Double.parseDouble(v), Field.Store.YES));
						else if("AdminCode".equals(k))
							doc.add(new LongField(k, Long.parseLong(v), Field.Store.YES));
						else
						{
							  if("Address".equals(k) || "Street".equals(k) || "Number".equals(k) || "Name".equals(k)){
								  v= QuanjiaoUtil.qj2bj(v);
								  doc.add(new TextField(k, v, Field.Store.YES));
							  }
						}
					}

					writer.addDocument(doc);
				}
				br.close();

				System.out.println("处理结束�?" + f.getAbsolutePath());
			}
			writer.commit();
			writer.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
