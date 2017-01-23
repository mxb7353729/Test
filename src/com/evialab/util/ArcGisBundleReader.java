package com.evialab.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.evialab.gisserver.CachUtil;

public class ArcGisBundleReader {

	public static void main(String[] args) {

	}

	String arcpath = "";
	String cachprefix = "arcgis";

	public void SetTilePath(String path) {
		arcpath = path;
		if (!arcpath.endsWith(java.io.File.separator))
			arcpath += java.io.File.separator;
	}

	static long ToUint4(byte arr[]) {
		return ((long) (arr[3] & 0xFF) << 24) + ((long) (arr[2] & 0xFF) << 16)
				+ ((long) (arr[1] & 0xFF) << 8) + ((long) (arr[0] & 0xFF));
	}

	long getIndex(long c, long r, String xfile) throws IOException {
		long spos = (c * 128 + r) * 5;

		// 1�? 打开索引文件，获取该块的偏移长度
		File f = new File(xfile);
		FileInputStream fis = new FileInputStream(f);

		fis.skip(16 + spos);

		byte[] a = new byte[5];
		fis.read(a);
		fis.close();

		// long idx = (a[4] << 32) + (a[3] << 24) + (a[2] << 16) + (a[1] << 8) +
		// a[0];
		long idx = ToUint4(a);

		return idx;
	}

	byte[] getImage(long idx, String bfile) throws IOException {
		File f = new File(bfile);
		FileInputStream fis = new FileInputStream(f);
		fis.skip(idx);

		byte[] lenb = new byte[4];
		fis.read(lenb);

		int len = (int) ToUint4(lenb);

		if (len <= 0) {
			fis.close();
			return null;
		}

		// System.out.println("size:" + len);

		byte[] imgdata = new byte[len];

		fis.read(imgdata);

		fis.close();

		return imgdata;
	}

	public byte[] getImage(long x, long y, int lev) {
		String cathpath = cachprefix + java.io.File.separator + CachUtil.imgPath(x, y, lev);
		byte[] data = CachUtil.readCach(cathpath);
		if (data != null)
			return data;

		// 计算bundle的文件路�?
		long C = x & 0xffffff80;
		long R = y & 0xffffff80;

		String sz = Integer.toString(lev);
		if (sz.length() < 2)
			sz = "0" + sz;

		String sC = Long.toString(C, 16);
		while (sC.length() < 4)
			sC = "0" + sC;

		String sR = Long.toString(R, 16);
		while (sR.length() < 4)
			sR = "0" + sR;

		String bundlepath = arcpath + "L" + sz + java.io.File.separator+"R" + sR + "C" + sC;

		String bundlefile = bundlepath + ".bundle";
		String bundlxfile = bundlepath + ".bundlx";

		// 计算块内的序�?
		long c = x - C;
		long r = y - R;

		try {
			// 获得图片位置
			long idx = getIndex(c, r, bundlxfile);

			byte[] buffer = getImage(idx, bundlefile);
			if (buffer != null) {
				CachUtil.writeCach(cathpath, buffer);
			}
			return buffer;
		} catch (Exception e) {
			return null;
		}
	}
}
