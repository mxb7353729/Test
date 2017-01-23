package com.evialab.app;


import java.io.IOException;

import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
 

public class AdminQueryTest {

	public static void main(String[] args) {

	 
	 

		try {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(IndexAdminFromDB.IndexPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new SmartChineseAnalyzer();
 

			//组合多个查询条件
			BooleanQuery.Builder mquery = new  BooleanQuery.Builder();
			
			
			//在name �? py里面搜索 关键�?
			QueryParser con_name = new MultiFieldQueryParser(new String[] { IndexAdminFromDB.Field_name  }, analyzer);
			con_name.setDefaultOperator(QueryParser.AND_OPERATOR);

			mquery.add(con_name.parse("北京海淀"), BooleanClause.Occur.MUST);
			  
			//在行政区划里搜索
			//QueryParser con_admin = new QueryParser(IndexDatabaseTable.Field_py,analyzer);
			//con_admin.setDefaultOperator(QueryParser.AND_OPERATOR);
			//mquery.add(con_admin.parse("dong"), BooleanClause.Occur.MUST);
			
		 	 
			// Query con_admin = NumericRangeQuery.newLongRange(IndexRoadFromDB.Field_admincode, 110100L, 110199L, true, true);
			// mquery.add(con_admin, BooleanClause.Occur.MUST);
			 
			Query query = mquery.build();
			
			System.out.println("Searching for: " + query.toString());

			doPagingSearch(searcher, query, 10);

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

			IndexableField fname = doc.getField(IndexAdminFromDB.Field_fullname);

			IndexableField code = doc.getField(IndexAdminFromDB.Field_code);
			IndexableField type = doc.getField(IndexAdminFromDB.Field_type);
		 

			System.out.println("name=" + fname.stringValue() + "\tcode=" + code.numericValue() + "\ttype=" + type.numericValue() );

		}

	}
}
