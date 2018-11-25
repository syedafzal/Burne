package com.burne.core;

import java.io.IOException;
import java.net.URL;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Worker implements Runnable {

	private String url;
	private Queue<String> queue;
	private ConcurrentHashMap<String, Boolean> visited;
	private String proxy;
	private int port;
	private boolean isProxySet;
	private final int TIMEOUT = 5000;
	//static Integer counter = 0;

	public Worker(String url, Queue<String> queue, ConcurrentHashMap<String, Boolean> visited) {
		this.queue = queue;
		this.url = url;
		this.visited = visited;
		Thread.currentThread().setName("url");
	}

	public Worker(String url, Queue<String> queue, ConcurrentHashMap<String, Boolean> visited, String proxy,
			String port) {
		this(url, queue, visited);
		this.proxy = proxy;
		this.port = null != port ? Integer.parseInt(port) : 80;
		isProxySet = null != this.proxy;
	}

	@Override
	public void run() {
		Response response = null;
		Response testResponse = null;
		Document doc = null;

		try {
			if (visited.containsKey(url) && visited.get(url)) {

			//	System.out.println("Already Processed " + url);
				return;
			}
			System.out.println("Started Processing " + url);
			visited.put(url, true);
			response = getResponse(url);
			doc = response.parse();

			Elements links = doc.select("a[href]");
			String link = null;
			String hostName = response.url().getHost();

			Elements media = doc.select("[src]");
			Pattern p = Pattern.compile("[\"'](.*?)[\"']");// ("/[\"'](.*?)[\"']/g");("([\"'])(?:(?=(\\?))\2.)*?\1"
			for (Element src : media) {
				if (src.tagName().equals("script")) {
					System.out.printf("Reading  %s: <%s>", src.tagName(), src.attr("abs:src"));
					String code = getResponse(src.attr("abs:src")).body();
					code = code.replaceAll("\n", "");

					try {
						Matcher m = p.matcher(code);

						while (m.find()) {
							String candidate = m.group(1);
							if (!candidate.isEmpty()) {
								if (candidate.startsWith("http")) {
									if (isSameDomain(candidate)&&!visited.containsKey(candidate)) {
										System.out.println("\nTesting Link " + m.group(1));
										visited.put(candidate, true);
									//	linked();
									//	System.out.println("Tested "+counter);
										boolean isVulnerable = checkForRFD(getResponse(candidate));
										if (isVulnerable) {
											System.out.println("\t---------------");
											System.out.println("Possible Vulnerable Link: " + candidate);
											System.out.println("\t---------------");
										}

									} /*Handling Relative Path*/
								} else if (candidate.startsWith("/")) {
									int i = url.lastIndexOf("/");
									candidate = url.substring(0, i) + candidate;
									if (isSameDomain(candidate)&&!visited.containsKey(candidate)) {
										System.out.println("\nTesting Link " + m.group(1) + " --> " + candidate );
										visited.put(candidate, true);
									//	linked();
									//	System.out.println("Tested "+counter);
										boolean isVulnerable = checkForRFD(getResponse(candidate));
										if (isVulnerable) {
											System.out.println("\t---------------");
											System.out.println("Possible Vulnerable Link: " + candidate);
											System.out.println("\t---------------");
										}

									}
								}
							}
						}
					} catch (Exception e) {

						System.out.println(e.getMessage());
						// e.printStackTrace();
					}

				}
			}

			for (Element element : links) {
				// System.out.println("in for "+url);

				link = element.attr("abs:href");
				if (null != link && !link.isEmpty() && !visited.containsKey(link)) {
					URL subURL = null;
					try {
						System.out.println("sub " + link);
						subURL = new URL(link);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (hostName.equalsIgnoreCase(subURL.getHost())) {

						visited.put(link, false);
						queue.add(link);

						System.out.println("Added to Queue " + link + " " + queue.size());

						testResponse = getResponse(link);
				//		linked();
					//	System.out.println("Tested "+counter);
						boolean isVulnerable = checkForRFD(testResponse);
						if (isVulnerable) {
							System.out.println("\t---------------");
							System.out.println("Possible Vulnerable Link: " + link);
							System.out.println("\t---------------");
						}

					} else {
						// System.out.println("External Link");
						visited.put(link, false);
					}
				}

			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Check for Reflected File Download Vulnerability
	 * 
	 */
	private boolean checkForRFD(Response res) {

		boolean check1 = false;
		boolean check2 = false;
		boolean check3 = false;

		// For RFD vulnerability to be true 3 conditions must be satisfied:

		// 1. JSON request should have context type set
		String contentType=null;
		try {
			contentType = res.contentType();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println("Could not read content-type header");
		}
		if (null != contentType && contentType.startsWith("application")) {
			check1 = true;
		} else {
			return false;
		}
		// 2. filename param should be missing in content-disposition header
		if (res.hasHeader("Content-Disposition")) {
			String disposition = res.header("Content-Disposition");
			if (null != disposition && !disposition.contains("filename")) {
				check2 = true;
			} else {
				return false;
			}

		} else {
			check2 = true;
		}

		// 3. Input should be reflected in the response body
		String url = res.url().toString();
		final String TEXT = "111999";
		// Add random value to detect if its reflected.
		url += TEXT;

		Response test = null;
		// Document testDoc = null;
		try {
			test = getResponse(url);
			if (test.body().contains(TEXT)) {
				check3 = true;
			} else {
				return false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return (check1 && check2 && check3);
	}

	/*
	 * Method takes a String URL and return HTTP Response Object
	 *
	 */
	private Response getResponse(String url) {

		Response res = null;
		try {
			Connection con = Jsoup.connect(url).method(Connection.Method.GET).followRedirects(true).timeout(TIMEOUT)
					.ignoreContentType(true);
			if (isProxySet) {

				res = con.proxy(proxy, port).execute();
			} else {
				res = con.execute();
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return res;

	}

	private boolean isSameDomain(String str) {
		boolean ret = false;

		if (null == str || str.isEmpty())
			return false;

		try {
			URL testurl = new URL(str);
			URL targeturl = new URL(this.url);
			ret = testurl.getHost().equalsIgnoreCase(targeturl.getHost());
		} catch (Exception e) {
			// System.out.println("Please check the URL"+str);
		}
		return ret;
	}
}

