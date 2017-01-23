package com.evialab.extend;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.app.IndexRoadFromDB;
import com.evialab.gisserver.RGCPostgreSql;
import com.evialab.util.DBPool;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class RoadQuery
 */
public class RoadQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RoadQuery() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request,response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	static IndexReader  s_reader = null;
	static IndexSearcher s_searcher = null;
	
	static void   initSearcher(String path) throws IOException
	{
		s_reader = DirectoryReader.open(FSDirectory.open(Paths.get(path)));
		s_searcher = new IndexSearcher(s_reader);

	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		//根据道路�? �?  城市 返回道路几何数据
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		String name = RequestUtil.getParameter(request, "name");
		
		if(RequestUtil.isNull(name))
		{
			ResponseUtil.error(response, "缺少name参数");
			return;
		}
		//如果有城市名获得城市
		String city = RequestUtil.getParameter(request, "city");
		
		//把城市名转为code搜索
		AdminQuery.AdminQueryResult admin = null;
		try {
			admin = AdminQuery.QueryAdmin(city);	
		} catch (ParseException e1) {
			 e1.printStackTrace();
		}
		
		
	    if(s_searcher == null)
	    {
	    	initSearcher(IndexRoadFromDB.IndexPath);
	    }
	    if(s_searcher == null)
	    {
	    	ResponseUtil.error(response, "内部错误");
			return;
	    }
	    
	    
		String output = request.getParameter("output");
		String callback = request.getParameter("callback");
		String encoding = request.getParameter("encoding");
 
		//先从index库里搜索名称
	    try{
	    	 BooleanQuery.Builder mquery = new  BooleanQuery.Builder();
	 		
	 	    Analyzer analyzer = new SmartChineseAnalyzer();
	 		//在name �? py里面搜索 关键�?
	 		QueryParser con_name = new MultiFieldQueryParser(new String[] { IndexRoadFromDB.Field_Name, IndexRoadFromDB.Field_py }, analyzer);
	 		con_name.setDefaultOperator(QueryParser.AND_OPERATOR);

	 		mquery.add(con_name.parse(name), BooleanClause.Occur.MUST);
	 		  
	 		 
	 		if(admin != null)
	 		{
	 			
	 			 Query con_admin = NumericRangeQuery.newLongRange(IndexRoadFromDB.Field_admincode, (long)admin.minadmin, (long)admin.maxadmin, true, true);
	 			 mquery.add(con_admin, BooleanClause.Occur.MUST);
	 		}
	 	 	 
	 		//
	 		Query query = mquery.build();
	 		TopDocs results = s_searcher.search(query,1);
			ScoreDoc[] hits = results.scoreDocs;
			JSONObject obj = new JSONObject();
			
			
			if(hits.length == 0)
			{
				ResponseUtil.writeJson2Response(response, output, encoding, callback, obj);
				return;
			}


			//得到route_id 根据此id 我们从数据库获取数据
			Document doc = s_searcher.doc(hits[0].doc);
			
			IndexableField road_name = doc.getField(IndexRoadFromDB.Field_Name);
			//obj.put("all_count", results.totalHits);
			obj.put("name", road_name.stringValue());
			
			IndexableField route_id = doc.getField(IndexRoadFromDB.Field_route_id);
			
			long id = route_id.numericValue().longValue();
			
			//从数据库里查询数�?
			Connection con = null;
			try {
				con = DBPool.getDataInstance().getConnection();
				PreparedStatement stm = null;
				if(admin == null)
				{
					String sql = "select * from public.v_r where route_id = ?";
					stm = con.prepareStatement(sql);
					stm.setObject(1, id);
				}
				else
				{
					String sql = "select * from public.v_r where route_id = ? and ((admincoder >= ? and admincoder<=?) or (admincodel >= ? and admincodel<=?))";
					stm = con.prepareStatement(sql);
					stm.setObject(1, id);
					stm.setObject(2, admin.minadmin);
					stm.setObject(3, admin.maxadmin);
					stm.setObject(4, admin.minadmin);
					stm.setObject(5, admin.maxadmin);
				}
				
				 
				//获取�?有路段信�?
				JSONArray roads  = new JSONArray();
				
				ResultSet rs = stm.executeQuery();
				while (rs.next()) {
					JSONObject road = RGCPostgreSql.roadInfo(rs);
				 
					road.put("admincode", rs.getObject("admincodel"));
					String geom = rs.getString("geom");
					
					road.put("geom", new JSONObject(geom));
					
					roads.put(road);
				}

				rs.close();

				obj.put("roads", roads);
				
				if(admin != null)
				{
					obj.put("admin", admin.name);
					obj.put("admincode", admin.code);
				}
				
				ResponseUtil.writeJson2Response(response, output, encoding, callback, obj);
				return;

			} catch (Exception e) {

				 e.printStackTrace();
				 
			} finally {
				if (con != null)
					DBPool.getDataInstance().closeConnection(con);
			}
			
		 
			
	    	
	    }catch(Exception e)
	    {
	    	ResponseUtil.error(response, "查询失败");
	    	e.printStackTrace();
	    	return;
	    	
	    }
	   
		
		
		
	}

}
