package models.db;

import java.util.Date;
import java.util.Random;

import javax.persistence.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

import com.avaje.ebean.Expr;

import play.data.format.Formats;
import play.db.ebean.*;
import play.db.ebean.Model.Finder;
import play.libs.Json;
import scala.Console;
import singletons.AccountManager;

@Entity
public class MediaResult extends Model {

	@Id
	public int mediaResultID;
	public String title = "";
	public String url = "";
	public String songID = "";
	@ManyToOne
	public Account account;
	@Formats.DateTime(pattern="dd/MM/yyyy")
	public Date created;
	public boolean isLocal;
	
	public static Finder<Integer,MediaResult> find = new Finder<Integer,MediaResult>(Integer.class, MediaResult.class);

	public static MediaResult create(String jsonResponse, boolean isLocal) {
		MediaResult result = new MediaResult();
		result.account = AccountManager.getCurrentUser();
		result.isLocal = isLocal;
		if(isLocal) {
			MediaResult existing = find.where().and(
					Expr.eq("account_account_id", result.account.accountID),
					Expr.eq("url", jsonResponse)).findUnique();
			if(existing != null) { 
				Console.println("existing");
				result = existing;
			} else {
				Console.println("not existing");
				result.title = jsonResponse.substring(jsonResponse.lastIndexOf("\\") + 1);
				result.url = jsonResponse;
				result.songID = (new Random()).nextInt(1000) + "";
			}
			result.created = new Date();
			result.save();
		}
		else if(!jsonResponse.startsWith("[]")) {
			JsonNode node = Json.parse(jsonResponse);
			if(node.findPath("error").asText().length() == 0) { 
				String songID = node.findPath("SongID").asText();
				MediaResult existing = find.where().and(
						Expr.eq("account_account_id", result.account.accountID),
						Expr.eq("song_id", songID)).findUnique();
				if(existing != null) { 
					result = existing;
				} else {
					result.title = node.findPath("ArtistName").asText() + " - " + node.findPath("SongName").asText();
					result.url = node.findPath("Url").asText();
					result.songID = songID;
				}
				result.created = new Date();
				result.save();
			}
		}
		return result;
	}
	
	public boolean isDummy() {
		return (title == "");
	}

	public ObjectNode toJSON() {
		ObjectNode jsonaki = Json.newObject();
		if(title != "") {
			jsonaki.put("title", title); 
			jsonaki.put("url", url);
			jsonaki.put("songid", songID);
			jsonaki.put("mediaid", mediaResultID);
		}
		return jsonaki;
	}
}
