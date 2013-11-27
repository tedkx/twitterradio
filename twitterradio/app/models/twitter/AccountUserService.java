package models.twitter;

import java.util.HashMap;
import play.Application;
import scala.Console;
import securesocial.core.Identity;
import securesocial.core.IdentityId;
import securesocial.core.java.BaseUserService;
import securesocial.core.java.Token;

public class AccountUserService extends BaseUserService {

	private HashMap<String, Identity> users  = new HashMap<String, Identity>();

	public AccountUserService(Application application) {
		super(application);
		Console.println("***** AccountService started");
	}
	
	@Override
	public Identity doSave(Identity user) {
		Console.println("***** AccountService SAVING IDENTITY " + user.identityId().userId());
		this.users.put(user.identityId().userId(), user);
		return user;
	}
	
	@Override
	public Identity doFind(IdentityId userId) {
		Console.println("***** AccountService FINDING USER " + userId.userId());
		return users.get(userId.userId());
	}

	@Override
	public void doDeleteExpiredTokens() { 
		Console.println("***** AccountService DELETING EXPIRED TOKENS");
//		Iterator<Map.Entry<String,Token>> iterator = tokens.entrySet().iterator();
//        while ( iterator.hasNext() ) {
//            Map.Entry<String, Token> entry = iterator.next();
//            if ( entry.getValue().isExpired() ) {
//                iterator.remove();
//            }
//        }
	}

	@Override
	public void doDeleteToken(String arg0) { }

	@Override
	public Identity doFindByEmailAndProvider(String arg0, String arg1) { return null; }

	@Override
	public Token doFindToken(String arg0) { return null; }

	@Override
	public void doSave(Token token) { }
}
