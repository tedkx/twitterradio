package utils;

import org.apache.commons.lang3.StringEscapeUtils;

import models.db.Band;
import play.api.templates.Html;
import play.mvc.Http;
import singletons.AccountManager;
import singletons.BandManager;

public class UI {

	public static String TWEET_OVERLAY_HTML = "<div class=\\\"tweet-overlay\\\"></div>";
	
	public static Html renderBands()
	{
		String basePath = "http://" + Http.Context.current().request().host() + "/";
		BandManager bm = BandManager.getInstance();
		String htmlStr = "<li><a href=\"" + basePath + "band/trends\">Trends</a></li>\n" +
				"<li class=\"divider\"></li>";
		if(AccountManager.getCurrentUser().bands != null) {
			for(Band b : AccountManager.getCurrentUser().bands)
			{
				htmlStr += "<li><a href=\"" + basePath + b.getLink() + "\">" + StringEscapeUtils.escapeHtml4(b.bandName) + "</a></li>";
			}
		}
		
		return Html.apply(htmlStr);
	}
}
