/**
 * 
 */
package com.evialab.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.json.JSONObject;
import org.json.XML;

/**
 * @author Administrator
 *
 */
public class ResponseUtil {

	public static boolean writeJson2Response(HttpServletResponse response, String outputparam, String encoding, String callback, JSONObject jresult, boolean zip) throws UnsupportedEncodingException, IOException {

		if (encoding != null && encoding.equals("gbk")) {
			encoding = "gbk";
		} else {
			encoding = "utf-8";
		}

		JSONObject jobj = new JSONObject();
		jobj.put("status", "ok");
		jobj.put("result", jresult);

		// 如果�?要xml，那么返�?
		String result = "";
		boolean xmlorjson = true;
		if (outputparam != null)
			xmlorjson = !outputparam.equals("json");
		if (xmlorjson) {
			response.setContentType("text/xml;charset=" + encoding);
			result = XML.toString(jobj, "xml");

			try {
				result = formatXml(result, encoding);
			} catch (Exception e) {

				return false;
			}
		}
		// 如果是json 并且有回调函�?
		else {
			response.setContentType("text/javascript;charset=" + encoding);
			result = jobj.toString();
			if (callback != null) {
				result = callback + "&&" + callback + "(" + result + ")";
			}
		}

		// 这里执行zip压缩
		byte[] data = result.getBytes(encoding);
		if (zip) {
			ByteArrayOutputStream stm = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(stm);

			gos.write(data);
			gos.finish();

			stm.flush();
			stm.close();

			data = stm.toByteArray();
			response.setHeader("Content-Encoding", "gzip");
		}

		response.getOutputStream().write(data);
		return true;
	}

	public static boolean writeJson2Response(HttpServletResponse response, String outputparam, String encoding, String callback, JSONObject jresult) throws UnsupportedEncodingException, IOException {

		return writeJson2Response(response, outputparam, encoding, callback, jresult, false);
	}

	public static boolean error(HttpServletResponse response, String error) throws UnsupportedEncodingException, IOException {

		return error(response, null, null, null, error);
	}

	public static boolean error(HttpServletResponse response, String outputparam, String encoding, String callback, String error) throws UnsupportedEncodingException, IOException {

		JSONObject js = new JSONObject();
		js.put("status", "error");
		js.put("error", error);

		if (encoding != null && encoding.equals("gbk")) {
			encoding = "gbk";
		} else {
			encoding = "utf-8";
		}

		// 如果�?要xml，那么返�?
		String result = "";
		boolean xmlorjson = true;
		if (outputparam != null)
			xmlorjson = !outputparam.equals("json");
		if (xmlorjson) {
			response.setContentType("text/xml;charset=" + encoding);
			result = XML.toString(js, "xml");

			try {
				result = formatXml(result, encoding);
			} catch (Exception e) {

				return false;
			}
		}
		// 如果是json 并且有回调函�?
		else {
			response.setContentType("text/javascript;charset=" + encoding);
			result = js.toString();
			if (callback != null) {
				result = callback + "&&" + callback + "(" + result + ")";
			}
		}
		response.getOutputStream().write(result.getBytes(encoding));
		return true;
	}

	public static boolean writeError2Response(HttpServletResponse response, String outputparam, String encoding, JSONObject jresult) throws UnsupportedEncodingException, IOException {

		if (encoding != null && encoding.equals("gbk")) {
			encoding = "gbk";
		} else {
			encoding = "utf-8";
		}

		// 如果�?要xml，那么返�?
		String result = "";
		boolean xmlorjson = true;
		if (outputparam != null)
			xmlorjson = !outputparam.equals("json");
		if (xmlorjson) {
			result = XML.toString(jresult, "xml");

			try {
				result = ResponseUtil.formatXml(result, encoding);

				response.setContentType("text/xml;charset=" + encoding);
				response.setCharacterEncoding(encoding);
				response.getOutputStream().write(result.getBytes(encoding));

				return true;
			} catch (Exception e) {

				return false;
			}
		}
		// 如果是json 并且有回调函�?
		else {
			result = jresult.toString();
			response.setCharacterEncoding(encoding);
			response.setContentType("text/javascript;charset=" + encoding);
			response.getOutputStream().write(result.getBytes(encoding));
			return true;
		}

	}

	// 把xml字符串转为带可读格式�? 默认为utf8编码
	public static String formatXml(String str) throws Exception {
		return formatXml(str, "utf-8");
	}

	// 把xml字符串转为带可读格式�?
	public static String formatXml(String str, String encoding) throws Exception {
		Document document = null;
		document = DocumentHelper.parseText(str);
		// 格式化输出格�?
		OutputFormat format = OutputFormat.createPrettyPrint();
		format.setEncoding(encoding);
		format.setIndent(" ");

		StringWriter writer = new StringWriter();
		// 格式化输出流
		XMLWriter xmlWriter = new XMLWriter(writer, format);
		// 将document写入到输出流
		xmlWriter.write(document);
		xmlWriter.close();
		return writer.toString();
	}

	public static byte[] compress(byte[] data) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// 压缩
		GZIPOutputStream gos = new GZIPOutputStream(baos);

		gos.write(data, 0, data.length);

		gos.finish();

		byte[] output = baos.toByteArray();

		baos.flush();
		baos.close();

		return output;
	}

}
