package controllers;


import play.mvc.*;
import scala.Console;
import singletons.AccountManager;
import singletons.BandManager;
import singletons.SessionBroker;
import singletons.TtsBroker;
import singletons.TwitterBroker;
import views.html.*;
import views.html.defaultpages.error;

import org.apache.commons.lang3.StringEscapeUtils;
import models.db.Account;
import models.db.Band;
import models.db.Keyword;
import models.db.Tweet;
import models.tts.FreeTtsSpeaker;
import play.mvc.Result;
import play.mvc.With;
import securesocial.core.java.SecureSocial;

public class Application extends Controller {
  
	//@SecureSocial.SecuredAction
	@With(BaseAction.class)
    public static Result index() {
    	Console.println("**** SESSIONID: " + SessionBroker.getCurrentUserID());
    	BandManager.getInstance().removeBand();
    	Account user = AccountManager.getCurrentUser();
        return ok(index.render(user, null, null));
    }
	
	//@SecureSocial.SecuredAction
	public static Result noband() {
	    return redirect("/band/0"); 
	}

	//@SecureSocial.SecuredAction
	@With(BaseAction.class)
    public static Result band(String bandID) {
		Band band = null;
		Account user = AccountManager.getCurrentUser();
		if(bandID.equals("0") || bandID.equals("trends")) { //???
			band = BandManager.getInstance().getTrendsBand();
		} else if(bandID.matches("\\d+")) { //is numeric, search by ID
			band = Band.find.byId(Integer.parseInt(bandID));
		} else { //get or create a band by name
			band = BandManager.getInstance().getBand(bandID);
			if(band == null) {
				band = BandManager.getInstance().CreateBand(bandID);
			}
		}
		if(band == null) {
			return ok(views.html.radioerror.render("The specified band does not exist"));
		}
		BandManager.getInstance().setBand(user.accountID, band);
		utils.U.out("keywords size: " + band.keywords.size());
		return ok(views.html.band.render(user, band, null));
	}
	
	//@SecureSocial.SecuredAction
    public static Result keyword(String text) {
    	Band band = null;//BandManager.getInstance().getBand(true);
		Account user = AccountManager.getCurrentUser();
		return ok(index.render(user, band, Keyword.Create(text, user.accountID)));
	}
  
	public static Result test()
	{
		String saveFolder = System.getProperty("java.io.tmpdir");
		//String saveFolder = "savefolder";
		return ok(saveFolder);
	}
	
	//@SecureSocial.SecuredAction
	@With(BaseAction.class)
	public static Result cleanup() {
		TwitterBroker.getInstance().closeStreams();
		return ok("All streams closed");
	}
	
	public static Result testTTS() {
		String audiourl = ((FreeTtsSpeaker)FreeTtsSpeaker.getInstance()).debugSpeak("This is some text and this is a #hashtag, get it?");
		return ok("Test Tweet created: <a href=\"" + audiourl + "\">here</a>");
	}
	
//	public static Result bootstrap() {
//		Band trends = BandManager.getInstance().getTrendsBand();
//		String retstr = "Application Bootstrapped";
//		for(Keyword keyword : trends.keywords) {
//			retstr += "<br/>" + keyword.text;
//		}
//		return status(OK, retstr);
//	}
}
