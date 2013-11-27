package models.db;

import play.db.ebean.Model;
import java.util.*;
import javax.persistence.*;
import play.data.format.*;
import play.data.validation.*;
import scala.Console;
import singletons.BandManager;
import utils.U;

@Entity
public class Keyword extends Model {

	@Id
	public long keywordID;
	@Constraints.Required
	public String text;
	public boolean isHashtag;
	@Formats.DateTime(pattern="dd/MM/yyyy")
	public Date created = new Date();
	@Column(nullable = true)
    @ManyToMany(cascade = CascadeType.ALL)
    public List<Tweet> tweets;
	@ManyToMany(cascade = CascadeType.ALL)
	public List<Band> bands;
	
	
	public static Finder<Long,Keyword> find = new Finder<Long,Keyword>(Long.class, Keyword.class);
	public static Keyword Create(String keywordText, Long userID) {
//		List<Keyword> keywords = Keyword.find.query()
//				.where("text = '" + keywordText + "'")
//				.findList();
		Keyword keyword = Keyword.find.query().where().eq("text", keywordText).findUnique();
		if(keyword == null)
		{
			U.out("Is band null: " + ((BandManager.getInstance().getCurrentBand(userID) == null) ? " true" :  "false"));
			keyword = new Keyword(keywordText, BandManager.getInstance().getCurrentBand(userID));
			keyword.save();
		}
		return keyword;
	}
	
	public static Keyword CreateTrendKeyword(String keywordText, Band trendsBand) {
		Keyword keyword = Keyword.find.query().where().eq("text", keywordText).findUnique();
		if(keyword == null) {
			keyword = new Keyword(keywordText, trendsBand);
		}
		return keyword;
	}

	protected Keyword(String keywordText, Band band) {
		this.text = keywordText;
		this.bands = new ArrayList<Band>();
		if(keywordText.contains("#")) { this.isHashtag = true; }
		if(!this.bands.contains(band) && band.bandName != "Trends") { 
			this.bands.add(band);
			U.out("band added");
		}
		if(!band.keywords.contains(this) && band.bandName != "Trends") {
			band.keywords.add(this);
			band.save();
		}
	}	
}
