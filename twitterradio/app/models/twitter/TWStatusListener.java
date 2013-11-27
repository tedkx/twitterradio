package models.twitter;

import scala.Console;
import singletons.TwitterBroker;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;

public class TWStatusListener implements StatusListener {

	private Long userId;
	private TwitterStreamListenerThread parent;
	public TWStatusListener(Long userId, TwitterStreamListenerThread parent) {
		this.userId = userId;
		this.parent = parent;
	}
	public void onStatus(Status status) {
		if(parent.isDead()) { return; }
        Console.println("Received status with ID: " + status.getId());
        TwitterBroker.getInstance().processStatus(status, userId);
    }
    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }
    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
    }
    @Override
    public void onException(Exception ex) {
    	System.out.println("StatusListener exception: " + ex.getMessage());
        ex.printStackTrace();
    }
    @Override
    public void onScrubGeo(long arg0, long arg1) {
        System.out.println("onScrubGeo not supported yet.");
    }
	@Override
	public void onStallWarning(StallWarning arg0) { }
}
