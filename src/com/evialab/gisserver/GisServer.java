package com.evialab.gisserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.evialab.app.IndexAdminFromDB;
import com.evialab.app.IndexRoadFromDB;
import com.evialab.common.MapnikRender;
import com.evialab.common.RGCRecord;
import com.evialab.common.FullDataReader;
import com.evialab.server.EviaServer;
import com.evialab.the3rd.Traffic;
import com.evialab.util.ArcGisBundleReader;
import com.evialab.util.DBPool;
import com.evialab.util.RCLogUtil;
import com.evialab.util.RSAUtil;
import com.evialab.util.RequestUtil;
import com.jetmap.route.RouteApi;

public class GisServer implements ServletContextListener {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("test");
	}   

	@Override
	public void contextInitialized(ServletContextEvent arg0) {

		// 链接基础数据�??
		ServletContext context = arg0.getServletContext();
		context.setAttribute("GisServer", this);

		// 解析配置 获取 �?有的地图配置
		try {
			String cfgpath = context.getRealPath("/") + "conf/gisserverconfig.xml";

			SAXReader saxReader = new SAXReader();
			Document doc = saxReader.read(new File(cfgpath));

			demoUIDGe = new DemoUIDGenerator();

			try {
				// 加载其他服务器的配置
				servers = doc.selectSingleNode("config/servers").getText().trim().split("\n");
				if (servers != null) {
					for (int i = 0; i < servers.length; i++) {
						servers[i] = servers[i].trim();
					}
				}
			} catch (Exception ex) {
				System.out.println("初始化服务器配置失败");
			}

			// 初始化导航引擎设�??
			try {

				Node proxy = doc.selectSingleNode("config/route/proxy");
				if (proxy != null) {
					RouteApi.proxyserver = proxy.getText().trim();
				}

				Node jnipath = doc.selectSingleNode("config/route/jnipath");
				Node routeKey = doc.selectSingleNode("config/route/key");
				Node routePath = doc.selectSingleNode("config/route/datapath");

				if (jnipath != null && routeKey != null && routePath != null) {
					RouteApi.initRouteEngine(jnipath.getText(), routeKey.getText(), routePath.getText());
				} else {
					System.out.println("导航引擎配置错误");
				}
			} catch (Exception ex) {
				System.out.println("初始化导航引擎配置失�?");
			}
			// 设置rgc存储的路�?
			Node rgclogpath = doc.selectSingleNode("config/rgclogpath");
			if (rgclogpath != null) {
				String path = rgclogpath.getText().trim();
				if (path.lastIndexOf(java.io.File.separator) != path.length() - 1) {
					path = path + java.io.File.separator;
				}
				Node open = doc.selectSingleNode("config/rgclogpath/open");
				boolean bopen = (open != null) && (open.getText().equals("true"));

				rgcrecord = new RGCRecord(path, bopen);
				System.out.println("rgc保存路径设置成功");
			}

	 

			// 设置POI索引路径
			Node nSearchPath = doc.selectSingleNode("config/searcher");
			if (nSearchPath != null) {
				m_searchPath = nSearchPath.getText().trim();
				LocalSearchLucene.InitSearcher(m_searchPath);

				System.out.println("搜索索引路径设置成功: " + m_searchPath);
			}

			// 初始化地理数据的 数据库查询连�?
			try {
				String dbserver = doc.selectSingleNode("config/dataconnection/server").getText().trim();
				String dbuser = doc.selectSingleNode("config/dataconnection/user").getText().trim();
				String dbpassword = doc.selectSingleNode("config/dataconnection/password").getText().trim();
				DBPool.initDataInstance(dbserver, dbuser, dbpassword);

				Connection con = DBPool.getDataInstance().getConnection();
				if (con == null) {
					System.out.println("数据数据库连接失�?" + dbserver + "," + dbuser + "," + dbpassword);

				} else {
					System.out.println("数据数据库连接成�?," + dbserver + "," + dbuser + "," + dbpassword);
					DBPool.getDataInstance().closeConnection(con);
				}

			} catch (Exception e) {
				System.out.println("数据数据库连接失败：" + e.getMessage());
			}

			// 初始化运维支撑的数据库连�?
			try {

				String dbserver = doc.selectSingleNode("config/serverconnection/server").getText().trim();
				String dbuser = doc.selectSingleNode("config/serverconnection/user").getText().trim();
				String dbpassword = doc.selectSingleNode("config/serverconnection/password").getText().trim();
				DBPool.initServiceInstance(dbserver, dbuser, dbpassword);

				Connection con = DBPool.getServiceInstance().getConnection();
				if (con == null) {
					System.out.println("运维数据库连接失�?" + dbserver + "," + dbuser + "," + dbpassword);

				} else {
					System.out.println("运维数据库连接成�?," + dbserver + "," + dbuser + "," + dbpassword);
					DBPool.getServiceInstance().closeConnection(con);
				}

				String servername = doc.selectSingleNode("config/servername").getText().trim();

				eviaserver = new EviaServer();

				if (eviaserver.init(servername)) {
					System.out.println("运维初始化成功，当前服务器名�?:" + servername);
				}

			} catch (Exception e) {
				System.out.println("运维数据库连接失败：" + e.getMessage());
			}

			// 初始化行政区划相关的
			admin = new ChinaAdmin();

			if (admin.init())
				System.out.println("行政区划数据库设置成功�??");
			else
				System.out.println("行政区划数据库设置失败�??");

			// 初始化交通流更新引擎
			Node nTraffic = doc.selectSingleNode("config/traffic");
			if (nTraffic != null) {
				try {
					Node dbserver = doc.selectSingleNode("config/traffic/server");
					Node dbuser = doc.selectSingleNode("config/traffic/user");
					Node dbpassword = doc.selectSingleNode("config/traffic/password");

					traffic = new Traffic(dbserver.getText(), dbuser.getText(), dbpassword.getText());
				} catch (Exception e) {
					System.out.println("实时路况引擎初始化失败：" + e.getMessage());
				}
			}
			// 初始化交通流出图引擎
			Node nRenderJNI = doc.selectSingleNode("config/trafficrender/jnipath");
			if (nRenderJNI != null) {
				MapnikRender.loadJNI(nRenderJNI.getText().trim());
				Node nMapfile = doc.selectSingleNode("config/trafficrender/mapfile");
				Node nPlugins = doc.selectSingleNode("config/trafficrender/plugindir");
				Node nFonts = doc.selectSingleNode("config/trafficrender/fontsdir");

				if (nMapfile != null) {
					render = new MapnikRender();
					if (render.init(nMapfile.getText().trim(), nPlugins.getText().trim(), nFonts.getText().trim())) {
						System.out.println("map render server inited success");
					} else
						System.out.println("map render server inited failed");
				} else {
					System.out.println("config.xml render config error");
				}
			} else {
				System.out.println("config.xml render config error");
			}

			// 设置缓存路径
			Node cachpath = doc.selectSingleNode("config/cachpath");
			if (cachpath != null) {
				CachUtil.cachpath = cachpath.getText().trim();

				System.out.println("缓存路径设置成功: " + CachUtil.cachpath);
			}

			// 设置新数据的路径
			Node nVectlayer = doc.selectSingleNode("config/tilepath2/vect");
			if (nVectlayer != null) {
				vectlayer = new ArcGisBundleReader();
				vectlayer.SetTilePath(nVectlayer.getText().trim());
				System.out.println(nVectlayer);

			} else {
				System.out.println("vectlayer 配置访问失败");
			}
		 
			// 设置成果数据
			Node fulldata = doc.selectSingleNode("config/fulldata");

			if(!FullDataReader.getDefaultInstance().LoadConfig(fulldata)){
				System.out.println("设置成果数据失败");
			}
			 
			// 设置道路查询的lucene索引路径
			Node raodindex = doc.selectSingleNode("config/raodindex");
			if(raodindex != null)
			{
				IndexRoadFromDB.IndexPath = raodindex.getText().trim();
			}
			 
			// 设置行政区划查询的lucene索引路径
			Node adminindex = doc.selectSingleNode("config/adminindex");
			if(adminindex != null)
			{
				IndexAdminFromDB.IndexPath = adminindex.getText().trim();
			}
			 
			
			// 预打�?空图文件
			blankimage = readfile(context, "blank.png");
			// 打开js

			// 兼容泰瑞的所有js
			evtrjs = readfile(context, "evtr_z.js");
			tr_server = readfile(context, "tr_server_z.js");
			tr_maptool = readfile(context, "tr_maptool_z.js");
			tr_cluster = readfile(context, "tr_cluster_z.js");
			tr_mappicker = readfile(context, "tr_mappicker_z.js");
			tr_mapsnap = readfile(context, "tr_snapmap_z.js");
			tr_citylist = readfile(context, "tr_citylist_z.js");
			tr_mapeditor = readfile(context, "tr_mapeditor_z.js");
			tr_mobile = readfile(context, "tr_mobile_z.js");

			// 非压缩的ev js文档
			openlayers_evjs = readfile(context, "openlayers_ev.js");
			openlayers_compjs = readfile(context, "openlayers_comp.js");
			ev_serverjs = readfile(context, "ev_server.js");
			ev_clusterjs = readfile(context, "ev_cluster.js");
			openlayers = readfile(context, "OpenLayers.debug.js");
			eviamapex_oljs = readfile(context, "eviamapex_ol.js");
			eviamapex_src_oljs = readfile(context, "eviamapex_src_ol.js");

			// 压缩的ev js
			eviamap_oljs = readfile(context, "eviamap_ol.js");
			eviamapjs = readfile(context, "eviamap.js");
			staticMapViewhtml = new String(readfile(context, "staticMapView.html"), "utf8");

			try {
				defaultIcon = ImageIO.read(new ByteArrayInputStream(readfile(context, "marker.png")));
			} catch (Exception e) {

			}

			File f = new File(context.getRealPath("/"));
			project = f.getName();
			if (project.equals("ROOT"))
				project = "";
			else
				project = "/" + project;

			System.out.println("当前工程:" + project);

			// 设置rsa加密文件
			RSAUtil.RSAKeyStore = context.getRealPath("/") + "conf/RSAKey.txt";

		} catch (Exception e) {
			System.out.println("map data inited failed");
			System.out.println(e.getMessage());
		}
	}

	private byte[] readfile(ServletContext context, String webinffile) {
		try {
			String fevjs = context.getRealPath("/") + "rtjs" + "/" + webinffile;

			File f = new File(fevjs);
			FileInputStream fis = new FileInputStream(f);
			byte[] by = new byte[(int) f.length()];
			fis.read(by);
			fis.close();

			return by;
		} catch (Exception e) {
			System.out.println(webinffile + " read failed");
			return null;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {

		// 关闭数据
		try {

			if (rgcrecord != null)
				rgcrecord.serverClose();

			if (demoUIDGe != null)
				demoUIDGe.serverClose();

			if (traffic != null)
				traffic.close();

			if (render != null)
				render.release();

			if (eviaserver != null)
				eviaserver.close();

			if (admin != null)
				admin.destory();
			
			RCLogUtil.getInstance().Stop();

		} catch (Exception e) {
			System.out.println("close gis common db failed" + e.getMessage());
		}
	}

	public boolean processUID(HttpServletRequest req, String service, int count) {

		if (eviaserver == null)
			return false;

		String uid = RequestUtil.getParameter(req, "uid");
		if (uid.equals(getTodayDemoUID()))
			uid = "";

		String userip = RequestUtil.getIp(req);

		return eviaserver.processUID(uid, service, userip, count);
	}

	public boolean processUID(HttpServletRequest req, String service) {

		return processUID(req, service, 1);
	}

	// 获取当日有效的demoUID
	DemoUIDGenerator demoUIDGe = null;

	public String getTodayDemoUID() {
		return demoUIDGe.getTodayDemoUID();
	}

	public String getSessionUID(HttpSession session) {
		String username = (String) session.getAttribute("uid");
		if (username != null)
			return username;

		return getTodayDemoUID();
	}

	String m_tilePath = "";
	String m_searchPath = "";

	public byte[] blankimage = null;

	// 泰瑞地图的js文件
	public byte[] evtrjs = null;
	public byte[] tr_server = null;
	public byte[] tr_maptool = null;
	public byte[] tr_cluster = null;
	public byte[] tr_mappicker = null;
	public byte[] tr_mapsnap = null;
	public byte[] tr_citylist = null;
	public byte[] tr_mapeditor = null;
	public byte[] tr_mobile = null;

	// 未经压缩的js文件
	public byte[] openlayers_evjs = null;
	public byte[] openlayers_compjs = null;
	public byte[] ev_clusterjs = null;
	public byte[] ev_serverjs = null;
	public byte[] openlayers = null;
	public byte[] eviamapex_src_oljs = null;

	// 经过压缩的包含openlayers的js文件
	public byte[] eviamap_oljs = null;
	public byte[] eviamapjs = null;
	public byte[] eviamapex_oljs = null;

	// 配置的服务器地址
	public String[] servers = null;

	public String staticMapViewhtml = "";

	public String project = "";

	public BufferedImage defaultIcon = null;

	public ChinaAdmin admin = null;

	// 记录rgc
	public RGCRecord rgcrecord;

	// 实时路况
	public Traffic traffic = null;

	// 路况出图
	public MapnikRender render = null;

	// 以后可能涉及多个图层，所以这里都定义为对象形式的
	public ArcGisBundleReader vectlayer = null;

	// 服务的运维支撑，包括UID有效性检�? �? 服务访问统计
	private EviaServer eviaserver = null; 

}
