package com.burne.core;

/**
 * @author Syedafzal_Ahamed
 *
 */
public class Burne {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		printBanner();
		
		if(args.length == 0) return;
		
		String target = args[0];
		String proxy = null;
		String port = null;
		
		boolean isProxySet = args.length == 2 ;
		if(isProxySet){
			String proxyport = args[1];
			proxy = proxyport.split(":")[0];
			if(proxyport.contains(":")){
				port = proxyport.split(":")[1];
			}
		}
		Director d = new Director(target,proxy,port);
		Thread t = new Thread(d);
		t.start();
			
		
		
		
		
	}
	
	static void printBanner(){
		
		String art[] = {
		                "______                       ",
		                "| ___ \\                      ",
		                "| |_/ /_   _ _ __ _ __   ___ ",
		                "| ___ \\ | | | '__| '_ \\ / _ \\",
		                "| |_/ / |_| | |  | | | |  __/",
		                "\\____/ \\__,_|_|  |_| |_|\\___|"
		                
		};
		for(String s: art)
			System.out.println(s);
		
		String msg[] = {
				"Reflective File Download Detection Tool, v0.1",
				"Author : Syed <syed@syedafzal.in>",
				"Usage  : java -jar Burne.jar <testsite>(required) <proxy>(optional)",
				"Example: java -jar Burne.jar \"http://mysite.com/\" \"127.0.0.1:80\"\n"
		};
		for(String s: msg)
			System.out.println(s);
		
	}

}