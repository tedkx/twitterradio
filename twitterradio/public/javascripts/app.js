$(document).ready(function() {
	var pathArray = window.location.href.split( '/' );
	baseURL = 'http://' + pathArray[2] + "/";
	
	initMenu();
	
	//make sure WebSocket closes when user leaves the page
	$(window).on('unload', function(){
		if(WebSocketWrapper.websocket != null) {
			WebSocketWrapper.websocket.onclose = function () {};
			WebSocketWrapper.websocket.close();
		}
	});
});

var settingsInitialized = false;
var baseURL = null;
function initMenu() {
	$('a#createband').click(Prompter.createBandDialog);
	$('a#musicsettingslink').click(Prompter.creatMusicSettingsDialog);
	
	if(settingsInitialized) { return; }
	settingsInitialized = true;
	
	Prompter.init();
	MusicPlayer.init();
	//stop setting dropdown menu item from closing on click
	$('.on-off-setting').on('click',function(event){
		event.stopPropagation();
		if($(this).attr("id") == "sentiment") { RadioStation.toggleSentiment(); }
		if($(this).attr("id") == "autoplay") { RadioStation.toggleAutoplay(); }
	});
}

function initBand(webSocketUri, keyword) {
	RadioStation.init(webSocketUri, keyword);
	if($('.keyword').last().html().length > 0) { $('#addkeywordbtn .btn').attr('disabled','true'); }
	$('#addkeywordbtn').on('click',Prompter.createKeywordDialog)
	$('#analog-wrap .keyword').on('click',RadioStation.keywordClicked);
	$('#btn-fb').click(Player.goToStart);
	$('#btn-prev').click(Player.prev);
	$('#btn-playpause').click(Player.play);
	$('#btn-stop').click(Player.stop);
	$('#btn-next').click(Player.next);
	$('#btn-ff').click(Player.goToEnd);
}

//Presents dialogs and handles their output
var Prompter = {
	init: function() {
		$(".modal#keywordbandmodal .btn").on("click", Prompter.keywordBandCallback);
		$(".modal#musicsettingsmodal #musicsearchbtn").on("click", Prompter.musicSearchCallback);
		$(".modal#musicsettingsmodal #musicfilebtn").on("click", Prompter.musicFileCallback);
		$(".modal#musicsettingsmodal #musicclearbtn").on("click", MusicPlayer.clearMedia);
		$('.alert-error').hide();
		$(".modal").modal({
			"backdrop" : "static",
			"keyboard" : true,
			"show" : false
		});
	},
	createBandDialog: function(e) {
		if(e && e.preventDefault) {	e.preventDefault(); }
		Prompter.showKeywordBandDialog('band');
	},
	createKeywordDialog: function(e) {
		e.preventDefault();
		Prompter.showKeywordBandDialog("keyword")
	},
	creatMusicSettingsDialog: function(e) {
		e.preventDefault();
		$('.alert-error').hide();
		$('#musicsettingsmodal #input').val("");
		$('.modal#musicsettingsmodal').modal('show');
		$('#musicsettingsmodal #input').focus();
	},
	showKeywordBandDialog: function(inputType) {
		$('.alert-error').hide();
		$('.modal#keywordbandmodal #keywordband').html((inputType == 'band') ? 'Band Name (no spaces allowed):' : 'New Keyword:');
		$('#keywordbandmodal #input-type').val(inputType);
		$('#keywordbandmodal #input').val("");
		$('.modal#keywordbandmodal').modal('show');
		$('#keywordbandmodal #input').focus();
	},
	musicFileCallback: function(e) {
		e.stopPropagation();
		var error = "";
		var val = $("#musicfile").val();
		if(val.replace(" ","") == "") { error = "No file selected"; }
		val = encodeURIComponent(val);
		if(error == "") {
			MusicPlayer.hideErrors();
			MusicPlayer.makeMediaRequest(val);
		} else {
			MusicPlayer.showError(error);
		}
	},
	musicSearchCallback: function(e) {
		e.stopPropagation();
		var error = "";
		var val = $("#musicsearch").val();
		if(val.replace(" ","") == "") { error = "No keywords inserted"; }
		
		if(error == "") {
			MusicPlayer.hideErrors();
			MusicPlayer.makeMediaRequest(val);
		} else {
			MusicPlayer.showError(error);
		}
	},
	setActiveSong: function(songID) {
		$('#musiclist .active').removeClass('active');
		$('#musiclist #song' + songID).addClass('active');
	},
	keywordBandCallback: function(e) {
		e.stopPropagation();
		var error = "";
		inputType = $('#keywordbandmodal #input-type').val();
		var value = $('#keywordbandmodal #input').val();
		if(value.length == 0) { error = "Name cannot be empty." };
		if(inputType == "band" && value.toLowerCase() == "trends") { error = "Trends is a reserved word, sorry."};
		if(inputType == "band" && value.indexOf(" ") > -1) { error = "No spaces are allowed"; }
		if(inputType == "keyword" && $('.keyword:contains("' + value + '")').size() > 0) { error = "You have already added that keyword."; }
		if(error == "") {
			$('#keywordbandmodal .alert-error').hide();
			$('#keywordbandmodal.modal').modal('hide');
			if(inputType == 'band') {
				window.location = baseURL + "band/" + value;
			} else {
				RadioStation.addKeyword(value);
			}
		} else {
			$('#keywordbandmodal .errortext').html(error);
			$('#keywordbandmodal .alert-error').show();
		}
	},
}

var MusicPlayer = {
	enabled: true,
	currentSongID: null,
	
	init: function() {
		$('#musiclist .close').on('click',function() { MusicPlayer.removeSong($(this).parent()); });
		if('#music-wrap').size() > 0) {
			
		}
	},
	setUIEnabled: function(enabled) {
		if(enabled) {
			$('#musicsearch').prop('disabled',false);
			$('#musicsearchbtn').prop('disabled',false);
			$('#musicsearchbtn').removeClass("disabled");
			$('#musicfile').prop('disabled',false);
			$('#musicfilebtn').prop('disabled',false);
			$('#musicfilebtn').removeClass("disabled");
		} else {
			$('#musicsearch').prop('disabled',true);
			$('#musicsearchbtn').prop('disabled',true);
			$('#musicsearchbtn').addClass("disabled");
			$('#musicfile').prop('disabled',true);
			$('#musicfilebtn').prop('disabled',true);
			$('#musicfilebtn').addClass("disabled");
		}
	},
	showError: function(errorText) {
		$('#musicsettingsmodal .errortext').html(errorText);
		$('#musicsettingsmodal .alert-error').show();
	},
	hideErrors: function() {
		$('#musicsettingsmodal .alert-error').hide();
		$('#musicsettingsmodal .errortext').html('');
	},
	addSong: function(song) {
		$('#song' + song.mediaid).remove();
		var songElem = $('<li id="song' + song.mediaid + '">' + song.title + '<button class="close">&times;</button></li>');
		songElem.data("songID",song.songid);
		$('#musiclist').append(songElem);
		songElem.find('.close').on('click',function() { MusicPlayer.removeSong($(this).parent()); });
		
		var musicAudioHtml = $("<audio id=\"audio-song-" + song.id + "\" src=\"" + song.url + "\" type=\"audio/wave\"></audio>");
		tweetAudioHtml.on('ended',Player.sound_ended);
		$('#audio-wrap').append(tweetAudioHtml);
	},
	removeSong: function(elem) {
		elem.remove();
		$.ajax({
			url: baseURL + 'async/media/remove/' + elem.attr('id').replace('song',''),
			type: "GET"
		});
	},
	clearMedia: function() {
		//stop music
		$('#mediawrap').html('');
		$('#musiclist').html('');
		$.ajax({
			url: baseURL + 'async/media/clear',
			type: "GET"
		});
	},
	makeMediaRequest: function(searchString) {
		MusicPlayer.setUIEnabled(false);
		var url = baseURL + 'async/media/get';
		if(typeof(searchString) != 'undefined') { url += "/" + searchString; }
		$.ajax({
			url: url,
			type: "GET",
			dataType: "json",
			success: MusicPlayer.mediaResultCallback,
			error: MusicPlayer.mediaResultCallback,
		});
	},
	mediaResultCallback: function(data, status, errorOrXHR) {
		MusicPlayer.setUIEnabled(true);
		if(status == "error") {
			alert("Ajax Error: " + errorOrXHR);
		} else if(typeof(data.songid) != 'undefined') {
			var fl = $('#musicfile');
			fl.replaceWith(fl = fl.clone(true));
			$('#musicsearch').val('');
			MusicPlayer.addSong(data);
		} else {
			MusicPlayer.showError("No related media found");
		}
	},
	
	play: function() {
		
	},
	stop: function() {
		
	},
	beginPrefetch: function() {
		
	}
}

// Handles incoming requests, switches keywords etc
var RadioStation = {
	recentlySwitched: false,
	switchInterval: 10000,
	currentKeyword: null,
	sentimentEnabled: true,
	autoplayEnabled: true,

	init: function(webSocketUri, keyword) {
		Player.init();
		$('#tweets-pane-loader').addClass('fadeOutUp');
		WebSocketWrapper.socketUri = webSocketUri;
		if(typeof(keyword) != 'undefined' && keyword != null) { WebSocketWrapper.init(keyword); }
	},
	
	keywordClicked: function(e) {
		if($(this).html().length == 0) { return; }
		if(RadioStation.recentlySwitched) {
			alert("Please allow 10 seconds to pass in order to change keyword due to Twitter API rate limiting.");
			return;
		}
		$('#needle').css("left",(parseInt($(this).css("left").replace("px","")) + $(this).width()/2) + 'px');
		RadioStation.switchTo($(this).html());
	},
	
	addKeyword: function(keywordText) {
		var keywords = $('.keyword');
		for(index in keywords) {
			if($(keywords[index]).html().length == 0) {
				$(keywords[index]).html(keywordText);
				return;
			}
		}
	},
	
	switchTo: function(keyword) {
		if(RadioStation.recentlySwitched) {
			alert("Please allow 10 seconds to pass in order to change keyword due to Twitter API rate limiting.");
			return;
		}
		RadioStation.recentlySwitched = true;
		setTimeout(function() { RadioStation.recentlySwitched = false; }, RadioStation.switchInterval);
		if($('#komparsos').size() > 0) { $('#komparsos').remove(); }
		$('#tweets-pane-loader').removeClass('fadeOutUp');
		$('#tweets-pane-loader').addClass('fadeInDown');
		$('#tweets-pane').empty();
		$('#audio-wrap').empty();
		Player.stop();
		WebSocketWrapper.init(keyword);
	},
	
	renderTweet: function(jsonTweet) {
		RadioStation.hideLoader();
		var bgcolor = (RadioStation.sentimentEnabled) ? " style=\"background-color:" + jsonTweet.color + ";\"" : "";
		var tweetHtml = $("<div class=\"tweet-wrap\" id=\"" + jsonTweet.id + "\"" + bgcolor + ">" +
				"	<div class=\"tweet\">" +
				"		<div class=\"tweet-overlay\"></div>" + 
				"		<div class=\"tweet-username\">@" + jsonTweet.username + "</div>" + 
				"		<div class=\"tweet-text\">" + jsonTweet.text + "</div>" + 
				"	</div>" + 
				"</div>");
		tweetHtml.data("bgcolor",jsonTweet.color);
		var tweetAudioHtml = $("<audio id=\"audio-" + jsonTweet.id + "\" src=\"" + jsonTweet.audiourl + "\" type=\"audio/wave\"></audio>");
		tweetAudioHtml.on('ended',Player.sound_ended);
		$('#audio-wrap').append(tweetAudioHtml);
		$('#tweets-pane').append(tweetHtml);
		document.getElementById('audio-' + jsonTweet.id).load();
		
		if(RadioStation.autoplayEnabled && Player.currentTweet == null) {
			Player.play(null);
		}
	},
	
	hideLoader: function() {
		var c = $('#tweets-pane-loader').attr('class');
		if(typeof(c) == 'undefined' || c == 'fadeInDown') { 
			$('#tweets-pane-loader').removeClass('fadeInDown'); 
			$('#tweets-pane-loader').addClass('fadeOutUp'); 
		}
	},
	
	toggleLoader: function() {
		if($('#tweets-pane-loader').attr('class') == 'fadeOutUp') { 
			$('#tweets-pane-loader').removeClass('fadeOutUp');
			$('#tweets-pane-loader').addClass('fadeInDown'); 
		} else {
			$('#tweets-pane-loader').removeClass('fadeInDown'); 
			$('#tweets-pane-loader').addClass('fadeOutUp'); 
		}
	},
	
	toggleSentiment: function() {
		if($('#sentiment .onoffswitch-inner').css("margin-left") == "0px") {
			RadioStation.sentimentEnabled = false;
			$(".tweet-wrap").css({backgroundColor: "#FFF"});
		} else {
			RadioStation.sentimentEnabled = true;
			$(".tweet-wrap").each(function() {
				$(this).css({backgroundColor: $(this).data("bgcolor")});
			});
		}
	},
	
	toggleAutoplay: function() {
		if($('#autoplay .onoffswitch-inner').css("margin-left") == "0px") {
			RadioStation.autoplayEnabled = false;
		} else {
			RadioStation.autoplayEnabled = true;
		}
	}
};

// Handles UI elements and audio playing
var Player = {
	currentTweet: null,
	lastTweet: null,
	pane: null,
	status: 'stopped',
	
	init: function() {
		this.pane = $('#tweets-pane').css('left',0);
	},
	
	// Audio Members
	sound_play: function() {
		if(this.currentTweet == null) { return; }
		var audioelem = document.getElementById('audio-' + this.currentTweet.attr('id'));
		audioelem.play();
		//play sound if currentTweet is not null
	},
	sound_stop: function(pause) {
		if(this.currentTweet == null) { return; }
		if(typeof(pause) == 'undefined') { pause = false; }
		var audioelem = document.getElementById('audio-' + this.currentTweet.attr('id'));
		if(audioelem == null) { return; } //when switching keyword this is possible
		if(audioelem.paused) { return; }
		audioelem.pause();
		if(!pause) { audioelem.currentTime = 0; }
	},
	sound_ended: function(e) {
		if(Player.currentTweet.size() > 0 && Player.currentTweet.attr('id') == Player.pane.children().last().attr('id')) { 
			Player.stop();
		} else {
			Player.next();
		}
	},
	
	// UI Members
	play: function(e) {
		if(e != null) { e.preventDefault(); }
		if($('.tweet-wrap.speaking').size() > 0) {
			if(document.getElementById('audio-' + Player.currentTweet.attr('id')).paused) {
				Player.sound_play();
			} else {
				Player.sound_stop(true);
			}
			return; 
		}
		var tweet = Player.getNextTweet();
		if(tweet != null) { Player.transition(tweet); }
	},
	
	pause: function(e) {
		if(e != null) { e.preventDefault(); }
		Player.stop(e);
	},
	
	stop: function(e) {
		if(e != null) { e.preventDefault(); }
		Player.transition(null);
	},

	next: function(e) {
		if(e != null) { e.preventDefault(); }
		var tweet = Player.getNextTweet();
		if(tweet != null) { Player.transition(tweet,'next'); }
	},
	
	goToStart: function(e) {
		if(e != null) { e.preventDefault(); }
		var tweet = (Player.pane.children().size() == 0) ? null : Player.pane.children().first();
		Player.transition(tweet,'start');
	},
	
	goToEnd: function(e) {
		if(e != null) { e.preventDefault(); }
		var tweet = (Player.pane.children().size() == 0) ? null : Player.pane.children().last();
		Player.transition(tweet,'end');
	},
	
	prev: function(e) {
		if(e != null) { e.preventDefault(); }
		var tweet = Player.getNextTweet(true);
		if(tweet != null) { Player.transition(tweet,'prev'); }
	},
	
	transition: function(tweet, action) {
		if(this.currentTweet != null && tweet != null && tweet.attr('id') == this.currentTweet.attr('id')) { return; }
		if(this.pane.is(':animated')) { this.pane.finish(); }
		
		this.sound_stop();
		
		var totalTweets = this.pane.children().size();
		if(typeof(action) == 'undefined') { action = 'stop'; }
		if(this.currentTweet != null) { this.currentTweet.removeClass('speaking'); }
		var l = 0, minLeft = (totalTweets - 3) * -200; //keep in mind, pane gets only negative valooz
		switch(action) {
			case 'next'	: 
			case 'prev'	:	l = (tweet.index() - 1) * -200; break;
			case 'start': 	l = 0; break;
			case 'end'	:	l = minLeft;
			default		:	break;
		}
		var tweetIndex = (tweet == null) ? 0 : tweet.index();
		$('#keyword-display').html("Playing tweet " + (tweetIndex + 1) + " of " + totalTweets);
		if(tweet != null) {
			if(l < minLeft) { l = minLeft; }
			if(l > 0 || totalTweets <= 2) { l = 0; }
			this.pane.animate({left: l + 'px'},500);
			tweet.addClass('speaking');
		}		
		this.setCurrentTweet(tweet);
		
		this.sound_play();
	},
	
	setCurrentTweet: function(tweet) {
		this.currentTweet = tweet;
		if(tweet != null) { this.lastTweet = tweet; }
	},
	
	getNextTweet: function(backward) {
		if(typeof(backward) == 'undefined') { backward = false; }
		if($('#tweets-pane').children().size() == 0) { 
			this.setCurrentTweet(null);
			return null; 
		}
		var tweet = $('.tweet-wrap.speaking');
		if(tweet.size() == 0) {
			tweet = this.pane.children().first();
		} else {
			tweet = (backward) ? tweet.prev() : tweet.next();
			if(tweet.size() == 0) { tweet = null; }
		}
		return tweet;
	},
}

var WebSocketWrapper = window.WebSocketWrapper = {
	websocket: null,
	socketUri: null,
	keyword: null,

	init: function(withKeyword) {
		this.keyword = withKeyword;
		if(this.websocket != null && this.websocket.readyState == 1) { this.websocket.close(); }
		this.websocket = new WebSocket(this.socketUri);
		this.websocket.onopen = function(evt) { WebSocketWrapper.onOpen(evt) }; 
		this.websocket.onclose = function(evt) { WebSocketWrapper.onClose(evt) }; 
		this.websocket.onmessage = function(evt) { WebSocketWrapper.onMessage(evt) }; 
		this.websocket.onerror = function(evt) { WebSocketWrapper.onError(evt) };
	},

	sendMessage: function(text) { WebSocketWrapper.websocket.send(text) },
	
	/* WebSocket members START */
	onOpen: function(evt) { 
		WebSocketWrapper.sendMessage(WebSocketWrapper.keyword);
	},
	onClose: function(evt) { console.log("WebSocket disconnected"); },
	onMessage: function(evt) {
		try {
			var rawObject = jQuery.parseJSON(evt.data);
			RadioStation.renderTweet(rawObject);
		} catch(err) {
			console.log("could not parse json data: " + evt.data);
		}
	},
	onError: function(evt) { console.log("WebSocket error: " + evt.data); },
	/* WebSocket members END */
};

