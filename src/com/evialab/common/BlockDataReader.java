package com.evialab.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BlockDataReader {

	public static byte[] getImage(int lev, long x, long y, String path) {

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

		String bundlepath = path + "L" + sz + java.io.File.separator + "R" + sR + "C" + sC;

		String bundlefile = bundlepath + ".bundle";
		String bundlxfile = bundlepath + ".bundlx";

		// 计算块内的序�?
		long c = x - C;
		long r = y - R;

		//10.2之前的格�?
		if(new File(bundlxfile).exists()){
			try {
				long idx = getIndex(c, r, bundlxfile);
				// 打开文件读取
				byte[] retimage = getImage(idx, bundlefile);

				return retimage;
			} catch (Exception e) {
				return null;
			}
		}
		//10.3以后�?
		else
		{
			/*try
			{
				long idx = getIndex103(71, 32, bundlefile);
			 
				byte[] retimage = getImage(idx, bundlefile);
				
				FileOutputStream fs = new FileOutputStream(new File("d:\\32_71.png"));
				fs.write(retimage);
				fs.close();
				 
			}catch(Exception e){
				
			}
			
			*/
			
			try {
				long idx = getIndex103(c,r, bundlefile);
				if(idx == 0)
					return null;
				// 打开文件读取
				byte[] retimage = getImage(idx, bundlefile);

				return retimage;
			} catch (Exception e) {
				
				System.out.println("getImage Error:" + e.getMessage());
				return null;
			}
		}
		 
	}

	public static long ToUint4(byte arr[]) {
		return ((long) (arr[3] & 0xFF) << 24) + ((long) (arr[2] & 0xFF) << 16) + ((long) (arr[1] & 0xFF) << 8) + ((long) (arr[0] & 0xFF));
	}

	// 103以后的没有bundlx文件，只有bundle
	synchronized static long   getIndex103(long c, long r, String bfile) throws IOException {
		//这里�?102以前排列不一样，注意
		long spos = (r * 128 + c) * 8;

		// 1�? 打开索引文件，获取该块的偏移长度
		File f = new File(bfile);
		FileInputStream fis = new FileInputStream(f);

		fis.skip(64 + spos);

		byte[] p = new byte[4];
		fis.read(p);
		
		byte[] s = new byte[4];
		fis.read(s);
		
		fis.close();
 
		long size = ToUint4(s);
		if(size == 0)
			return 0;
		
		long idx = ToUint4(p);

		return idx - 4;
	}

	synchronized static long getIndex(long c, long r, String xfile) throws IOException {
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

	synchronized static byte[] getImage(long idx, String bfile) throws IOException {
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
}
