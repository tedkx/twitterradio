package models.db;

import java.util.Date;
import java.util.List;
import play.data.format.Formats;
import play.db.ebean.*;
import scala.Console;
import javax.persistence.*;

@Entity
public class Spoken extends Model {

	@Id
	public int spokenID;
	public long userID;
	public long tweetID;
	public long keywordID;
	@Formats.DateTime(pattern="dd/MM/yyyy")
	public Date created = new Date();
	public static Finder<Integer,Spoken> find = new Finder<Integer,Spoken>(Integer.class, Spoken.class);
    
	public Spoken(long userId, long tweetId, long keywordId) {
		this.userID = userId;
		this.tweetID = tweetId;
		this.keywordID = keywordId;
		this.created = new Date();
	}
	
	public static Spoken Create(long userId, long tweetId, long keywordId) {
		Spoken spoken = Spoken.find.query()
			.where("userID = " + userId + " AND keywordID = " + keywordId + " and tweetID = " + tweetId)
			.findUnique();
		if(spoken == null) {
			spoken = new Spoken(userId, tweetId, keywordId);
			spoken.save();
		}
		return spoken;
	}
	
	public static Long getLastTweetID(Long userId, Long keywordId) {
		List<Spoken> spokenList = Spoken.find.query()
				.where("userID = " + userId + " AND keywordID = " + keywordId)
				.orderBy("created DESC")
				.findList();
		if(spokenList == null || spokenList.size() == 0) { return 0L; }
		return spokenList.get(0).tweetID;
	}
	
	public static boolean isSpoken(Long tweetId, Long userId, Long keywordId) {
		return (Spoken.find.query()
				.where("userID = " + userId + " AND keywordID = " + keywordId + " and tweetID = " + tweetId)
				.findUnique() == null);
	}
}
