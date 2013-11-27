var soundsFolder = "http://localhost:9000/public/audio/";
var dummyTweets = [
	{ id:"123435", audiourl: soundsFolder + "359460132574330881.wav", text: "Khan!!", username:"SomeGuy", color:"#F5FFF5" }, 
	{ id:"223435", audiourl: soundsFolder + "359460375307104257.wav", text: "Beam me up Scotty", username:"cutexoxo16", color:"#C3FFC3" }, 
	{ id:"323435", audiourl: soundsFolder + "359460896734584834.wav", text: "I'm HAL 9000", username:"yoloz", color:"#FF9999" },
	{ id:"423435", audiourl: soundsFolder + "359461106336530433.wav", text: "This is a big pile of sh*t!", username:"Wh4t3v4t_3n|)", color:"#D5FFD5" }, 
	{ id:"523435", audiourl: soundsFolder + "lack_of_faith.wav", text: "I find your lack of faith disturbing", username:"Stoya", color:"#FFB6B6" },
	{ id:"623435", audiourl: soundsFolder + "run.wav", text: "Run Forest, run!!", username:"Swagger1023", color:"#FFE9E9" }, 
	{ id:"723435", audiourl: soundsFolder + "your_father.wav", text: "I am your father", username:"SomeGuy", color:"#A8FFA8" }
];
var lastTweetAdded = -1;
	
$(document).ready(function() {

	$('#loadtweets').click(debug_loadtweets);
	$('#addtweet').click(debug_addtweet);
	
	$('#animate').click(function() { 
		if($('#tweets-pane-loader').attr('class') == 'fadeOutUp') { 
			$('#tweets-pane-loader').removeClass('fadeOutUp');
			$('#tweets-pane-loader').addClass('fadeInDown'); 
		} else {
			$('#tweets-pane-loader').removeClass('fadeInDown'); 
			$('#tweets-pane-loader').addClass('fadeOutUp'); 
		}		
	});
});

function debug_loadtweets(e) {
	alert('loadingtweets');
}

function debug_addtweet(e) {
	lastTweetAdded = (lastTweetAdded >= (dummyTweets.length - 1)) ? 0 : lastTweetAdded + 1;
	RadioStation.renderTweet(dummyTweets[lastTweetAdded]);
	//alert('addingtweets: adding tweet #' + (lastTweetAdded+1) + " of " + dummyTweets.length);
}

function debug_tweetjson() {
	var json = '';
}