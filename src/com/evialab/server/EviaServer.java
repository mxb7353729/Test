package com.evialab.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.evialab.util.DBPool;

public class EviaServer extends TimerTask {

	final static int Interval = 10 * 60 * 1000; // 10分钟刷新间隔

	String servername = "";
	int serverid = 0;

	Timer mTimer = null;

	HashMap<String, Integer> service2id = new HashMap<String, Integer>();
	HashMap<String, Integer> uid2id = new HashMap<String, Integer>();
	HashMap<VisitKey, VisitSta> records = new HashMap<VisitKey, VisitSta>();
	
	Object locker = new Object();

	// 初始化操作，通过servername查询serverid，如果没有，那么添加此服务到server�? 并且刷新服务类型
	public boolean init(String sname) {

		Connection con = null;
		try {
			con = DBPool.getServiceInstance().getConnection();

			servername = sname;
			getServerID(con);
			refreshUID(con);
			refreshService(con);
			

		} catch (Exception e) {

			System.out.println("EviaServer初始化失�?:" + e.getLocalizedMessage());
			return false;
		} finally {
			if (con != null)
				DBPool.getServiceInstance().closeConnection(con);
		}

		System.out.println("EviaServer 初始化成�?");
		// �?启定时刷�?

		mTimer = new Timer();
		mTimer.schedule(this, Interval, Interval);

		return true;
	}

	// 服务器关闭前的处�?
	public void close() {
		// 停止定时�?
		mTimer.cancel();

		// 启动记录保存
		Connection con = null;
		try {
			con = DBPool.getServiceInstance().getConnection();

			// 记录当前内存访问次数, 清空�?有访问次�?
			record(con);

			return;

		} catch (Exception e) {

			System.out.println("close函数失败�?" + e.getLocalizedMessage());
			return;
		} finally {
			if (con != null)
				DBPool.getServiceInstance().closeConnection(con);
		}
	}

	// �?测某个UID是否有效，并且记录对该类服务的访问次�?
	public boolean processUID(String uid, String service, String userip, int count) {

		// 如果不是有效的UID,那么返回
		int userid = 0;
		if(uid.isEmpty()){
			userid = 0;  //如果传过来的uid是空的，那么认为是我们的临时uid
		}
		else{
			synchronized (locker) {
				if (!uid2id.containsKey(uid))
					return false;
				userid = uid2id.get(uid);
			}
		}
		

		// 如果服务不存在，那么添加到数据库，并且重新刷新服务类�?
		int serviceid = 0;
		synchronized (locker) {
			if (!service2id.containsKey(service))
			{
				serviceid = getNewServiceID(service);
				service2id.put(service, serviceid);
			}
			else
				serviceid = service2id.get(service);
		}

		VisitKey vk = new VisitKey(userid, serviceid, userip);
		synchronized (locker) {
			if (!records.containsKey(vk)) {
				VisitSta st = new VisitSta(count);
				records.put(vk, st);
			} else {
				records.get(vk).count += count;
			}
		}

		return true;
	}

	// 定时函数
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Connection con = null;
		try {
			con = DBPool.getServiceInstance().getConnection();
			// 加载有效用户uid
			if (!refreshUID(con)) {
				System.out.println("刷新UID有效性失败，请检查数据库连接");
				return;
			}
			// 记录当前内存访问次数, 清空�?有访问次�?
			record(con);
			System.out.println(new Date()+"定时刷新完成");
			return;

		} catch (Exception e) {

			System.out.println("定时刷新失败�?" + e.getLocalizedMessage());
			return;
		} finally {
			if (con != null)
				DBPool.getServiceInstance().closeConnection(con);
		}
	}

	// 刷新有效用户uid映射
	boolean refreshUID(Connection con) throws SQLException {

		ResultSet rs = con.createStatement().executeQuery("select * from t_client where state = 1");

		synchronized (locker) {
			uid2id.clear();
			while (rs.next()) {
				uid2id.put(rs.getString("uid"), rs.getInt("id"));
			}
		}
		rs.close();
		return true;
	}

	// 刷新服务类型映射
	boolean refreshService(Connection con) throws SQLException {
		ResultSet rs = con.createStatement().executeQuery("select * from t_service");

		synchronized (locker) {
			service2id.clear();
			while (rs.next()) {
				service2id.put(rs.getString("name"), rs.getInt("id"));
			}
		}
		rs.close();
		return true;
	}

	// 记录�?有访问记�?
	boolean record(Connection con) throws SQLException {

		String sql = "INSERT INTO t_record(serverid, serviceid, cilentid, userip, count, btime)   VALUES (  ?, ?, ?, ?, ?, ?)";

		PreparedStatement stm = con.prepareStatement(sql);

		synchronized (locker) {

			Set<VisitKey> keys = records.keySet();

			for (Iterator<VisitKey> it = keys.iterator(); it.hasNext();) {
				VisitKey vk = it.next();
				VisitSta vv = records.get(vk);

				

				stm.setObject(1, serverid);
				stm.setObject(2, vk.serviceid);
				stm.setObject(3, vk.userid);
				stm.setObject(4, vk.userip);
				stm.setObject(5, vv.count);
				stm.setObject(6, new Timestamp(vv.begintime.getTime()));
				stm.addBatch();
			}
			stm.executeBatch();
			records.clear();
		}

		return true;
	}

	// 添加新的服务类型，并且刷新新的服务映�?
	int getNewServiceID(String service) {
		Connection con = null;
		try {
			con = DBPool.getServiceInstance().getConnection();
			// 插入新的服务类型，并且获取插入的UID
			PreparedStatement stm = con.prepareStatement("insert into t_service(name) values(?)", Statement.RETURN_GENERATED_KEYS);

			stm.setObject(1, service);
			stm.execute();
			ResultSet rs = stm.getGeneratedKeys();
			rs.next();

			int id = rs.getInt(1);

			stm.close();

			
			return id;

		} catch (Exception e) {

			System.out.println("插入新服务失败：" + e.getLocalizedMessage());
			return 0;

		} finally {
			if (con != null)
				DBPool.getServiceInstance().closeConnection(con);
		}
	}

	// 获取服务器ID，如果查询失败，那么插入�?�?
	boolean getServerID(Connection con) throws SQLException {

		ResultSet rs = con.createStatement().executeQuery("select id from t_server where name='" + servername + "'");
		if (rs.next()) {

			serverid = rs.getInt(1);
			rs.close();
			return true;
		}
		rs.close();

		// 插入新的服务类型，并且获取插入的UID
		PreparedStatement stm = con.prepareStatement("insert into t_server(name) values(?)", Statement.RETURN_GENERATED_KEYS);

		stm.setObject(1, servername);
		stm.execute();

		rs = stm.getGeneratedKeys();
		rs.next();

		serverid = rs.getInt(1);

		stm.close();

		return true;
	}

	class VisitKey {
		public int userid = 0;
		public int serviceid = 0;
		public String userip = "";

		public VisitKey(int u, int s, String ip) {
			userid = u;
			serviceid = s;
			userip = ip;
		}

		@Override
		public boolean equals(Object obj) {
			VisitKey vk = (VisitKey) obj;
			return this.userid == vk.userid && this.serviceid == vk.serviceid && this.userip.equals(vk.userip);
		}

		@Override
		public int hashCode() {
			String sl = userip + "_" + userid + "_" + serviceid;
			return sl.hashCode();
		}

	}

	class VisitSta {
		public int count = 0;
		public Date begintime = new Date();

		public VisitSta(int cnt) {
			count = cnt;
		}
	}

}
