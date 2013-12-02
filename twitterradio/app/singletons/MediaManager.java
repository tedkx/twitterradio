package singletons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.db.MediaResult;

import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import scala.Console;
import utils.U;

public class MediaManager {

	static String TINYSONG_API_KEY = "9351bb67a67e6f57eb2491b1571b9e96";
	
//	public static MediaManager getInstance() {
//		if(instance == null) { instance = new MediaManager(); }
//		return instance;
//	}
	
	public static List<MediaResult> getMedia() {
		return getMedia(AccountManager.getCurrentUser().accountID);
	}
	
	public static List<MediaResult> getMedia(Long userID) {
		List<MediaResult> results = MediaResult.find.where().eq("account_account_id", userID).findList();
		if(results == null) { results = new ArrayList<MediaResult>(); }
		return results;
	}
	
	public static void truncate(Long userID) {
		for(MediaResult result : getMedia(userID)) { result.delete(); }
	}

	public static void removeMedia(Long userID, int id) {
		MediaResult media = MediaResult.find.byId(id);
		Console.println("Media with id " + id + " is null: " + (media == null));
		if(media != null) { media.delete(); }
	}

	public static MediaResult GetSongUrl(String searchString) {
		try {
			String url = "http://tinysong.com/b/";
			String response = "";
			if(searchString.contains("\\") || searchString.contains("/")) {
				response = searchString;
			} else {
				url += java.net.URLEncoder.encode(searchString, "UTF-8");
				url += "?format=json&key=" + TINYSONG_API_KEY;
				response = U.GetJsonResult(url);
			}
			
			Console.println("requesting: " + url);
			Console.println("response: " + response);

			MediaResult result = MediaResult.create(response, response == searchString);
	
			return result;
		} catch (Exception ex) {
			Console.print("GetSongURL error: " + ex.getMessage());
		}
		return null;
	}
	
	public static MediaResult SuggestSong() {
		String suggestedSong = "Pineal gland optics";
		return GetSongUrl(suggestedSong);
	}
}
