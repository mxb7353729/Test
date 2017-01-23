package com.evialab.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class RoadQueryTest {

	public static void main(String[] args) {

		String field = IndexRoadFromDB.Field_Name;
		//String queryString = "jian cai ";

		String[] keys = new String[] { "海淀区上地四街1号院", "110099" };
		String[] fields = new String[] { IndexRoadFromDB.Field_Name, IndexRoadFromDB.Field_py };
		BooleanClause.Occur[] occurs = new BooleanClause.Occur[] { BooleanClause.Occur.MUST, BooleanClause.Occur.SHOULD };
		int hitsPerPage = 10;

		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexRoadFromDB.IndexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new SmartChineseAnalyzer();

			//QueryParser parser = new QueryParser(field, analyzer);
			//parser.setDefaultOperator(QueryParser.AND_OPERATOR);

			// Query query = parser.parse(queryString);

			//组合多个查询条件
			BooleanQuery.Builder mquery = new  BooleanQuery.Builder();
			
			
			//在name �? py里面搜索 关键�?
			QueryParser con_name = new MultiFieldQueryParser(new String[] { IndexRoadFromDB.Field_Name, IndexRoadFromDB.Field_py }, analyzer);
			con_name.setDefaultOperator(QueryParser.AND_OPERATOR);

			mquery.add(con_name.parse("上地四街"), BooleanClause.Occur.MUST);
			  
			//在行政区划里搜索
			//QueryParser con_admin = new QueryParser(IndexDatabaseTable.Field_py,analyzer);
			//con_admin.setDefaultOperator(QueryParser.AND_OPERATOR);
			//mquery.add(con_admin.parse("dong"), BooleanClause.Occur.MUST);
			
		 	 
			 Query con_admin = NumericRangeQuery.newLongRange(IndexRoadFromDB.Field_admincode, 110100L, 110199L, true, true);
			 mquery.add(con_admin, BooleanClause.Occur.MUST);
			 
			Query query = mquery.build();
			
			System.out.println("Searching for: " + query.toString());

			doPagingSearch(searcher, query, hitsPerPage);

			reader.close();

		} catch (Exception e) {

		}

	}

	public static void doPagingSearch(IndexSearcher searcher, Query query, int hitsPerPage) throws IOException {

		// Collect enough docs to show 5 pages
		TopDocs results = searcher.search(query, 5 * hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		if (results.totalHits > hits.length) {

			 hits= searcher.search(query, numTotalHits).scoreDocs;
	 
		}

		for (int i = 0; i < numTotalHits; i++) {

			Document doc = searcher.doc(hits[i].doc);

			IndexableField fn = doc.getField(IndexRoadFromDB.Field_Name);

			IndexableField id = doc.getField(IndexRoadFromDB.Field_ID);
			IndexableField mapid = doc.getField(IndexRoadFromDB.Field_mapid);
			IndexableField admin = doc.getField(IndexRoadFromDB.Field_admincode);
			IndexableField route_id = doc.getField(IndexRoadFromDB.Field_route_id);

			System.out.println("name=" + fn.stringValue() + "\tmid=" + mapid.numericValue() + "\tid=" + id.numericValue() + "\tadmin=" + admin.numericValue()+ "\troute_id=" + route_id.numericValue());

		}

	}
}
