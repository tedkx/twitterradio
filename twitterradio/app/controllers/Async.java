package controllers;

import models.db.Tweet;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import scala.Console;
import singletons.AccountManager;
import singletons.BandManager;
import singletons.MediaManager;
import singletons.SentimentManager;
import singletons.WebSocketManager;

public class Async extends Controller {
	
	/**
	 * Gets the current trends wrapped in a Band object
	 * @return Trends Band
	 */
	@With(BaseAction.class)
	public static Result trends() {
		ctx().response().setHeader("Content-Type", "application/json; charset=UTF-8");
		return status(OK,BandManager.getInstance().getTrendsBand().toJson());
	}
	
	/**
	 * Initializes a websocket to transfer tweets to the client
	 * @return
	 */
	@With(BaseAction.class)
	public static WebSocket<String> stream() {
		return WebSocketManager.getInstance().initializeSocket();
	}
	
	/**
	 * Sentiment test action. 
	 * @deprecated Not used. Just for testing
	 * @return
	 */
	@With(BaseAction.class)
	public static Result sentiment() {
		Tweet tweet = new Tweet(12345678L, "some status", "username");
		return status(OK,SentimentManager.getInstance().process(tweet));
	}
	
	/**
	 * Searches for a song with the selected text. 
	 * @param text The text to search songs for
	 * @return A string with the song URL, null if nothing found
	 */
	@With(BaseAction.class)
	public static Result getMedia(String text) {
		return status(OK,MediaManager.GetSongUrl(text).toJSON());
	}
	
	/**
	 * Searches for a song based on user's last 10 listened tweets
	 * @param text The text to search songs for
	 * @return A string with the song URL, null if nothing found
	 */
	@With(BaseAction.class)
	public static Result suggestMedia() {
		return status(OK,MediaManager.SuggestSong().toJSON());
	}
	
	/**
	 * Deletes a user's saved media entry
	 */
	@With(BaseAction.class)
	public static Result removeMedia(String id) {
		MediaManager.removeMedia(AccountManager.getCurrentUser().accountID, Integer.valueOf(id));
		return status(OK);
	}
	
	/**
	 * Deletes user's saved media list
	 */
	@With(BaseAction.class)
	public static Result clearMedia() {
		MediaManager.truncate(AccountManager.getCurrentUser().accountID);
		return status(OK);
	}
}
