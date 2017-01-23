package com.jetmap.route;

import java.util.Timer;
import java.util.TimerTask;

public class RouteCrack extends TimerTask{

	
	Timer timer = new Timer();
	long key = 9063830331000000L;
	public static void main(String[] args) {

		RouteApi.loadLib("");
		RouteCrack c = new RouteCrack();
		c.crack();
		
	}
	public void crack()
	{
		timer.schedule(this, 0, 10 * 1000);
		while(true)
		{
			/*if(RouteApi.CheckKey(key+"")==1)
			{
				System.out.println("crack:" + key);
				timer.cancel();
				break;
			}*/
			key++;
		}
	}
	public void run() {
		  
		 System.out.println(key);
	}

}
