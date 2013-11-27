package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;

import play.Play;
import play.libs.Json;
import scala.Console;
import twitter4j.GeoLocation;

public abstract class U {
	
	public static boolean DEBUG = true;
	
	public static GeoLocation getGeoLocation() {
		return new GeoLocation(37.951203,23.670466);
	}
	
	public static String GetJsonResult(String url) {
		return GetJsonResult(url, null);
	}
	
	public static String GetJsonResult(String url, HashMap<String,String> headers) {
		StringBuffer result = new StringBuffer();
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			if(headers != null){
				for(String key : headers.keySet()) {
					request.addHeader(key, headers.get(key));
				}
			}
			
			HttpResponse response = client.execute(request);
	
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) { result.append(line); }
		
			return result.toString();
		} catch (Exception ex) {
			Console.print("GetJsonResult error: " + ex.getMessage());
		}
		return null;
	}

//	public static String getRunningDir(boolean urlSpaces) {
//		String dir = (new U()).getClass().getProtectionDomain().getCodeSource()
//				.getLocation().toString();
//		return (urlSpaces) ? dir : dir.replaceAll("\\%20", " ");
//	}

//	public static String getRunningDir() {
//		return getRunningDir(false);
//	}

//	public static String getAudioDir() {
//		return getRunningDir(false).substring(6) + "audiosources/";
//	}

//	public static URL getRunningDirURL() {
//		return (new U()).getClass().getProtectionDomain().getCodeSource()
//				.getLocation();
//	}

	//public static String getAppUrl(HttpServletRequest request) {
	//	String url = request.getScheme() + "://" + request.getServerName();
	//	if(request.getServerPort() != 80) { url += ":" + request.getServerPort(); }
	//	return url + request.getContextPath() + "/";
	//}
	
//	public static String getAppAbsolutePath(HttpServletRequest request) {
//		return request.getServletContext().getRealPath("/");
//	}

	public static void out(String text) {
		System.out.println(text);
	}
	
	public static void print(Object obj) {
		Console.println(obj);
	}

	public static Properties getCorePropertiesBundle() {
		Properties ps = new Properties();
		try {
			ps.load(U.class.getClassLoader().getResourceAsStream(
					"resources/core.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return ps;
	}

	public static String getProperty(String key) {
		return Play.application().configuration().getString(key, "");
	}

	public static HashMap<String, String> getProperties(String[] keys) {
		HashMap<String, String> props = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			props.put(keys[i], Play.application().configuration().getString(keys[i], ""));
		}
		return props;
	}

	public static String UrlEncode(String text) {
		String newtext = text.replaceAll("\\?", "%3F").replaceAll("\\+", "%2B")
				.replaceAll(" ", "+");
		U.out(newtext);
		return newtext;
	}
}
