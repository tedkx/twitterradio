package utils;

import play.Application;
import play.GlobalSettings;
import singletons.BandManager;
import singletons.TwitterBroker;

public class Global extends GlobalSettings {

	@Override
	public void onStart(Application app) {
		super.onStart(app);
		//initialize trends
		TwitterBroker.getInstance();
		BandManager.getInstance().getTrendsBand();
		
		//schedule trends refreshing
		
	}
	
	@Override
	public void onStop(Application app) {
		TwitterBroker.getInstance().closeStreams();
		super.onStop(app);
	}
}
