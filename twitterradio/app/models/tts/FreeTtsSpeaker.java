package models.tts;

import interfaces.ISpeaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import models.db.Tweet;

import play.Play;
import play.db.ebean.Model;
import utils.U;

import com.sun.speech.freetts.*;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

public class FreeTtsSpeaker extends Model implements ISpeaker {

    public static final boolean isSingleton = true;
	private com.sun.speech.freetts.Voice defaultVoice;
    private com.sun.speech.freetts.Voice hashtagVoice;
    private SingleFileAudioPlayer sfap;
    private static FreeTtsSpeaker instance = null;
	
    /**
     * Master constructor with limited visibility because 
     * initialization begins from getInstance()
     */
	protected FreeTtsSpeaker()
	{
//		System.setProperty("mbrola.base",Play.application().path().getAbsolutePath() + "/lib/mbrola");
//		com.sun.speech.freetts.Voice[] voices = VoiceManager.getInstance().getVoices();
//        for(com.sun.speech.freetts.Voice vo : voices) {
//            System.out.println(vo.getName() + " " + vo.getGender().toString());
//        }
		defaultVoice = VoiceManager.getInstance().getVoice(U.getProperty("twadio.DefaultVoice"));
		defaultVoice.allocate();
		//hashtagVoice = VoiceManager.getInstance().getVoice(U.getProperty("twadio.SecondaryVoice"));
		//hashtagVoice.allocate();
	}
	
	@Override
	public List<SupportedFeature> getSupportedFeatures() {
		ArrayList<SupportedFeature> supp = new ArrayList<SupportedFeature>();
		supp.add(SupportedFeature.LanguageDetection);
		supp.add(SupportedFeature.Mp3Conversion);
		return supp;
	}
	
	@Override
	public boolean supports(SupportedFeature feature) {
		return this.getSupportedFeatures().contains(feature);
	}

	public static ISpeaker getInstance() {
		if(instance == null) { instance = new FreeTtsSpeaker(); }
		return instance;
	}
	
	public String debugSpeak(String text)
	{
		Random rand = new Random();
		Long id = rand.nextLong();
		try {
			sfap = new SingleFileAudioPlayer(U.getProperty("twadio.AudioDirectory") + "/" + id, 
					javax.sound.sampled.AudioFileFormat.Type.WAVE);
			defaultVoice.setAudioPlayer(sfap);
			hashtagVoice.setAudioPlayer(sfap);

			U.out("default speaking");
			defaultVoice.speak("This is some text and this is a ");
		    U.out("hashtag speaking");
			hashtagVoice.speak("hashtag");
			U.out("default speaking");
			defaultVoice.speak(", get it?");
			sfap.close();
		} catch (Exception e) { //pokemon exception
			System.out.println(e.getMessage());
		}
		
		return U.getProperty("application.baseURL") + U.getProperty("twadio.AudioWebPath") + "/" + id + ".wav";
	
	}

	@Override
	public String speak(Tweet tweet) {
		try {
			String absPath = System.getProperty("java.io.tmpdir") + tweet.tweetID;
			//String absPath = U.getProperty("twadio.AudioDirectory") + "/" + tweet.tweetID;
			U.out("path to save: " + absPath);
			sfap = new SingleFileAudioPlayer(absPath, javax.sound.sampled.AudioFileFormat.Type.WAVE);
			defaultVoice.setAudioPlayer(sfap);
			//hashtagVoice.setAudioPlayer(sfap);

			defaultVoice.speak(tweet.text);
			sfap.close();
		} catch (Exception e) { //pokemon exception
			System.out.println("FreeTtsSpeaker.speak() error: " + e.getMessage());
		}

		//tweet.audioUrl = U.getProperty("application.baseURL") + U.getProperty("twadio.AudioWebPath") + "/" + tweet.tweetID + ".wav";
		tweet.audioUrl = U.getProperty("application.baseURL") + "audio/" + tweet.tweetID;
		//tweet.save();
		return tweet.audioUrl;
	}

	@Override
	public void cleanup()
	{
		//defaultVoice.deallocate();
		hashtagVoice.deallocate();
		instance = null;
	}

}
