package singletons;

import scala.Console;
import utils.U;

public class MediaManager {

	static String TINYSONG_API_KEY = "9351bb67a67e6f57eb2491b1571b9e96";
	
	public static String GetSongUrl(String searchString) {
		try {
			String url = "http://tinysong.com/a/";
	
			url += java.net.URLEncoder.encode(searchString, "UTF-8");
			url += "?format=json&key=" + TINYSONG_API_KEY;
			U.out("Getting sentiment for text " + searchString);
	
			return U.GetJsonResult(url);
		} catch (Exception ex) {
			Console.print("GetSongURL error: " + ex.getMessage());
		}
		return null;
	}
}
