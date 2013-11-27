package models.twitter;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;

public class TwitterStreamListenerThread implements Runnable {
    
	private TwitterStream twitterStream;
    private models.db.Keyword keyword;
    private Long userId;
    private TWStatusListener listener;
    private boolean dead = false;
    
    public TwitterStreamListenerThread(TwitterStream stream, Long userId, models.db.Keyword keyword) {
    	this.twitterStream = stream;
    	this.userId = userId;
        this.keyword = keyword;
        new Thread(this).start();
    }
    
    private void connect() {
    	listener = new TWStatusListener(userId, this);
        twitterStream.addListener(listener);
        FilterQuery filter = new FilterQuery();
        filter.track(new String[] { keyword.text });
        twitterStream.filter(filter);
    }
    
    public void run() {
    	connect();
        while(true) {
//        	if(this.reconnecting) {
//        		twitterStream.shutdown();
//            	twitterStream.cleanUp();
//            	twitterStream = TwitterBroker.initializeTwitterStream(this.userId);
//            	connect();
//            	this.reconnecting = false;
//                System.out.println("--- STREAMTHREAD RECONNECTED ---");
//        	} 
        	if(this.dead) {
            	twitterStream.shutdown();
            	twitterStream.cleanUp();
            	listener = null;
                System.out.println("--- STREAMTHREAD CLOSING ---");
                return;
            }
            try { Thread.sleep(200); } catch(Exception e) { e.printStackTrace();return; }
        }
    }

    public boolean isDead() { return this.dead; }
    public synchronized void die() { this.dead = true; }
}
