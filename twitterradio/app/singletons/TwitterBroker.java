package singletons;

import java.util.HashMap;
import scala.Console;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import utils.*;
import models.*;
import models.db.Account;
import models.db.Band;
import models.db.Keyword;
import models.db.Spoken;
import models.db.Tweet;
import java.util.*;

public class TwitterBroker {

	private static final int TRENDS_EXPIRATION = 60; // minutes
	
	private static TwitterBroker instance;
	protected HashMap<Long,TwitterStreamData> streamsData;
	private Band trendsBand = null;
	private Date trendsExpirationDate;
	private boolean isGettingTrends = false;
	
	protected TwitterBroker() {
		streamsData = new HashMap<Long,TwitterStreamData>();
	}
	
	public static TwitterBroker getInstance() { 
		if(instance == null) { instance = new TwitterBroker(); }
		return instance;
	}
	
	/**
	 * Gets the twitter data allocated for the current user
	 * @return
	 */
	public TwitterStreamData getTwitterData() {
		return this.getTwitterData(SessionBroker.getCurrentUserID());
	}
	
	/**
	 * Gets the twitter data allocated for the current user
	 * @return
	 */
	public TwitterStreamData getTwitterData(String userId){
		return getTwitterData(Long.parseLong(userId));
	}
	/**
	 * Gets the twitter data allocated for the current user
	 * @return
	 */
	public TwitterStreamData getTwitterData(Long userId){
		if(this.streamsData.containsKey(userId)) { return this.streamsData.get(userId); }
		return null;
	}
	
	/** 
	 * Creates a twitter stream for the logged in user and
	 * queries for a number of initial tweets
	 * @param userId
	 * @return
	 */
	public List<Status> initKeywordSearch(String keyword, Long userId) {
		List<Status> initialTweets = null;
		TwitterStreamData data = this.getTwitterData(userId);
		
		//first of all, if a stream is already open it must shutdown
		if(data != null) {
			if(data.listenerThread != null) {
				Console.println("Closing listener thread");
				data.listenerThread.die();
				data.listenerThread = null;
				//this closes both the listener and the twitterstream
			}
		} else {
			U.out("Creating new stream data for user " + userId);
			data = new TwitterStreamData(userId);
		}
		
		//(Re)Initialize the twitter stream
		data.stream = initializeTwitterStream(userId);
		
		//Switch the keyword to search
		data.keyword = Keyword.Create(keyword, userId);

		//First get some initial tweets (if config says initialTweetsNumber is > 0)
		int initialTweetsNumber = Integer.parseInt(U.getProperty("twadio.InitialTweetsNumber"));
		if(initialTweetsNumber > 0) {
			Twitter twitter = initializeTwitter(userId);
			Query query = new Query(data.keyword.text);
			query.setCount(initialTweetsNumber);
			query.setResultType("recent");
			try {
				initialTweets = twitter.search(query).getTweets();
				//Collections.reverse(initialTweets);
			} catch(TwitterException e) {
				Console.println(e.getMessage());
			}
		}
		
		//Then set the stream to return every new status
		data.initListener();
		
		//Finally store the instance to be accessed later
		this.streamsData.put(userId, data);
		
		return initialTweets;
	}
	
	/**
	 * Saves a tweet and forwards it to the websocket after
	 * it is received by the stream listener
	 * @param status
	 * @param userID
	 */
	public void processStatus(Status status,long userID) {
		try {
			models.db.Keyword keyword = this.streamsData.get(userID).keyword;
			Tweet tweet = Tweet.Create(status, keyword);
			//Spoken.Create(userID, tweet.tweetID, keyword.keywordID);
			WebSocketManager.getInstance().sendTweet(tweet, userID);
		} catch (Exception ex) { 
			U.out("Error while processing status " + status.getId());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Cleans up the twitterStream instance for the current user
	 * @param userId
	 */
	public void closeUserStream(Long userId) {
		TwitterStreamData data = this.getTwitterData(userId);
		if(data != null) {
			if(data.listenerThread != null) {
				data.listenerThread.die();
				data.listenerThread = null;
			}
			if(data.stream != null) {
				data.stream.cleanUp();
				data.stream = null;
			}
			data.keyword = null;
		}
	}
	
	/** 
	 * Returns a band with current twitter trends. this
	 * Band is not saved in the database nor is dependent
	 * on a specific user
	 */
	public Band getTrends() {
		Console.println("Refreshing trends");
		trendsBand = BandManager.CreateTrendsBand();
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(U.getProperty("securesocial.twitter.consumerKey"), U.getProperty("securesocial.twitter.consumerSecret"));
        AccessToken accessToken = new AccessToken(U.getProperty("twadio.MyToken"), U.getProperty("twadio.MySecret"));
        twitter.setOAuthAccessToken(accessToken);
		try {
			U.print("Getting trends from twitter");
			//ResponseList<Location> locs = twitter.getClosestTrends(U.getGeoLocation());
			//ResponseList<Location> locs = twitter.getAvailableTrends();
			//U.print("Got locations");
			//Trends trends = twitter.getPlaceTrends(locs.get(0).getWoeid());
			Trends trends = twitter.getPlaceTrends(1);
			
			for(Trend trend : trends.getTrends()) {
				trendsBand.keywords.add(Keyword.CreateTrendKeyword(trend.getName(), trendsBand));
			}
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, TRENDS_EXPIRATION);
			trendsExpirationDate = cal.getTime();
		} catch (Exception e) { 
			System.out.println("error while getting trends, " + e.getMessage());
			//e.printStackTrace();
		}
		return trendsBand;
	}
	
	/**
	 * Checks if trends have expired, based on the expiration
	 * time set above
	 */
	public static boolean TrendsAreStale()
	{
		if(instance.trendsExpirationDate == null) { return true; }
		return instance.trendsExpirationDate.before(new Date());
	}
	
	/** 
	 * Initialized a twitterstream instance
	 * @param userId
	 */
	public static TwitterStream initializeTwitterStream(Long userId) {
		TwitterStream stream = new TwitterStreamFactory().getInstance();
		Account user = AccountManager.get(userId);
		try {
			AccessToken accessToken = new AccessToken(user.token, user.secret);
			stream.setOAuthConsumer(U.getProperty("securesocial.twitter.consumerKey"), U.getProperty("securesocial.twitter.consumerSecret"));
			stream.setOAuthAccessToken(accessToken);
		} catch (Exception e) { 
			U.out("error while creating stream, " + e.getMessage());
		}
		return stream;
	}
	
	public void closeStreams() {
		for(TwitterStreamData data : streamsData.values()) {
			if(data.listenerThread != null) {
				data.listenerThread.die();
				data.listenerThread = null;
			}
			if(data.stream != null) {
				data.stream.cleanUp();
				data.stream = null;
			}
		}
	}
	
	private static Twitter initializeTwitter(Long userId) {
		Account user = AccountManager.get(userId);
		Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(U.getProperty("securesocial.twitter.consumerKey"), U.getProperty("securesocial.twitter.consumerSecret"));
        AccessToken accessToken = new AccessToken(user.token, user.secret);
        twitter.setOAuthAccessToken(accessToken);
        return twitter;
	}

}
