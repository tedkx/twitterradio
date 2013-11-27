package controllers;

import java.io.File;

import play.mvc.Controller;
import play.mvc.Result;
import utils.U;

/**
 * Controller for serving the tweets audio files
 * that are created dynamically
 */
public class TweetsAudioService extends Controller {

	public static Result getAudio(Long tweetID) {
	    String path = System.getProperty("java.io.tmpdir") + tweetID.toString() + ".wav";
	    U.out("requesting audio at path " + path);
	    try {
	    	return ok(new File(path));
	    } catch(Exception ex) { 
	    	U.out("Error while fetching wav file for id " + tweetID + ", " + ex.getMessage());
	    	return ok("");
	    }
	}
}
