package models.tts;

import play.db.ebean.Model;
import com.sun.speech.freetts.*;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

public class Voice extends Model {

	private String name;
    private com.sun.speech.freetts.Voice systemVoice;
    private SingleFileAudioPlayer sfap;
	
	public Voice(String voiceName)
	{
		name = voiceName;
		systemVoice = VoiceManager.getInstance().getVoice(name);
		systemVoice.allocate();
	}
	
	/**
	 * Outputs the supplied text into a file and returns the filename
	 * @param txt
	 * @return
	 */
	public String speak(String txt)
	{
		String filename = "wavs/tweetid.wav";
		try {
			this.sfap = new SingleFileAudioPlayer(filename, javax.sound.sampled.AudioFileFormat.Type.WAVE);
			//Override audio player class to output byte array in order to convert to ogg
			this.systemVoice.setAudioPlayer(sfap);
			systemVoice.speak(txt);
			sfap.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			//pokemon exception
		}
		return filename;
	}
	
	public void speak(String[] txts) 
	{
		for(String txt : txts)
		{
			this.speak(txt);
		}
	}
	
	public void deallocate()
	{
		this.systemVoice.deallocate();
	}
}
