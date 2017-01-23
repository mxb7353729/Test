package com.evialab.extend;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONObject;

import com.evialab.app.IndexAdminFromDB;
import com.evialab.app.IndexRoadFromDB;
import com.evialab.util.ResponseUtil;

public class AdminQuery {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static class AdminQueryResult {
		public String name;
		public int code;
		public int type;
		public int pcode;
		public int minadmin = 0;
		public int maxadmin = 0;
		
		public  AdminQueryResult(String n, int c, int t)
		{
			name =n;
			code =c;
			type = t;
 
 			if(type == 1)
 			{
 				minadmin =  code / 10000 * 10000;
 				maxadmin = minadmin+9999;
 			}
 			else if( type == 2)
 			{
 				minadmin =  code / 100 * 100;
 				maxadmin = minadmin+99;
 			}
 			else
 			{
 				minadmin = maxadmin =  code;
 			}
		}
	}

	static IndexReader s_reader = null;
	static IndexSearcher s_searcher = null;
	static Analyzer s_analyzer = null;

	static void initSearcher(String path) throws IOException {
		s_reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
		s_searcher = new IndexSearcher(s_reader);
		s_analyzer = new SmartChineseAnalyzer();
	}

	public static AdminQueryResult QueryAdmin(String admin) throws ParseException, IOException {
		AdminQueryResult result = null;
		if(admin == null || admin.isEmpty())
			return null;

		if (s_searcher == null) {

			initSearcher(IndexAdminFromDB.IndexPath);

		}
		if (s_searcher == null) {
			return null;
		}

		QueryParser con_admin = new QueryParser(IndexAdminFromDB.Field_name, s_analyzer);
		con_admin.setDefaultOperator(QueryParser.AND_OPERATOR);

		Query query = con_admin.parse(admin);

		TopDocs results = s_searcher.search(query, 1);
		ScoreDoc[] hits = results.scoreDocs;

		if (hits.length == 0) {
			return null;
		}
		Document doc = s_searcher.doc(hits[0].doc);
		
		//IndexableField name = doc.getField(IndexAdminFromDB.Field_name);
		IndexableField fullname = doc.getField(IndexAdminFromDB.Field_fullname);
		IndexableField code = doc.getField(IndexAdminFromDB.Field_code);
		IndexableField pcode = doc.getField(IndexAdminFromDB.Field_pcode);
		IndexableField type = doc.getField(IndexAdminFromDB.Field_type);
		
		
		 
		result = new AdminQueryResult(fullname.stringValue(),code.numericValue().intValue(), type.numericValue().intValue());
		result.pcode = pcode.numericValue().intValue();
		

		return result;
	}

}
