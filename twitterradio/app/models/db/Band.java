package models.db;

import play.api.templates.Html;
import play.data.format.Formats;
import play.db.ebean.*;
import java.util.*;
import javax.persistence.*;
import org.apache.commons.lang3.StringEscapeUtils;

@Entity
public class Band extends Model {

	@Id
	public int bandID;
	public String bandName;
	public boolean isTrendsBand;
	
	@ManyToMany(cascade = CascadeType.ALL)
    @Column(nullable = true)
    public List<Keyword> keywords;

	@ManyToOne(cascade = CascadeType.ALL)
	public Account account;
	@Formats.DateTime(pattern="dd/MM/yyyy")
	public Date created = new Date();
	
	public static Finder<Integer,Band> find = new Finder<Integer,Band>(Integer.class, Band.class);
	
	private Band() {  }

	public Band(String bandName, Account user) {
		this.bandName = bandName;
		this.account = user;
		this.isTrendsBand = false;
		this.keywords = new ArrayList<Keyword>();
		this.created = new Date();
	}
	
	public String toMenuHtmlString() {
    	String htmlStr = "<div class=\\\"menu-item-wrap\\\">" + 
    			"<div class=\\\"menu-item\\\">" + StringEscapeUtils.escapeEcmaScript(this.bandName) + "</div>";
    	for(Keyword k : this.keywords) {
    		htmlStr += "<div class=\\\"submenu-item-wrap\\\">" +
				"<div class=\\\"submeni-item\\\">" + StringEscapeUtils.escapeEcmaScript(k.text) + "</div>" +
				"</div>";
    	}
    	return htmlStr + "</div>";
    }
    
    public Html toMenuHtml() {
    	return Html.apply(this.toMenuHtmlString());
	}
    
    public Html renderKeywords() {
    	String html = "";
    	int i = 0;
    	int rownum = 0;
    	for(i = 0;i<this.keywords.size(); i++) {
    		rownum++;
    		if(rownum > 3) { rownum = 1; }
    		if(i == this.keywords.size() -1 ) { rownum = 2; }
    		html += "<div class=\"keyword keyword-row" + rownum + "\" style=\"left:" + (100 + i*65) + "px;\">" + this.keywords.get(i).text + "</div>";
    	}
    	for(int j = i; j<10;j++) {
    		rownum++;
    		if(rownum > 3) { rownum = 1; }
    		if(j == 9) { rownum = 2; }
    		html += "<div class=\"keyword keyword-row" + rownum + "\" style=\"left:" + (100 + j*65) + "px;\"></div>";
    	}
    	return Html.apply(html);
    }
    
    public String getLink() {
    	return "band/" + this.bandID;
    }
    
	public String toJson(){
		return "{" + 
			"\"id\": \"" + this.bandID + "\", " +
			"\"html\": \"" + this.toMenuHtmlString() + "\", " +
			"\"bandName\": \"" + this.bandName + "\", " +
			"\"created\": \"" + this.created.toString() + "\"" +
			"}";
	}

	/**
	 * Don't stop to check if band exists. If this method
	 * was called, it doesn't
	 * @param userID
	 * @return
	 */
//    public static Band Create(String bandName,Long userID) {
//    	Band band = null;
//    	Account user = Account.find.byId(userID);
//    	if(user != null) {
//    		band = Band.Create(bandName, user);
//    	}
//    	return band;
//    }
//    
//    /**
//	 * Don't stop to check if band exists. If this method
//	 * was called, it doesn't
//	 * @param userID
//	 * @return
//	 */
//    public static Band Create(String bandName, Account user) {
//    	Band band = new Band(bandName, user);
//    	band.save();
//    	return band;
//    }
    
    
}
