package com.evialab.gisserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import org.json.JSONObject;

public class GCCach {
	public static ArrayList<String>  nations = null;
	
	Connection  dbcon = null;
	
	ArrayList<Province> provinces = new ArrayList<Province>(); 
	HashMap<String , City> cities = new HashMap<String , City>();
	
	public GCCach(Connection con){
		try {	
			if (nations == null) {
				String nationstr = "壮族,满族,回族,苗族,维吾尔族,土家�?,"
						+ "彝族,蒙古�?,藏族,布依�?,侗族,瑶族,朝鲜�?,"
						+ "白族,哈尼�?,哈萨克族,黎族,傣族,畲族,傈僳�?,"
						+ "仡佬�?,东乡�?,高山�?,拉祜�?,水族,佤族,纳西�?,"
						+ "羌族,土族,仫佬�?,锡伯�?,柯尔克孜�?,达斡尔族,景颇�?,"
						+ "毛南�?,撒拉�?,布朗�?,塔吉克族,阿昌�?,普米�?,鄂温克族,"
						+ "怒族,京族,基诺�?,德昂�?,保安�?,俄罗斯族,裕固�?,"
						+ "乌兹别克�?,门巴�?,鄂伦春族,独龙�?,塔塔尔族,赫哲�?,珞巴�?";

				String[] nn = nationstr.split(",");
				nations = new ArrayList<String>();
				for (int i = 0; i < nn.length; i++) {
					nations.add(nn[i]);
				}

			}

			dbcon = con;
		
			//查询�?有省的名�?
			PreparedStatement smt  = dbcon.prepareStatement("select pcode,pname from admin_province");
			ResultSet rs = smt.executeQuery();
			while(rs.next()){
				 
				Province pro = new Province(rs.getString(2));
				pro.admincode =  rs.getInt(1);

				provinces.add(pro);
			}

		 } catch (SQLException e) {
		  
		}
		
	}
	
	private class CutIndex{
		public int idx = -1;
		public int len = 0;
	}
	public class CutUnit
	{
		public CutUnit(String w, int l){
			words = w;
			level = l;
		}
		public String words;
		public int    level;
	}
	public class CutResult{
	    public ArrayList<CutUnit> cutwords = new ArrayList<CutUnit>();
	    public String  address = "";
	    public Province  province = null;
	    public City  city = null;
	    public District  district = null;
	    public Block  block = null; 
	    public String roadAndDoor = null;  //道路和门�?
	    public String road = null;  //只有道路
	    public ArrayList<JSONObject> points = new ArrayList<JSONObject>();
	}
	
	
	public CutResult   addressCut(String city, String address)
	{
		System.out.print(city);
		City c = getCityInfo(city);
		
		if(c == null)
			return null;
		
		CutResult result = new CutResult();
		result.city = c;
		//System.out.println(c.admincode);
		result.address = address;
		
		//�?
		provinceCut(result);
		//�?
		cityCut(result);
		//区县
		districtCut(result);
		
		
		address = result.address;
		if(address.isEmpty()){
			address = result.cutwords.get(result.cutwords.size()-1).words;
		}
		
		String admin = "";
		if(result.district != null)
			admin = result.district.admincode + "";
		try {
			LocalSearchLucene.GCSearchPOI(address, Long.parseLong(admin), result.points);
		} catch (Exception e) {
			System.out.println("lucene查询参数错误(poi)");
		}
		//道路门牌  优先切道路门牌，如果道路门牌切失败，那么先查乡镇再切道路门牌
		/*if(!roadAndDoorCut(result))
		{
			//乡镇街道
			blockCut(result);
			//道路门牌
			roadAndDoorCut(result);
		}*/
 
	    //切词完毕之后，如果查询的结果不到10个，那么根据剩余的地�?或�?�最后一个切词，再次查询poi
		if(result.points.size()<10)
		{
			//乡镇街道
			blockCut(result);
			//道路门牌
			roadAndDoorCut(result);
			
			address = result.address;
			if(address.isEmpty()){
				address = result.cutwords.get(result.cutwords.size()-1).words;
			}
			
			//String admin = "";
			if(result.district != null)
				admin = result.district.admincode + "";
			try {
				LocalSearchLucene.GCSearchPOI(address, Long.parseLong(admin), result.points);
			} catch (Exception e) {
				System.out.println("lucene查询参数错误(poi)");
			}
			
		}
		
		return result;
		
	}
	//省级切词
	private boolean   provinceCut( CutResult result)
	{
		synchronized(provinces){
		 	
			Iterator<Province> it= provinces.iterator();
			while(it.hasNext()){
				Province pro = it.next();
				 
				if(pro.cutAddress(result, 2))
				{
					result.province = pro;
					return true;
				}
			}
		}
		return false;
	}
	
	//市级切词 
	private boolean cityCut(CutResult result) 
	{
		//查询city的基本信�?

		return result.city.cutAddress(result, result.city.admincode < 100 ? 2 : 3);
		  
	}
	//县级切词   
	private boolean districtCut(CutResult result)
	{
		 
		//获取市级的所有县�?
		City c = result.city;
		if(c == null)
			return false;

		try {
			PreparedStatement smt = dbcon.prepareStatement("select admincode,adminname from admin where cityname = ?");
			smt.setString(1, c.fname);
			//smt.setString(1, result.address);
			ResultSet rs = smt.executeQuery();
			while(rs.next()){
				 
				District dic = new District(rs.getString(2));
				dic.admincode = rs.getInt(1);
				
				if(dic.cutAddress(result,4))
				{
					result.district = dic;
					return true;
				}
			}
			
		} catch (SQLException e) {
			 
		}
 
		return false;
	}
	//乡镇�?
	private boolean blockCut(CutResult result) 
	{ 
		//获取当前�? 或�?? 当前县的�?�? 镇名
		City c = result.city;
		if(c == null)
			return false;
		
		//如果有县级信�?
		District dic = result.district;
		
		String sql = "select  name from v_poi_hamlet  where ";
		if(dic != null)
		{
			sql += "admincode = " + dic.admincode;
		}
		else 
		{
			if(c.admincode < 100)
				sql += "admincode/10000 = " + c.admincode;
			else
				sql += "admincode/100 = " + c.admincode;
		}
		
		try {
			PreparedStatement smt = dbcon.prepareStatement(sql);
			ResultSet rs = smt.executeQuery();
			while(rs.next()){
				 
				Block block = new Block(rs.getString(1));
 
				if(block.cutAddress(result,6))
				{
					result.block = block;
					return true;
				}
			}
			
		} catch (SQLException e) {
		}
		
		return false;
	}
	
	private String processDoor(String src){
		
		//1,去掉�?
		src = src.replace("�?","");
		src = src.replace("0","�?");
		src = src.replace("1","�?");
		src = src.replace("2","�?");
		src = src.replace("3","�?");
		src = src.replace("4","�?");
		src = src.replace("5","�?");
		src = src.replace("6","�?");
		src = src.replace("7","�?");
		src = src.replace("8","�?");
		src = src.replace("9","�?");
		return src;
	}
	//道路门牌
	private boolean roadAndDoorCut(CutResult result)
	{
		String address = new String(result.address);
		//System.out.println("门牌"+address);
		Pattern p = Pattern.compile("[1-9][0-9]*�?");
		
		Matcher m = p.matcher(address); 
 
		if (m.find()) { 
			int idx = m.end();
  
			String roadAndDoor = address.substring(0,idx);
			
			String admin = "";
			if(result.district != null)
				admin = result.district.admincode + "";
			
			//如果能查到这个地�?，那么认为切词成�?
			//这里根据四维的数据再做一些处�?
			//
			String  dst = processDoor(roadAndDoor);
			
			if(LocalSearchLucene.GCSearchPOI(dst,Long.parseLong(admin), result.points)){
				
				
				sliptRoadAndDoor(roadAndDoor, result);
				result.roadAndDoor = roadAndDoor;
				result.address = address.substring(idx);
				return true;
			}
		 
		} 
		 
		//如果道路和门牌一起失败，那么单独切路
		return roadCut(result);
	}
	
	private void sliptRoadAndDoor(String roadAndDoor, CutResult result)
	{
		Pattern p = Pattern.compile("路|道|街|巷|里|弄|条|胡同");
		
		Matcher m = p.matcher(roadAndDoor); 
 
		if (m.find()) { 
			int idx = m.end();
 
 
			String road = roadAndDoor.substring(0,idx);
			
			String door = roadAndDoor.substring(idx);
			
			result.cutwords.add(new CutUnit(road, 10)); 
			
			result.cutwords.add(new CutUnit(door, 17)); 
			
			result.road = road;
		}
	}
	//道路切词
	private boolean roadCut(CutResult result)
	{
		String address = new String(result.address);
		
		Pattern p = Pattern.compile("路|道|街|巷|里|弄|条|胡同");
		
		Matcher m = p.matcher(address); 
 
		if (m.find()) { 
			int idx = m.end();
 
			String road = address.substring(0,idx);
			
			String admin = "";
			if(result.district != null)
				admin = result.district.admincode + "";
			try{
			if(LocalSearchLucene.GCSearchPOI(road,Long.parseLong(admin),  result.points)){
				
				
				result.cutwords.add(new CutUnit(road, 10)); 
				
				result.road = road;
				result.address = address.substring(idx);
				return true;
			}
			}
			catch (Exception e) {
				System.out.println("lucene查询参数错误(road)");
			}
		 
		} 
 
		return false;	
	}
  
	private City getCityInfo(String city){
	
		//先从当前缓存中找，如果有那么返回，否则从数据库查�?
		synchronized (cities) {
			try {
				if (cities.containsKey(city)) {
					return cities.get(city);
				}
				City c = null;

				//PreparedStatement statement = dbcon.prepareStatement("select ccode,cname from admin_city where cname like ? ");
				PreparedStatement statement = dbcon.prepareStatement("select cityadcode,cityname from admin where cityname like ? ");

				statement.setString(1, city + "%");
				ResultSet rs = statement.executeQuery();
				if (rs.next()) {
					c = new City(rs.getString(2));
					c.admincode = rs.getInt(1);
				}
				if (c != null) {
					cities.put(c.fname, c);
					cities.put(c.name, c);
				}
				return c;

			} catch (SQLException e) {

			}
		}
		return null;
	}
	
    
	//行政区划
	public class TwoName {
		public TwoName(String f){
			fname = f;
		}
		protected boolean getName(String unit){
			int idx = fname.indexOf(unit);
			if(idx == fname.length() - unit.length()){
				name = fname.substring(0,idx);
				
				adminunit = unit;
				return true;
			}
			return false;
		}
		protected boolean getNationName(){
			//遍历55个少数民�?				
			for(int i = 0; i < GCCach.nations.size();i++)
			{
				String n = GCCach.nations.get(i);
				
				int idx = fname.indexOf(n);
				
				if(idx > 0)
				{
					name = fname.substring(0, idx);
					adminunit =  fname.substring(idx);
					return true;
				}
			}
			return false;
		}
		public String fname;  //全称 比如 北京�?  陕西�?
		public String name;   //去掉行政单位 比如 北京  陕西
		public String adminunit; //行政单位
		public int admincode; //行政区划编码
 
		
		private CutIndex indexOf(String address){
			int idx  = address.indexOf(fname);
			if(idx >= 0){
				CutIndex ridx = new  CutIndex();
				ridx.idx = idx;
				ridx.len = fname.length();
				return ridx ;
			}
			 
			idx  = address.indexOf(name);
			if(idx >= 0){
				CutIndex ridx = new  CutIndex();
				ridx.idx = idx;
				ridx.len = name.length();
				return ridx;
			}
			return null;
		}
		
		
		public boolean cutAddress(CutResult result, int level){
			
			String address = new String(result.address);
			//int admincode=new Integer(result.city.admincode);
			
			CutIndex  idx = indexOf(address);
			if(idx == null)
				return false;
			 
			if(idx.idx > 0)
			{				
				String before = address.substring(0,idx.idx);
				result.cutwords.add(new CutUnit(before,0));
			}
			
			String word = address.substring(idx.idx, idx.len+idx.idx);
			result.cutwords.add(new CutUnit(word, level)); 
			
			result.address = address.substring(idx.idx + idx.len);

			return true;
		}
	}
	

	 //省级
	public class Province extends TwoName{

		public Province(String f) {
		   super(f);
			
		   if(!getName("�?")  &&  !getName("�?")  && !getNationName()){
			   name = f;
		   }	
		}
    	
    }

	//�?
	public class City extends TwoName{
	 	
		public City(String f){
			super(f);
			
			if(!getName("�?") && !getNationName()){
				   name = f;
			}
		}
	}
	//区县
	public class District extends TwoName{

		public District(String f){
			super(f);
			if(!getName("�?") && !getNationName() && !getName("�?")  && !getName("�?")){
				   name = f;
			} 
		}
	}
	//乡镇
	public class Block extends TwoName{

		public Block(String f){
			super(f);
			if(!getName("�?") && !getName("�?") && !getName("街道")&&!getNationName()){
				   name = f;
			} 
		}
	}
}
