package com.evialab.gisserver;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.evialab.util.RequestUtil;
import com.evialab.util.ResponseUtil;

public class StaticMap extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		req.setCharacterEncoding("UTF-8");
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		resp.setHeader("Access-Control-Allow-Origin", "*");
		ServletContext context = getServletContext();
		GisServer server = (GisServer) context.getAttribute("GisServer");
		if (server == null) {
			ResponseUtil.error(resp, "内部错误");
			return;
		}

		if (!server.processUID(req, "SE_StaticAPI")) {
			ResponseUtil.error(resp, "无效UID");
			return;
		}

		// 根据请求绘图
		/*
		 * widthheight �? [400,400] 图片宽度和高度�?�取值范围：[50, 2000]�? watermark �? null
		 * 图片水印。取值范围参考用户手册�?? center �? null
		 * 地图中心点位置，参数可以为经纬度坐标。坐标格式：lng<经度>，lat<纬度>，例�?116.43213,38.76623�? zoom �?
		 * 11 地图级别。范围[3,18] boxes �? null 地图视野范围。格式：minX,minY;maxX,maxY�? type �?
		 * view 操作类型。范围[save,view]。m overlaycoordinatetype �? 0
		 * 叠加物坐标类型�?�范围[0(像素坐标)�?1(经纬度坐�?)]�? marks �? null
		 * 标注，格式：经度坐标@纬度坐标，标记点图片链接地址。多个标注之间用 ^ 分隔�? txts �? null
		 * 标签，格式：经度坐标@纬度坐标；文字内�?
		 * ；文字大小；文字颜色r色�?�@g色�?�@b色�?�；文字背景颜色r色�?�@g色�?�@b色�?�；文字背景宽@高�?�多个标签之间用 ^ 分隔。允�?16个字符�??
		 * polylines �? null
		 * 折线，格式：�?1经度坐标@纬度坐标，点n经度坐标@纬度坐标；r色�?�@g色�?�@b色�?�；线宽度；线类型（1为实�?
		 * �?2为虚线）；�?�明度�?�多个折线之间用 ^ 分隔�? polygons �? null
		 * 面，格式：点1经度坐标@纬度坐标，点n经度坐标@纬度坐标�?
		 * 填充色r色�?�@g色�?�@b色�?�；透明度；边框色r色�?�@g色�?�@b色�?�；边框宽度。多个面之间�? ^ 分隔�?
		 */
		try {

			// 根据要求绘图
			MapRender render = new MapRender(server);

			String widthheight = RequestUtil.getParameter(req, "widthheight");
			if (!RequestUtil.isNull(widthheight)) {
				widthheight = widthheight.replace("[", "");
				widthheight = widthheight.replace("]", "");
				int width = Integer.parseInt(widthheight.split(",")[0]);
				int height = Integer.parseInt(widthheight.split(",")[1]);

				render.createGraphic(width, height);
			}

			int zoom = Integer.parseInt(RequestUtil.getParameter(req, "zoom"));

			String boxes = RequestUtil.getParameter(req, "boxes");

			if (!RequestUtil.isNull(boxes)) {
				double w, e, s, n;
				String[] t = boxes.split(",|;");
				w = Double.parseDouble(t[0]);
				s = Double.parseDouble(t[1]);
				e = Double.parseDouble(t[2]);
				n = Double.parseDouble(t[3]);

				render.createGraphicWithBox(w, e, s, n, zoom);
			} else {
				String[] center = RequestUtil.getParameter(req, "center").split(",");
				double lon = Double.parseDouble(center[0]);
				double lat = Double.parseDouble(center[1]);

				render.zoomTo(lon, lat, zoom);
			}

			// 根据范围绘图
			render.render();

			boolean isBL = "1".equals(RequestUtil.getParameter(req, "overlaycoordinatetype"));

			// 绘制多边�?
			String polygons = RequestUtil.getParameter(req, "polygons");
			if (!RequestUtil.isNull(polygons)) {
				String[] polys = polygons.split("=");
				for (int i = 0; i < polys.length; i++) {
					try {
						String[] tt = polys[i].split(";");

						String[] cc = tt[0].split(",");
						double[] x = new double[cc.length];
						double[] y = new double[cc.length];

						for (int k = 0; k < cc.length; k++) {
							x[k] = Double.parseDouble(cc[k].split("@")[0]);
							y[k] = Double.parseDouble(cc[k].split("@")[1]);
						}

						float opacity = Float.parseFloat(tt[2]);
						Color fillcolor = parseColor(tt[1], opacity);
						Color color = parseColor(tt[3]);
						float weight = Float.parseFloat(tt[4]);

						render.renderPoly(x, y, isBL, fillcolor, color, weight);
					} catch (Exception e) {

					}

				}
			}
			// 绘制折线
			String polylines = RequestUtil.getParameter(req, "polylines");

			if (!RequestUtil.isNull(polylines)) {
				String[] lines = polylines.split("=");
				for (int i = 0; i < lines.length; i++) {
					try {
						String[] tt = lines[i].split(";");

						String[] cc = tt[0].split(",");
						double[] x = new double[cc.length];
						double[] y = new double[cc.length];

						for (int k = 0; k < cc.length; k++) {
							x[k] = Double.parseDouble(cc[k].split("@")[0]);
							y[k] = Double.parseDouble(cc[k].split("@")[1]);
						}

						float opacity = Float.parseFloat(tt[4]);
						Color color = parseColor(tt[1], opacity);
						float weight = Float.parseFloat(tt[2]);

						int lineType = Integer.parseInt(tt[3]);

						render.renderLine(x, y, isBL, color, weight, lineType);
					} catch (Exception e) {

					}

				}
			}

			// 绘制其他
			String marks = RequestUtil.getParameter(req, "marks");
			if (!RequestUtil.isNull(marks)) {
				String[] markers = marks.split("=");
				for (int i = 0; i < markers.length; i++) {
					try {
						String[] tt = markers[i].split(",");
						double lon = Double.parseDouble(tt[0].split("@")[0]);
						double lat = Double.parseDouble(tt[0].split("@")[1]);
						// String c = tt[1];
						// String s = tt[2];
						// String n = tt[3];

						// String path = context.getRealPath("/img/tips/") + "/"
						// + c +"/"+ s+"/" + n + ".png";
						String path = tt[1];

						render.renderMarker(lon, lat, isBL, path);
					} catch (Exception e) {

					}

				}
			}

			// 绘制文字
			String txts = RequestUtil.getParameter(req, "txts");
			System.out.println(txts);
			if (!RequestUtil.isNull(txts)) {
				String[] txt = txts.split("=");
				for (int i = 0; i < txt.length; i++) {
					try {
						String[] tt = txt[i].split(";");
						double lon = Double.parseDouble(tt[0].split("@")[0]);
						double lat = Double.parseDouble(tt[0].split("@")[1]);

						String content = tt[1];
						int fontSize = Integer.parseInt(tt[2]);
						Color color = parseColor(tt[3]);
						Color bgcolor = parseColor(tt[4]);
						int bgw = Integer.parseInt(tt[5].split("@")[0]);
						int bgh = Integer.parseInt(tt[5].split("@")[1]);

						 
						render.renderText(lon, lat, isBL, new String[]{content}, fontSize, color, bgcolor, bgw, bgh);
					} catch (Exception e) {

					}

				}
			}

			/*
			 * FileWriter fw = new FileWriter("d:\\param.txt");
			 * 
			 * fw.write(txts); fw.write(polylines); fw.flush(); fw.close();
			 */

			render.setRoot(context.getRealPath("/") + "/", RequestUtil.getBasePath(req));
			render.setDefaultIcon(server.defaultIcon);

			// 我们得overlay
			String overlays = RequestUtil.getParameter(req, "o");
			if (!RequestUtil.isNull(overlays)) {
				// 使用json解析
				try {
					JSONArray obj = new JSONArray(overlays);

					for (int i = 0; i < obj.length(); i++) {

						render.renderOverlay((JSONObject) obj.get(i));
					}

				} catch (Exception e) {

				}

			}

			byte[] data = render.getByte();

			String type = RequestUtil.getParameter(req, "type");
			if ("view".equals(type) || "print".equals(type)) {

				String path = getCachName();

				 File folder = new File(StaticCach.StaticachPath);
				 if(!folder.exists()){
					 folder.mkdirs();
				 }
			   
				String filepath = StaticCach.StaticachPath + path;

				File f = new File(filepath);
				FileOutputStream fis = new FileOutputStream(f);
				fis.write(data);
				fis.close();

				String url = RequestUtil.getBasePath(req) + "/staticcach?f=" + path;

				String html = server.staticMapViewhtml.replace("STATICMAPURL", url);
				byte[] buffer = html.getBytes("utf8");
				resp.setContentLength(buffer.length);
				resp.getOutputStream().write(buffer);
				return;
			} else {
				resp.setContentType("image/png");
				resp.setContentLength(data.length);

				resp.setHeader("Content-disposition", "attachment;filename=staticmap.png");
				//resp.setHeader("filename", "staticmap.png");
				resp.getOutputStream().write(data);
			}

		} catch (Exception ex) {

		}

	}

	private synchronized String getCachName() {
		count++;
		String path = String.valueOf(System.currentTimeMillis()) + String.valueOf(count) + ".png";
		return path;
	}

	Color parseColor(String sc) {
		String[] cc = sc.split("@");
		int R = Integer.parseInt(cc[0]);
		int G = Integer.parseInt(cc[1]);
		int B = Integer.parseInt(cc[2]);
		if (R < 0) {
			R = (~R) & 0x0FF;
			G = (~G) & 0xFF;
			B = (~B) & 0xFF;
		}
		return new Color(R * 1.0f / 255, G * 1.0f / 255, B * 1.0f / 255);
	}

	Color parseColor(String sc, float op) {
		String[] cc = sc.split("@");
		int R = Integer.parseInt(cc[0]);
		int G = Integer.parseInt(cc[1]);
		int B = Integer.parseInt(cc[2]);
		if (R < 0) {
			R = (~R) & 0x0FF;
			G = (~G) & 0xFF;
			B = (~B) & 0xFF;
		}
		return new Color(R * 1.0f / 255, G * 1.0f / 255, B * 1.0f / 255, op);
	}

	static int count = 0;
}
