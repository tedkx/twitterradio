package singletons;

import java.util.HashMap;
import java.util.List;
import scala.Console;
import utils.U;
import models.db.Account;
import models.db.Band;

/**
 * Manager for user's CURRENT band and trends
 */
public class BandManager {

	private HashMap<Long,Band> currentUserBand;
	private Band trendsBand;
	
	private static BandManager instance;
	private BandManager() {
		currentUserBand = new HashMap<Long,Band>();
		trendsBand = TwitterBroker.getInstance().getTrends();
	}
	
	public static BandManager getInstance() {
		if(instance == null) { instance = new BandManager(); }
		return instance;
	}
	
	/**
	 * Sets the current band for the supplied user
	 * @param userID
	 * @param bandID
	 */
	public void setBand(Long userID, Band band) {
		this.currentUserBand.put(userID, band);
		SessionBroker.session().put(SessionBroker.SESSION_CURRENT_BAND_ID, band.bandID + "");
	}
	
	/**
	 * Gets the band that corresponds to the supplied id
	 */
	public Band getBand(int bandID) {
		return Band.find.byId(bandID);
	}
	
	/**
	 * Gets the band that belongs to the current user
	 * by its name
	 */
	public Band getBand(String bandName){
		List<Band> bands = Band.find.query().where().eq("band_name", bandName)
				.where().eq("account_account_id", AccountManager.getCurrentUser().accountID).findList();
		Console.println("found " + bands.size() + " bands with name " + bandName);
		if(bands.size() > 0) { return bands.get(0); }
		return null;
//		for(Band band : AccountManager.getCurrentUser().bands){
//			if(band.bandName == bandName) { return band; }
//		}
//		return null;
	}

	/**
	 * Returns the current band for the user id supplied
	 * @param userID
	 * @return
	 */
	public Band getCurrentBand(Long userID) {
		if(this.currentUserBand.containsKey(userID)) { return this.currentUserBand.get(userID); }
		return null;
	}
	
	/**
	 * Returns the current band for the current user
	 * @param userID
	 * @return
	 */
	public Band getCurrentBand() {
		return getCurrentBand(SessionBroker.getCurrentUserID());
	}
//	
//	public Band getBand(boolean force) {
//		if(!force) { return getBand(); }
//		Long userID = SessionBroker.getCurrentUserID();
//		if(!this.currentUserBand.containsKey(userID)) { 
//			this.setBand(userID, Band.Create(userID));
//		}
//		return this.currentUserBand.get(userID);
//	}
	
	public Band CreateBand(String bandName) {
		Console.println("Creating band " + bandName);
		Account user = AccountManager.getCurrentUser();
		Band band = new Band(bandName, user);
		band.save();
		U.out("User is null: " + ((user == null) ? "true" : "false"));
		U.out("Userbands is null: " + ((user.bands == null) ? "true" : "false"));
		user.bands.add(band);
		this.setBand(user.accountID, band);
		return band;
	}
	
	/** 
     * This band is not user-dependent, it is the same for
     * all users. It just needs to be refreshed every now
     * and then
     */
    public static Band CreateTrendsBand() {
    	Band band = new Band("trends", null);
    	band.bandName = "Trends";
    	band.bandID = -1;
    	band.isTrendsBand = true;
    	return band;
    }
	
	public void removeBand() {
		SessionBroker.unsetCurrentBandID();
		Long userID = SessionBroker.getCurrentUserID();
		if(currentUserBand.containsKey(userID)) {
			this.currentUserBand.remove(userID);
		}
	}
	
	public void removeBand(Long userID) {
		this.currentUserBand.remove(userID);
	}

	public Band getTrendsBand() {
		if(trendsBand == null || TwitterBroker.TrendsAreStale()) {
			trendsBand = TwitterBroker.getInstance().getTrends();
		}
		return trendsBand;
	}
}
