package singletons;

import java.util.HashMap;
import org.codehaus.jackson.JsonNode;
import play.libs.Json;
import models.db.Tweet;
import scala.Console;
import utils.U;

public class SentimentManager {

	private static SentimentManager instance;
	private HashMap<Long,Boolean> enabledForUser;
	private boolean enabledByDefault; 
	private static int maxSentiment = 0xFF - 0x99;
	
	private SentimentManager() {
		enabledForUser = new HashMap<Long,Boolean>();
		enabledByDefault = (U.getProperty("EnableSentiment") == "1") ? true : false;
	}
	
	public static SentimentManager getInstance() {
		if(instance == null) { instance = new SentimentManager(); }
		return instance;
	}
	
	public void enable(Long userID) { enabledForUser.put(userID, true); }
	public void enable() { enable(AccountManager.getCurrentUser().accountID); }
	public void disable(Long userID) { enabledForUser.put(userID, false); }
	public void disable() { disable(AccountManager.getCurrentUser().accountID); }
	public boolean isEnabled() { return isEnabled(AccountManager.getCurrentUser().accountID); }
	public boolean isEnabled(Long userID) {
		if(!enabledForUser.containsKey(userID)) {
			enabledForUser.put(userID, enabledByDefault);
		}
		return enabledForUser.get(userID);
	}
	
	public String process(Tweet tweet) {
		try {
			String url = "https://loudelement-free-natural-language-processing-service.p.mashape.com/nlp-text/";//?user=" + tweet.username + " &text= " + tweet.text;
			url += "?text=" + java.net.URLEncoder.encode(tweet.text, "UTF-8");
			U.out("Getting sentiment for tweet " + tweet.tweetID);
			
			HashMap<String,String> headers = new HashMap<String,String>();
			headers.put("X-Mashape-Authorization", "TMGMp0A7XnI54kBbOVwZJoyCG9zMp5zH");
			
			JsonNode node = Json.parse(U.GetJsonResult(url, headers));
			String status = node.findPath("status").asText();
			double probability = (status.toLowerCase().equals("ok")) ? Math.abs(node.findPath("sentiment-score").asDouble()) : 0;
			
			String sentiment = (status.toLowerCase().equals("ok")) ? node.findPath("sentiment-text").asText() : "positive";
			String color = Integer.toHexString(0x99 + (int)(probability * maxSentiment)).toUpperCase();
			String hexColor = (sentiment.equals("negative")) ? "FF" + color + color : color + "FF" + color;

			tweet.sentimentColor = "#" + hexColor;
			return "color: " + hexColor + ", sentiment: " + sentiment;
		} catch (Exception ex) { 
			Console.print("Sentiment process error: " + ex.getMessage());
		}
		return "Error";
	}
}
