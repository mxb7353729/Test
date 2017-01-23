package com.evialab.unittest;

public class RandomLngLat {
	static double randRange(double min, double max) {
		return min + Math.random() * (max - min);
	}

	public RandomLngLat() {
		//lon = randRange(100, 120);
		//lat = randRange(20, 40);
		lon = randRange(115.38403, 117.27368);
		lat = randRange(39.49703, 40.64041);
	}

	public double lon = 0;
	public double lat = 0;
	
	// 生成随机点位
	public	static RandomLngLat[] CreateRandomPoints(int cnt) {

			RandomLngLat[] arr = new RandomLngLat[cnt];
			for (int i = 0; i < cnt; i++)
				arr[i] = new RandomLngLat();

			return arr;
		}
}
