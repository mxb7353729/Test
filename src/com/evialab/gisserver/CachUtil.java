package com.evialab.gisserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CachUtil {

	public static String cachpath = java.io.File.separator;
	
	public static String imgPath(long x, long y, int z)
	{
		String baseurl = z + java.io.File.separator;

		// 计算有几级附加目�?
		int nf = z >> 2; // z / 4
		char[] hx = intToHex(x);
		char[] hy = intToHex(y);

		int fcnt = 0;

		// 注意这个顺序，先计算高级
		for (int i = nf - 1; i >= 0; i--) {
			// 取距离最后一个hex值最近的字符
			String ax = "0";
			if (hx.length >= i + 2)
				ax = hx[hx.length - 2 - i] + "";

			String ay = "0";
			if (hy.length >= i + 2)
				ay = hy[hy.length - 2 - i] + "";

			// 这里还需要�?�虑
			if (fcnt >= 2) {
				if (hx.length - 2 - i - 2 >= 0)
					ax = hx[hx.length - 2 - i - 2] + "0" + ax;
				if (hy.length - 2 - i - 2 >= 0)
					ay = hy[hy.length - 2 - i - 2] + "0" + ay;
			}

			int ix = hexToDigit(ax);
			int iy = hexToDigit(ay);

			String f = ((ix < 10) ? ("0" + ix) : (ix + ""))
					+ ((iy < 10) ? ("0" + iy) : (iy + ""));

			baseurl += f + java.io.File.separator;

			fcnt++;
		}

		String idx = getZYX(z, y, x);
		return baseurl + idx + ".png";
	}
	public static String cachPath(String type, long x, long y, int z,String suffix)
	{
		return type + java.io.File.separator + imgPath(x,y,z) + suffix;
	}
	
	private static String getZYX(long z, long y, long x) {
		long idx = x;
		idx += y << 20;
		idx += z << 40;

		return idx + "";
	}

	private static int hexToDigit(String a) {
		return Integer.parseInt(a, 16);
	}

	public static byte[] readCach(String path) {
		try {
			String p = cachpath + path;
			File f = new File(p);
			FileInputStream fis = new FileInputStream(f);
			byte[] by = new byte[(int) f.length()];
			fis.read(by);
			fis.close();

			return by;
		} catch (Exception e) {
			return null;
		}
	}

	private static char[] intToHex(long i) {
		return Long.toHexString(i).toCharArray();
	}
	public static boolean writeCach(String path, byte[] data) {

		try {
			String p = cachpath + path;
			File f = new File(p);
			f.getParentFile().mkdirs();
			FileOutputStream fis = new FileOutputStream(f);
			fis.write(data);
			fis.close();

			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
}
