package singletons;

import java.io.File;
import java.util.HashMap;
import utils.Reflector;
import utils.U;
import models.db.Tweet;
import interfaces.*;

public class TtsBroker {
	private static TtsBroker instance = null;
	public String speakerClass;
	protected boolean isSpeakerSingleton;
	protected String dir = "";
	private HashMap<String,String> properties;
	
	protected TtsBroker() {
		speakerClass = U.getProperty("twadio.TextToSpeechClass");
		this.properties = U.getProperties(new String[] {"twadio.TextToSpeechClass"});
		//speakable = Reflector.instantiate(this.properties.get("TextToSpeechClass"), ISpeakable.class);
		//speakable = new FreeTtsSpeaker();
	}
	
	/**
	 * Creates an audio file that contains the spoken text
	 * of the tweet, sets its audioURL property and saves it
	 * @param tweet
	 * @return
	 */
	public String speak(Tweet tweet)
	{
		ensureAudioDirectoryExists();
		ISpeaker speaker = (ISpeaker) Reflector.instantiate(speakerClass, ISpeaker.class);
		return speaker.speak(tweet);
	}
	
	protected void ensureAudioDirectoryExists()
	{
		String audioDir = U.getProperty("twadio.AudioDirectory");
		File dir = new File(audioDir);
		if(!dir.exists()) { dir.mkdir(); }
	}
	
	public void setDirectory(String directory) {
		this.dir = directory;
	}
	
	public String getDirectory() {
		return this.dir;
	}
	
	public static TtsBroker getInstance() {
		if(instance == null) { instance = new TtsBroker(); }
		return instance;
	}

	public void cleanup()
	{
		if(isSpeakerSingleton) {
			((ISpeaker) Reflector.instantiate(speakerClass, ISpeaker.class)).cleanup();
		}
	}
}
