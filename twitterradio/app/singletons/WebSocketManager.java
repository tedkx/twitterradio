package singletons;

import java.util.List;
import java.util.HashMap;
import play.mvc.WebSocket;
import models.TwWebSocket;
import models.db.Tweet;
import scala.Console;
import twitter4j.Status;

public class WebSocketManager {

	private static WebSocketManager instance;
	private HashMap<Long,TwWebSocket> sockets;

	protected WebSocketManager() {
		sockets = new HashMap<Long,TwWebSocket>();
	}
	
	public static WebSocketManager getInstance() {
		if(instance == null) { instance = new WebSocketManager(); }
		return instance;
	}
	
	/**
	 * Initializes a new socket connection with a user
	 * @param userID
	 * @return
	 */
	public WebSocket<String> initializeSocket() {
		Long userID = SessionBroker.getCurrentUserID();
		TwWebSocket socket = new TwWebSocket(userID);
		this.sockets.put(userID, socket);
		return socket;
	}
	
	/**
	 * When a message is sent by the client, a keyword searh starts
	 * @param socket
	 * @param event
	 */
	public void processWebSocketMessage(models.TwWebSocket socket, String event) {
		Console.println("Switching keyword for user " + socket.getUserID() + " to " + event);
		List<Status> initialTweets = TwitterBroker.getInstance().initKeywordSearch(event, socket.getUserID());
		for(Status status : initialTweets) { TwitterBroker.getInstance().processStatus(status, socket.getUserID()); }
	}
	
	/**
	 * Sends a tweet to the client through the websocket
	 * @param tweet
	 * @param userID
	 */
	public void sendTweet(Tweet tweet, long userID) {
		if(sockets.containsKey(userID) && sockets.get(userID) != null) {
			sockets.get(userID).send(tweet.toJson());
		}
		Console.println("Sending a tweet to user " + userID);
	}
}
