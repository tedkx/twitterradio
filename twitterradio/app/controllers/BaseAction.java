package controllers;

import play.mvc.*;
import singletons.AccountManager;


public class BaseAction extends Action.Simple {
	
	/** BaseAction intercepts controller action and stores current
	 * user in the database, if he doesn't exist. Also ensures that
	 * user has at least the default band
	 */
	public Result call(Http.Context ctx) throws Throwable
	{
		AccountManager.getInstance().ensureAccountExists(ctx);
		return delegate.call(ctx);
	}

}