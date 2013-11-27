package models;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.WebSocket;
import scala.Console;
import singletons.TwitterBroker;
import singletons.WebSocketManager;

public class TwWebSocket extends WebSocket<String> {

	private long userID;
	private WebSocket.In<String> in;
	private WebSocket.Out<String> out;
	private TwWebSocket instance;
	public TwWebSocket(long userID) {
		this.userID = userID;
		this.instance = this;
	}
	
	public long getUserID() { return this.userID; }
	
	@Override
	public void onReady(play.mvc.WebSocket.In<String> in, play.mvc.WebSocket.Out<String> out) {
		this.in = in;
		this.out = out;

		utils.U.out("***********************************");
		utils.U.out("Socket for user " + userID + " online");
		in.onMessage(new Callback<String>() {
			public void invoke(String event) { 
				utils.U.out("***********************************");
				utils.U.out("WebSocket received message " + event);
				WebSocketManager.getInstance().processWebSocketMessage(instance, event);
			} 
		});

		in.onClose(new Callback0() {
			public void invoke() { 
				utils.U.out("***********************************");
				utils.U.out("User " + userID + " disconnected");
				TwitterBroker.getInstance().closeUserStream(userID);
			}
		});
		
	}
	
	public void send(String text) {
		try {
			out.write(text);
		} catch(Exception ex) { System.out.println("WEBSOCKET OUT ERROR: " + ex.getMessage()); }
	}
}
