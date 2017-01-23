package com.evialab.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.dom4j.Node;
 

public class FullDataReader {

	

	public FullDataReader() {

	}
 
	static FullDataReader reader = null;

	public static FullDataReader getDefaultInstance() {
		if (reader == null)
			reader = new FullDataReader();
		return reader;
	}

	public byte[] GetImage(long x, long y, int level) {

		if(levels == null)
			return null;
		for(int i= 0; i< levels.size(); i++){
			byte[] img = levels.get(i).GetImage(x, y, level);
			if(img != null)
				return img;
		}
		return null;
	}

	List<LevelRange> levels = null;
	
	public boolean LoadConfig(Node fulldata){
		
		if(fulldata == null)
			return false;
		
		List<Node> ranges = fulldata.selectNodes("levelrange");
		if(ranges == null || ranges.size() == 0)
			return false;
		for(int i = 0; i < ranges.size(); i++){
			Node n = ranges.get(i);
			
			LevelRange lr = null;
			try{
				String fullpath = n.selectSingleNode("path").getText();
				String minlevel = n.selectSingleNode("minlevel").getText();
				String maxlevel = n.selectSingleNode("maxlevel").getText();
				String ids = n.selectSingleNode("ids").getText();

				lr = new LevelRange();
				lr.setBlockpath(fullpath);
				lr.setMinLevel(Integer.parseInt(minlevel));
				lr.setMaxLevel(Integer.parseInt(maxlevel));
				lr.parseIDS(ids);
				
				System.out.println("分块成果目录:"+ fullpath + " 级别:" + minlevel + "-" + maxlevel);
			}
			catch(Exception e){
				System.out.println("设置分块成果目录失败: " + e.getLocalizedMessage());
			}
			if(lr == null)
				continue;
			
			if(levels == null)
				levels = new ArrayList<LevelRange>();
			
			levels.add(lr);

			
		}
		try {
			
		} catch (Exception e) {
			System.out.println("没有设置成果数据失败�?" + e.getLocalizedMessage());
		}
		return levels.size() > 0;
	}

	class Block {
		int lev;
		int minx;
		int maxx;
		int miny;
		int maxy;

		public Block() {
			lev = 0;
			minx = maxx = miny = maxy = 0;
		}
	}
	
	class LevelRange{
		
		public String blockpath = "";
		public int minLevel = 1;
		public int maxLevel = 20;
		public HashMap<Integer, List<Block>> blocks = null;
		public String getBlockpath() {
			return blockpath;
		}
		public void setBlockpath(String blockpath) {
			this.blockpath = blockpath;
			if (!blockpath.endsWith(java.io.File.separator))
				blockpath += java.io.File.separator;
		}
		public int getMinLevel() {
			return minLevel;
		}
		public void setMinLevel(int minLevel) {
			this.minLevel = minLevel;
		}
		public int getMaxLevel() {
			return maxLevel;
		}
		public void setMaxLevel(int maxLevel) {
			this.maxLevel = maxLevel;
		}
		public byte[] GetImage(long x, long y, int level) {

			if(level < minLevel || level > maxLevel)
				return null;
			
			String folder = getFolder(x,y,level);
			if(folder == null)
				return null;
			
			String path = blockpath +  java.io.File.separator + "b_" + folder+ java.io.File.separator + "Layers" + java.io.File.separator + "_alllayers" + java.io.File.separator;
		 
			return BlockDataReader.getImage(level,x, y, path);
		}
		public void parseIDS(String ids) {
			blocks = new HashMap<Integer, List<Block>>();
			ids = ids.trim();
			String[] lines = ids.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String t = lines[i].trim();
				if(t.indexOf("add") < 0)
					continue;
				t = t.replace("add(", "");
				t = t.replace(")", "");
				String[] vs = t.split(",");

				Block b = new Block();
				b.lev = Integer.parseInt(vs[0]);
				b.minx = Integer.parseInt(vs[1]);
				b.maxx = Integer.parseInt(vs[2]);
				b.miny = Integer.parseInt(vs[3]);
				b.maxy = Integer.parseInt(vs[4]);

				List<Block> bs = blocks.get(b.lev);
				if (bs == null) {
					bs = new ArrayList<Block>();
					blocks.put(b.lev, bs);
				}
				bs.add(b);

			}
		}
		private String getFolder(long x, long y, int lev) {
			 

			Iterator<Entry<Integer, List<Block>>> iterator = blocks.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, List<Block>> entry = iterator.next();

				int level = entry.getKey();

				// 计算 这个块在 level 上的序号
				long lx = x >> (lev - level);
				long ly = y >> (lev - level);

				Iterator<Block> it = entry.getValue().iterator();
				while (it.hasNext()) {

					Block b = it.next();
					// 计算是否相交
					if (lx >= b.minx && lx <= b.maxx && ly >= b.miny && ly <= b.maxy) {

						return level + "_" + b.minx + "_" + b.maxx + "_" + b.miny + "_" + b.maxy;
					}
				}
			}

			System.out.println("找不到块:x:"+x + "\ty:"+y+"\tz:"+lev);
			return null;
		}
	}

	
}
