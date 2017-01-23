package com.evialab.the3rd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.evialab.gisserver.GisServer;
import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

/**
 * Servlet implementation class GetFuel
 */
public class GetFuel extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetFuel() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_Fuel")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}

		try {
			String admincode = RequestUtil.getParameter(req, "admincode");

			String url = "adcode=" + admincode;

			JSONObject result = getFuel(url);

			String output = RequestUtil.getParameter(req, "output");
			String callback = RequestUtil.getParameter(req, "callback");
			ResponseUtil.writeJson2Response(resp, output, "utf-8", callback, result);
		} catch (Exception e) {
			ResponseUtil.error(resp, e.getMessage());
			System.out.println(e.getMessage());
			return;
		}
	}

	// 访问服务器获得json结果
	JSONObject getFuel(String param) throws UnsupportedEncodingException {
		String url = "http://newte.sh.1251225243.clb.myqcloud.com/TEGateway/xxx/Fuel.json?bizcode=06049f184f6340f536102cb8bb57ea98&";
		url += param;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if (getURL(url, output)) {

			String str = output.toString("utf8");

			JSONObject json = new JSONObject(str);
			return json;
		} else {
			System.out.println(url);
			return null;
		}

	}

	boolean getURL(String url, OutputStream stream) {
		try {
			URL getUrl;

			getUrl = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();

			connection.connect();

			byte[] buf = new byte[1024 * 20];

			InputStream instream = connection.getInputStream();

			int nread = 0;

			while ((nread = instream.read(buf)) != -1) {

				stream.write(buf, 0, nread);
			}

			instream.close();

			connection.disconnect();
		} catch (Exception e) {

			return false;
		}
		return true;

	}
}
