package models;

import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;

public class MediaResult {

	public String title = "";
	public String url = "";
	public String songID = "";

	public ObjectNode toJSON() {
		ObjectNode jsonaki = Json.newObject();
		if(title != "") { jsonaki.put("title", title); }
		if(url != "") { jsonaki.put("url", url); }
		if(url != "") { jsonaki.put("songid", songID); }
		return jsonaki;
	}
}
