package singletons;

import java.util.HashMap;
import java.util.LinkedList;
import models.MediaResult;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import scala.Console;
import utils.U;

public class MediaManager {

	static String TINYSONG_API_KEY = "9351bb67a67e6f57eb2491b1571b9e96";
	static MediaManager instance;
	private HashMap<Long,LinkedList<MediaResult>> userResults;
	
	public MediaManager() {
		userResults = new HashMap<Long,LinkedList<MediaResult>>();
	}
	
	public static MediaManager getInstance() {
		if(instance == null) { instance = new MediaManager(); }
		return instance;
	}
	
	public MediaResult[] getMedia() {
		return this.getMedia(AccountManager.getCurrentUser().accountID);
	}
	
	public MediaResult[] getMedia(Long userID) {
		ensureExists(userID);
		truncate(userID);
		MediaResult[] media = new MediaResult[] {  };
		userResults.get(userID).toArray(media);
		return media;
	}
	
	public void truncate(Long userID) {
		
	}
	
	public void addMedia(MediaResult result) {
		addMedia(result, AccountManager.getCurrentUser().accountID);
	}
	
	public void addMedia(MediaResult result, Long userID){
		ensureExists(userID);
		userResults.get(userID).add(result);
	}

	private void ensureExists(Long userID) {
		if(!userResults.containsKey(userID) || userResults.get(userID) == null) {
			userResults.put(userID, new LinkedList<MediaResult>());
		}
	}
	
	public MediaResult GetSongUrl(String searchString) {
		try {
			String url = "http://tinysong.com/b/";
	
			url += java.net.URLEncoder.encode(searchString, "UTF-8");
			url += "?format=json&key=" + TINYSONG_API_KEY;
			U.out("Getting sentiment for text " + searchString);
			
			String response = U.GetJsonResult(url);
			
			MediaResult result = new MediaResult();
			Console.print(response);
			if(!response.startsWith("[]")) {
				JsonNode node = Json.parse(response);
				result.title = node.findPath("ArtistName").asText() + " - " + node.findPath("SongName").asText();
				result.url = node.findPath("Url").asText();
				result.songID = node.findPath("SongID").asText();
				addMedia(result);
			}
	
			return result;
		} catch (Exception ex) {
			Console.print("GetSongURL error: " + ex.getMessage());
		}
		return null;
	}
	
	public MediaResult SuggestSong() {
		String suggestedSong = "Pineal gland optics";
		addMedia(result);
		return GetSongUrl(suggestedSong);
	}
}
