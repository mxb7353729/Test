package com.evialab.gisserver;

import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.search.RangeFilter;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.RequestUtil;

public class HmSearch {

	
	// TODO Auto-generated method stub
	static private HmSearch g_reader = null;
	//static private String m_cachePath = "D:\\data\\2015WinHmIndex";
	static private String m_cachePath ="E:\\四维\\2 数据\\2015win\\hamletlucene\\hamletindex";
	//static private String m_cachePath = "E:\\四维\\2 数据\\2015win\\hamletlucene\\2015hamletindex";
	//static private String m_cachePath2 = "E:\\四维\\2 数据\\本地搜索处理\\testkind";
	//static private HashMap<String, String> m_city2Prov = new HashMap<String, String>();
	//static private HashMap<String, Searcher> m_searcherCollect = new HashMap<String,Searcher>();
	//static private HashSet<String> m_provs = new HashSet<String>();
	
	static private IndexSearcher m_searcher = null;
	
	//在环境启动中初始�?
	public static void InitSearcher(String path)
	{
		try
		{
			//m_cachePath = path;
			
			//IndexReader reader = IndexReader.open(m_cachePath);
			IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(m_cachePath)));
			m_searcher = new IndexSearcher(reader);
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}	 

	//搜索结果中的�?条记录组合为Json对象
	private static void DocToJson(Object jo, Document doc, int resultSet)
	{
		String set =  Integer.toBinaryString(resultSet);
		
		String []b  = new String[5];
		b[4] = "Name";
		b[3] = "Address";
		b[2] = "Telephone";
		b[1] = "Admin_Code";
		b[0] = "ZipCode";
		
		JSONObject point = new JSONObject();
		
		for(int i = set.length() - 1; i >= 0; i--)
		{
			String sub = set.substring(i, i+1);
			if(sub.compareTo("1") == 0) 
			{
				IndexableField fld = doc.getField(b[i]);
				if(fld != null)
				{
					point.put(b[i].toLowerCase(), doc.get(b[i]));
				}
			}
		}
		point.put("lng", doc.get("x"));
		point.put("lat", doc.get("y"));
		
		if(jo instanceof JSONObject)
			((JSONObject)jo).append("point", point);
		else if(jo instanceof JSONArray)
			((JSONArray)jo).put( point);
			
	}
	//名称搜索,搜索条件过多返回错误，应该预读入POI的类型，以便拆解Kind时，加快速度
	public static int NameSearcher(JSONObject jresult, String city, String words, String classp, String[]rect,
			int page, int pageCap, int resultmode,boolean xml)
	{

		if(m_searcher == null)
			HmSearch.InitSearcher("");;
		IndexSearcher s= m_searcher;//测试
		int start = (page-1) * pageCap;
		int end = page*pageCap;
		//JSONObject jresult = new JSONObject();
		try
		{
			 //BooleanQuery q = new BooleanQuery();
			BooleanQuery.Builder q = new  BooleanQuery.Builder();
			 
	        Analyzer analyzer = new StandardAnalyzer();

	        //如果城市参数为空，或者为全国，那么全国搜�?
	        if(!(city == null || "全国".equals(city)))
	        {
	        	QueryParser parserCity = new QueryParser("City", analyzer);
	 	        Query queryCity = parserCity.parse(city);	
	 	        q.add(queryCity, BooleanClause.Occur.MUST);
	        }
 
	        if(RequestUtil.isNull(classp))	//class为空的情况，只�?�虑关键�?
	        {
		        QueryParser parserWord = new QueryParser("Name", analyzer);
		        Query queryWord = parserWord.parse(words);	
		        q.add(queryWord, BooleanClause.Occur.MUST);
	        }

	        
	        TopDocs docs = null;
	        if(rect != null)
	        {
		        if(rect.length == 4)
		        {
		        	String west = rect[0];
		        	String east = rect[1];
		        	String south = rect[2];
		        	String north = rect[3];
		            //范围搜索
			        DecimalFormat df = new DecimalFormat("000.0000");
		            
		            Query filterX = NumericRangeQuery.newDoubleRange("x", Double.parseDouble(west),
		            		Double.parseDouble(east), true, true);
		            
		            Query fiterY = NumericRangeQuery.newDoubleRange("y", Double.parseDouble(south),
		            		Double.parseDouble(north), true, true);
		            
		        
		            q.add(filterX, BooleanClause.Occur.MUST);
		            q.add(fiterY, BooleanClause.Occur.MUST);
		        }
	        }

	        docs =s.search(q.build(), end);
	        
            
 	        //�?始查�?
	        if(docs == null)
	        {
				jresult.put("status", "error");
				jresult.put("error", "不支持的查询类型");
	        	return 13;
	        }
	        if(docs.totalHits > 0)
	        {
		        
		        if(start > (docs.totalHits-1))
		        {
					jresult.put("status", "error");
					jresult.put("error", "没有更多的查询结�?");
		        	return 10;
		        }
		        
		        if(end > docs.totalHits)
		        {
		        	end = docs.totalHits;
		        }
		        
		        
		        //JSONObject jpoints = new JSONObject();
		        Object jpoints = xml ? new JSONObject() : new JSONArray();
		        
		        double minx = 180;
		        double maxx = -180;
		        double miny = 180; 
		        double maxy = -180;
		        
		        for(int i = start; i < end; i++)
		        {
		        	Document doc = s.doc(docs.scoreDocs[i].doc);
		        	DocToJson(jpoints, doc, resultmode);
		        	double x =  Double.parseDouble(doc.get("x"));
		        	double y = Double.parseDouble(doc.get("y"));
		        	if(x < minx) minx = x;
		        	if(x > maxx) maxx = x;
		        	if(y < miny) miny = y;
		        	if(y > maxy) maxy = y;
		        }
		        
				jresult.put("points", jpoints);
				jresult.put("curpage", page);
				jresult.put("pagecount", (docs.totalHits-1) / pageCap + 1);
				jresult.put("total", docs.totalHits);
				jresult.put("curresult", 1);
				jresult.put("bound", Double.toString(minx) + "," + Double.toString(miny)
						 + ";" + Double.toString(maxx) + "," + Double.toString(maxy));
	        }

		}
		catch(Exception ex)
		{
			System.out.println("HmSearch: " + "NameSearcher: " + ex.getMessage());
			jresult.put("status", "error");
			jresult.put("error", "搜索异常，请联系�?术支�?");
			return 1;
		}
		return 0;
	}
	


	//根据名字和AdminCode搜索POI
	//Address
	public static boolean GCSearchPOI(String address, String admin, String city, ArrayList<JSONObject> jos)
	{

//			JSONObject jo = new JSONObject();
//			if(city.isEmpty() && admin.isEmpty() )
//			{
//				jo.put("error", "-1");
//				return jo;
//			}
        ////范围搜索构�?�结�?
		try
		{		 
			BooleanQuery q = new BooleanQuery();
 
			Analyzer analyzer = new StandardAnalyzer();
			
			QueryParser parserA = new QueryParser("Address", analyzer);
			Query queryAddress = parserA.parse(address);	
			q.add(queryAddress, BooleanClause.Occur.MUST);

			if(!admin.isEmpty())
			{
				QueryParser parserX = new QueryParser("AdminCode", analyzer);
				Query queryAdmin = parserX.parse(admin);
				q.add(queryAdmin, BooleanClause.Occur.MUST);
			}
 
			if(!city.isEmpty())
			{
				QueryParser parserC = new QueryParser("City", analyzer);
				Query queryCity = parserC.parse(city);
				q.add(queryCity, BooleanClause.Occur.MUST);
			}
		    
		    TopDocs docs = null;
		    int maxcnt = 10;
		    docs =m_searcher.search(q, maxcnt);
 
	        for(int i = 0; i < Math.min(docs.totalHits,maxcnt); i++)
	        {
	        	Document doc = m_searcher.doc(docs.scoreDocs[i].doc);
	        	JSONObject joitem = new JSONObject();
	        	joitem.put("id", i);
	        	joitem.put("address", doc.get("Address"));
	        	joitem.put("lat", doc.get("y"));
	        	joitem.put("lng", doc.get("x"));
	        	joitem.put("name", doc.get("Name"));
	        	
	        	
	        	jos.add(joitem);
 
	        	
	        	//System.out.println(doc.get("Address"));
	        }
	        return docs.totalHits > 0;
		}
		catch(Exception ex)
		{
			
			System.out.println("LocalSearchL: " + "GCSearchPOI: " + ex.getMessage());
			System.out.println("address=" + address + ",admin=" + admin + ",city=" + city);
		}
		return false;
	
	}
	public static void main(String[] args) {
		HmSearch.InitSearcher(m_cachePath);
		JSONObject j = new JSONObject();
		int n =  HmSearch.NameSearcher(j, null,"小南�?",null,null, 1, 10, 31,false);
		System.out.print(j.toString());
	}

}
