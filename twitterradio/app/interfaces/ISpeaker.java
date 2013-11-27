package interfaces;

import java.util.List;

import models.db.Tweet;

public interface ISpeaker {

	public enum SupportedFeature {
		LanguageDetection ("LanguageDetection", 1),
		Mp3Conversion ("Mp3Conversion", 2);
		
		private final String name;
	    private final int value;
	    SupportedFeature(String name, int value) {
	        this.name = name;
	        this.value = value;
	    }
	}
	
	/**
	 * Gets a list of features that the implementation supports
	 * @return List
	 */
	public List<SupportedFeature> getSupportedFeatures();
	
	/**
	 * Determines if a specific feature is supported by the implementation
	 * @param The SupportedFeature to test
	 * @return boolean
	 */
	public boolean supports(SupportedFeature feature);
	
	/**
	 * Converts the specified tweet to speech and returns the 
	 * filename it was saved to
	 * @param tweet
	 * @return Audio file name
	 */
	public String speak(Tweet tweet);
	
	/**
	 * Frees up resources and prepares for gc
	 */
	public void cleanup();
}
