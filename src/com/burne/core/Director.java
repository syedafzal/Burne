package com.burne.core;

import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Director implements Runnable
{

	private Queue<String> queue;
	private ConcurrentHashMap<String,Boolean> visited;
	
	String proxy;
	String port;
	private URL url;
	private final int wait = 4000; 
	//static private int count = 0;
	public Director(String strUrl){
		
		queue = new ConcurrentLinkedQueue<String>();
		//visited = new ConcurrentSkipListSet<String>();
		visited = new ConcurrentHashMap<>();
		visited.put(strUrl, false);
		queue.add(strUrl);
		try{
			url = new URL(strUrl);
		}catch(Exception e){
			System.out.println("Please check the URL");
		}
		
	}
	
	public Director(String strUrl,String proxy, String port){
		
		this(strUrl);
		
		this.proxy = proxy;
		this.port = port;
		
	}
	
	@Override
	public void run()
	{
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		long startTime = System.nanoTime();
		while(!queue.isEmpty() || !executor.isTerminated()){
			
			String url =  queue.poll();

			if(null == url ){
				if(queue.size() == 0){
					try
					{
						Thread.sleep(wait);
						if(queue.size() == 0 ){
							executor.shutdown();
							break;
					}
					}catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}else{
					continue;
				}
			}
			if(!isSameDomain(url)){
				continue;
			}
			
			System.out.println("Link Pulled from Queue "+url);
			//count++;
			Worker c = new Worker(url,queue,visited,proxy,port);
			System.out.println("Starting thread "+url);
			executor.submit(c);

			
		}
		while(!executor.isTerminated());
		
	//	System.out.println("Done Crawling Total thread used"+count);
		
		long endTime   = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println("Done checking. Total time elapsed "+totalTime);
		
	}
	private boolean isSameDomain(String str){
		boolean ret = false;
		
		if(null==str || str.isEmpty())
			return false;
		
		try{
			URL testurl = new URL(str);
			ret = testurl.getHost().equalsIgnoreCase(url.getHost());
		}catch(Exception e){
			System.out.println("Please check the URL");
		}
		return ret;
	}
	public static void main(String[] args)
	{
		Director d = new Director("https://notepad-plus-plus.org/");
		Thread t = new Thread(d);
		t.start();
	}

}