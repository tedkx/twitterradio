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
function onYouTubePlayerAPIReady() { MusicPlayer.init(); }
var settingsInitialized = false;
var baseURL = null;
function initMenu() {
	$('a#createband').click(Prompter.createBandDialog);
	$('a#musicsettingslink').click(Prompter.creatMusicSettingsDialog);
	
	if(settingsInitialized) { return; }
	settingsInitialized = true;
	
	if($('#slider').size() > 0) { 
		$('#slider').slider( {
			orientation: "vertical",
			tooltip: "hide",
			max: 100,
			value:0			
		}).on('slide', function(e) { 
			MusicPlayer.setVolume(100 - e.value);
		}); 
		$('.slider').css("height","150px");
	}
	Prompter.init();
	//MusicPlayer.init();
	//stop setting dropdown menu item from closing on click
	$('.on-off-setting').on('click',function(event){
		event.stopPropagation();
		if($(this).attr("id") == "sentiment") { RadioStation.toggleSentiment(); }
		if($(this).attr("id") == "autoplay") { RadioStation.toggleAutoplay(); }
		if($(this).attr("id") == "musiconoff") { MusicPlayer.toggle(); }
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

//Handles music playback, using a provider to provide abstraction.
//Initializes and then awaits provider to be ready in order to start playback
var MusicPlayer = {
	enabled: true,
	status: "stopped",
	currentIndex: -1,
	currentSongID: null,
	provider: null,
	
	init: function() {
		$('#musiclist .close').on('click',function() { MusicPlayer.removeMedia($(this).parent()); });
		if(typeof(window.initialSongs) != 'undefined') {
			for(var i = 0;i < window.initialSongs.length; i++) {
				var s = window.initialSongs[i];
				var elem = $('<li id="song' + s.id + '">' + s.title + '<button class="close">&times;</button></li>');
				$('#musiclist').append(elem);
				elem.data('songID',s.songID);
				elem.data('url',s.url);	
				MusicPlayer.wireUIEvents(elem);
			}
		}
		if($('#music-wrap').size() > 0) {
			MusicPlayer.provider = YouTubeProvider;
			MusicPlayer.provider.init();
		}
	},
	
	addMedia: function(song) {
		$('#song' + song.mediaid).remove();
		var songElem = $('<li id="song' + song.mediaid + '">' + song.title + '<button class="close">&times;</button></li>');
		songElem.data("songID",song.songid);
		songElem.data("url",song.url);
		$('#musiclist').append(songElem);
		MusicPlayer.wireUIEvents(songElem);
		
		if(MusicPlayer.provider != null && typeof(MusicPlayer.provider.addMedia) == 'function') { 
			MusicPlayer.provider.addMedia(song); 
		}
	},
	beginPrefetch: function() {
		var shouldPrefetch = ($('#musiclist').size() - MusicPlayer.currentIndex < 2); 
		alert(MusicPlayer.currentIndex + ' of ' + $("#musiclist").size() + ", should prefetch: " + shouldPrefetch);
	},
	clearMedia: function() {
		MusicPlayer.stop();
		if(typeof(MusicPlayer.provider.clearMedia) == 'function') { MusicPlayer.provider.clearMedia(); }
		$('#music-wrap').html('');
		$('#musiclist').html('');
		$.ajax({
			url: baseURL + 'async/media/clear',
			type: "GET"
		});
	},
	ended: function(e) {
		MusicPlayer.stop();
		MusicPlayer.beginPrefetch();
		MusicPlayer.play();
	},
	getNextSongElem: function() {
		MusicPlayer.currentIndex++;
		var elem = $('#musiclist li:eq(' + MusicPlayer.currentIndex + ')');
		if(elem.size() == 0) { 
			MusicPlayer.currentIndex = 0;
			elem = $('#musiclist li:eq(' + MusicPlayer.currentIndex + ')');
		}
		return elem;
	},
	hideErrors: function() {
		$('#musicsettingsmodal .alert-error').hide();
		$('#musicsettingsmodal .errortext').html('');
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
			MusicPlayer.addMedia(data);
		} else {
			MusicPlayer.showError("No related media found");
		}
	},
	play: function(elem) {
		if(typeof(elem) == 'undefined' || elem.size() == 0) {
			elem = MusicPlayer.getNextSongElem();
		} else {
			MusicPlayer.currentIndex = elem.index();
		}
		$('#musiclist .active').removeClass('active');
		elem.addClass('active');
		MusicPlayer.setStatus(true);
		if(MusicPlayer.provider != null) { MusicPlayer.provider.play(elem); }
	},
	providerReady: function() {
		MusicPlayer.play();
	},
	removeMedia: function(elem) {
		var elemData = elem.data(); //keep this, when elem is removed, so is its data
		elem.remove();
		$.ajax({
			url: baseURL + 'async/media/remove/' + elem.attr('id').replace('song',''),
			type: "GET"
		});
		if(MusicPlayer.provider != null && typeof(MusicPlayer.provider.removeMedia) == 'function') { 
			MusicPlayer.provider.removeMedia(elemData); 
		}
	},
	setStatus: function(playing) {
		if(playing) { MusicPlayer.status = "playing"}
		else { MusicPlayer.status = "stopped"; }
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
	setVolume: function(value){
		if(MusicPlayer.provider != null && typeof(MusicPlayer.provider.setVolume) == 'function') {
			MusicPlayer.provider.setVolume(value);
		}
	},
	showError: function(errorText) {
		$('#musicsettingsmodal .errortext').html(errorText);
		$('#musicsettingsmodal .alert-error').show();
	},
	stop: function() {
		$('#musiclist .active').removeClass('active');
		MusicPlayer.setStatus(false);
		if(MusicPlayer.provider != null) { MusicPlayer.provider.stop(); }
	},
	toggle: function() {
		if($('#musiconoff .onoffswitch-inner').css("margin-left") == "0px") {
			MusicPlayer.stop();
		} else {
			MusicPlayer.play();
		}
	},
	wireUIEvents: function(elem){
		if($('#music-wrap').size() > 0) {
			elem.on('click', function() {
				MusicPlayer.stop();
				MusicPlayer.play($(this));
			});
		}
		elem.find('.close').on('click',function() { MusicPlayer.removeMedia($(this).parent()); });
	}
};

var YouTubeProvider = {
	player: null,
	
	init: function() {
		$('body').append($('<div id="ytplayer" style="width:400px;height:300px;"></div>'));
    	YouTubeProvider.player = new YT.Player('ytplayer', {
			height: '100',
			width: '200',
			events: {
				'onReady': YouTubeProvider.playerReady,
				'onStateChange': YouTubeProvider.playerStateChange
			}
        });
	},
	
	addMedia: function() { },
	playerReady: function(e) {
		YouTubeProvider.player.setVolume(100);
		MusicPlayer.providerReady();
	},
	playerStateChange: function(e) {
		if(e.data == -1) {  } //video was stopped
		if(e.data == 0) { //video ended
			MusicPlayer.ended(e);
		}
	},
	play: function(elem) {
		var songID = elem.data("songID");
		YouTubeProvider.player.loadVideoById(songID);
	},
	removeMedia: function(elemData) {
		if(elemData.songID == YouTubeProvider.player.getVideoData().video_id) {
			YouTubeProvider.player.stopVideo();
			MusicPlayer.ended(null);
		}
	},
	setVolume: function(value) {
		YouTubeProvider.player.setVolume(value);
	},
	stop: function() {
		YouTubeProvider.player.stopVideo();
	}
};

var GroovesharkProvider = {
	
	addMedia: function(song) {
		if($("#music-wrap").size() > 0) {
			var musicAudioHtml = $("<audio id=\"audio-song-" + song.id + "\" src=\"" + song.url + "\" type=\"audio/wave\"></audio>");
			tweetAudioHtml.on('ended',Player.sound_ended);
			$('#music-wrap').append(tweetAudioHtml);
		}
	}
};

var SpotifyProvider = {
	
};

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

