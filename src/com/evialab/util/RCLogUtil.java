package com.evialab.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RCLogUtil {

	static RCLogUtil g_instance = null;

	public static RCLogUtil getInstance() {
		if (g_instance == null) {
			g_instance = new RCLogUtil("d:\\rc.log");
		}
		return g_instance;
	}

	PrintWriter out = null;

	public RCLogUtil(String path) {

		try {
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path,true)), true);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void Stop() {
		if (out != null) {
			out.close();
			out = null;
		}
	}

	public void Log(String uid, String start, String end, String type) {
		if (out != null) {
			out.println(uid + "\t" + start + "\t" + end + "\t" + type);
			out.flush();
		}
	}

	public void LogNativeRes(String res) {
		if (out != null) {
			out.println("res:" + res);
			out.flush();
		}
	}

}
