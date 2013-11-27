package controllers;

import models.db.Tweet;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import singletons.BandManager;
import singletons.SentimentManager;
import singletons.WebSocketManager;

public class Async extends Controller {
	
	@With(BaseAction.class)
	public static Result trends() {
		ctx().response().setHeader("Content-Type", "application/json; charset=UTF-8");
		return status(OK,BandManager.getInstance().getTrendsBand().toJson());
	}
	
	@With(BaseAction.class)
	public static WebSocket<String> stream() {
		return WebSocketManager.getInstance().initializeSocket();
	}
	
	@With(BaseAction.class)
	public static Result sentiment() {
		Tweet tweet = new Tweet(12345678L, "some status", "username");
		return status(OK,SentimentManager.getInstance().process(tweet));
	}
	
	public static Result medialink() {
		//JsonNode json = request().body().asJson();
		//9351bb67a67e6f57eb2491b1571b9e96
	}
}
