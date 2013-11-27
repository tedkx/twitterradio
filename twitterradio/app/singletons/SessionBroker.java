package singletons;

import play.mvc.Controller;
import play.mvc.Http.Session;

public abstract class SessionBroker {

	public static String SESSION_CURRENT_USER_ID = "currentuserid";
	public static String SESSION_CURRENT_BAND_ID = "currentbandid";
	
	public static void unsetCurrentBandID() {
		Session session = session();
		if(session != null) {
			session.remove(SESSION_CURRENT_BAND_ID);
		}
	}
	
	public static void unsetCurrentUserID() {
		Session session = session();
		if(session != null) {
			session.remove(SESSION_CURRENT_USER_ID);
		}
	}
	
	public static Long getCurrentUserID(Session session) {
		if(session == null) { session = Controller.session(); }
		try {
			return Long.parseLong(session.get(SESSION_CURRENT_USER_ID));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Long getCurrentUserID() {
		Session session = Controller.session();
		if(session == null) { session = Controller.session(); }
		try {
			return Long.parseLong(session.get(SESSION_CURRENT_USER_ID));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Integer getCurrentBandID(Session session) {
		if(session == null) { session = Controller.session(); }
		try {
			return Integer.parseInt(session.get(SESSION_CURRENT_BAND_ID));
		} catch(Exception e) {
			return null;
		}
	}
	
	public static Session session() {
		try {
			return Controller.session();
		} catch(Exception e) {
			return null;
		}
	}
}
