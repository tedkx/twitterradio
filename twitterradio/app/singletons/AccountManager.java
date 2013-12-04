package singletons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import play.mvc.Http.Context;
import scala.Console;
import securesocial.core.Identity;
import securesocial.core.java.SecureSocial;
import utils.U;
import models.db.Account;
import models.db.Band;

public class AccountManager {

	private static AccountManager instance;
	private HashMap<Long,Account> accounts;
	
	private AccountManager() {
		accounts = new HashMap<Long,Account>();
		List<Account> accountsList = Account.find.all();
		if(accountsList.size() == 0) { 
			Account debugAccount = new Account();
			debugAccount.bands = new ArrayList<Band>();
			debugAccount.fullName = "Thodwris Kalaitzidis";
			debugAccount.secret = "fbN9cbFrhLlJX36XfNSAMk1UUDxdY7PunQ8bZPBmBQ";
			debugAccount.token = "21860462-JFYoXCt6BoQEsEBnxFIPDGiHy4CuypNOa2ZAR035M";
			debugAccount.save();
			accountsList.add(debugAccount);
		}
		for(Account acc : accountsList) {
			accounts.put(acc.accountID, acc);
		}
	}
	
	public static AccountManager getInstance() {
		if(instance == null) { instance = new AccountManager(); }
		return instance;
	}
	
	public static Account get(Long userID) {
		if(AccountManager.getInstance().accounts.containsKey(userID)) { 
			return AccountManager.getInstance().accounts.get(userID); 
		}
		return null;
	}
	
	public HashMap<Long,Account> getAll() { return accounts; }
	
	public void ensureAccountExists(Context ctx) {
		Identity user = getIdentity(ctx);
		if(user == null) { return; }
		
		Account acc = accounts.get(Long.parseLong(user.identityId().userId()));
		if(acc == null) {
			acc = new Account();
			acc.accountID = Long.parseLong(user.identityId().userId());
			acc.fullName = user.fullName();
			acc.token = (U.DEBUG) ? U.getProperty("twadio.MyToken") : user.oAuth1Info().get().token();
			acc.secret = (U.DEBUG) ? U.getProperty("twadio.MySecret") : user.oAuth1Info().get().secret();
			acc.musicProvider = "youtube";
			acc.save();
			accounts.put(acc.accountID, acc);
		}
		try { 
			ctx.session().put(SessionBroker.SESSION_CURRENT_USER_ID,String.valueOf(acc.accountID));
		} catch(Exception ex) { Console.println("Account creation exception: " + ex.getMessage()); }
	}
	
	public static Identity getIdentity(Context ctx) {
		if(U.DEBUG) {
			return new models.DebugIdentity();
		} else {
			return (Identity) ctx.args.get(SecureSocial.USER_KEY);
		}
	}

	public static Account getCurrentUser() {
		Long userID = SessionBroker.getCurrentUserID();
		if(userID != null) { return AccountManager.get(userID); }
		return null;
	}
}
