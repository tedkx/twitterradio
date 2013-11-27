package models;

import models.twitter.TwitterStreamListenerThread;
import twitter4j.TwitterStream;

public class TwitterStreamData {

	//public Identity user;
	public Long userId;
	public TwitterStream stream;
	public TwitterStreamListenerThread listenerThread;
	public models.db.Keyword keyword; 
	
	public TwitterStreamData(Long userId)	{
		this.userId = userId;
	}
	
	public void initListener() {
		this.listenerThread = new TwitterStreamListenerThread(stream, userId, keyword);
	}
}