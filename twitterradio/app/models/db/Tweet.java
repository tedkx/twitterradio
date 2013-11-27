package models.db;

import java.util.*;
import play.api.templates.Html;
import play.data.format.Formats;
import play.db.ebean.*;
import singletons.SentimentManager;
import singletons.TtsBroker;
import twitter4j.Status;
import javax.persistence.*;

import org.apache.commons.lang3.StringEscapeUtils;

import utils.UI;

@Entity
public class Tweet extends Model {

	@Id
	public long tweetID;
	public long originalID;
	public String text;
    public String audioUrl;
    public String username;
    public String sentimentColor;
    @Formats.DateTime(pattern="dd/MM/yyyy")
	public Date created = new Date();
    @Column(nullable = true)
    @ManyToMany(cascade = CascadeType.ALL)
    public List<models.db.Keyword> keywords;
    
    public static Finder<Long,Tweet> find = new Finder<Long,Tweet>(Long.class, Tweet.class);
    
    protected Tweet(long tweetID, long originalId, String text, String username, Date created) {
    	this.tweetID = tweetID;
    	this.originalID = originalId;
    	this.text = text;
    	this.username = username;
    	this.created = created;
    	this.keywords = new ArrayList<models.db.Keyword>();
    }
    
    public static Tweet Create(Status status, models.db.Keyword keyword) {
    	Tweet tweet = Tweet.find.byId(status.getId());
    	if(tweet == null) {
    		long originalId = status.getId();
    		if(status.isRetweet()) {
    			originalId = status.getRetweetedStatus().getId();
    		}
    		tweet = new Tweet(status.getId(),originalId,status.getText(),status.getUser().getName(),status.getCreatedAt());
    		tweet.keywords.add(keyword);
    		SentimentManager.getInstance().process(tweet);
    		//the TtsBroker will first create audio from the tweet text and then save it to the database
    		TtsBroker.getInstance().speak(tweet);
    	} else {
    		tweet.keywords.add(keyword);
    		tweet.save();
    	}
    	return tweet;
    }

    public String toHtmlString() {
    	String htmlStr = "<div class=\\\"tweet-wrap\\\" id=\\\"tweet-" + this.tweetID + "\\\">" + 
    		"<div class=\\\"tweet\\\">" +
    			"<div class=\\\"tweet-overlay\\\"></div>" +
        		"<div class=\\\"tweet-username\\\">" + StringEscapeUtils.escapeEcmaScript(this.username) + "</div>" +
        		"<div class=\\\"tweet-text\\\">" + StringEscapeUtils.escapeEcmaScript(this.text) + "</div>" +
        	"</div>" +
        "</div>";
    	return htmlStr;
    }
    
    public Html toHtml() { return Html.apply(this.toHtmlString()); }
    
	public String toJson(){
		//String html =  JSONObject.quote(this.toHtmlString());
		return "{" + 
			"\"id\": \"" + this.tweetID + "\", " +
			"\"username\": \"" + this.username + "\", " +
			"\"text\": \"" + this.text + "\", " +
			"\"audiourl\": \"" + this.audioUrl + "\", " +
			"\"color\": \"" + this.sentimentColor + "\", " +
			"\"created\": \"" + this.created.toString() + "\"" +
			"}";
		
	}
	
	 /** Only for debug */
    public static Tweet Create(long id, String status, models.db.Keyword keyword) {
    	Tweet tweet = new Tweet(id, id, "Some test text", "TedKalaitzidis", new Date());
    	tweet.keywords.add(keyword);
    	//SentimentManager.getInstance().process(tweet);
    	TtsBroker.getInstance().speak(tweet);
		return tweet;
    }
    /** Only for debug */
    public Tweet(long tweetID, String text, String username) {
    	this.tweetID = tweetID;
    	this.originalID = tweetID;
    	this.text = text;
    	this.username = username;
    	this.created = new Date();
    	this.keywords = new ArrayList<models.db.Keyword>();
    	//SentimentManager.getInstance().process(this);
    }
}
