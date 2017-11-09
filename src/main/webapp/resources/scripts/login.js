var nameweb;
var playersPictures;
var usertype;
var pollingLimit = 4*24*60*60*1000+12*60*60*1000;//4 days and half. Friday at midday
var pollingReady = 23*60*60*1000;//Polling ready at 23:00

function facebookStatusChangeCallback(response) {
	if(response.status && response.status == 'connected'){
		FB.api("/me",
				{fields: "picture,email,name"},
				function(response) {
					loggedIn(response,'facebook');
				});
	}
}

function googleStatusChangeCallback(){
	var response = {name: auth2.currentUser.get().getBasicProfile().getName(),
				id: auth2.currentUser.get().getBasicProfile().getId(),
				picture: auth2.currentUser.get().getBasicProfile().getImageUrl()};
	loggedIn(response,'google');
}

function loggedOut(){
  	$('#notification-score-button').slideUp();
  	$('#logout-button').slideUp(function (){ $('#login-button').slideDown() });
  	$('#player-name').slideUp();
  	$('#player-picture').slideUp();
  	$( "td:contains('"+nameweb+"')" ).css( "font-weight", "normal" );
	$( "tr:contains('"+nameweb+"')" ).removeClass( "info" );
	hideAdmin();
	nameweb = null;
	usertype = null;
}

function updateUserName(){
	if(nameweb){
		$( "td:contains('"+nameweb+"')" ).css( "font-weight", "bold" );
		$( "tr:contains('"+nameweb+"')" ).addClass( "info" );
	}
}

function loggedIn(response,loginType) {
    if (response && !response.error) {
    	
    	var data = {};
		if(loginType=='facebook'){
			data.facebook = response;
			data.id = response.id;
			data.name = response.name;
			data.picture = response.picture.data.url;
		}else if(loginType=='google'){
			data.google = response;
			data.id = response.id;
			data.name = response.name;
			data.picture = response.picture;
		}
		
  	  $('#player-name').text(data.name);
  	  $('#player-picture').attr("src",data.picture);
  	  $('#login-button').slideUp(function() {$('#logout-button').slideDown();});
  	  $('#player-name').slideDown();
  	  $('#player-picture').slideDown();
	  $('#login-selector').modal('hide');
  	  
	  $.post(
			window.location.pathname+'api/player.json', 
			JSON.stringify(data), 
			function( data ) {
				if(data && data.nameweb){
					
    				nameweb = data.nameweb;
    				usertype = data.usertype;
    				showAdmin();
    				updateUserName();
    				
    				//verify if match played and no score and current date<last match date + 5
    				console.log("Played? "+JSON.stringify(selectedSeasonMatches[currentMatch].data).includes(nameweb));
    				console.log("Valid period to vote? "+(selectedSeasonMatches[currentMatch].day+pollingLimit>new Date().getTime()));
    				console.log("Now is after Match? "+(selectedSeasonMatches[currentMatch].day+pollingReady<new Date().getTime()));
    				
    				if(JSON.stringify(selectedSeasonMatches[currentMatch].data).includes(nameweb) && //Has played
    						(selectedSeasonMatches[currentMatch].day+pollingLimit>new Date().getTime()) && //Expired 5 days to vote
    						(selectedSeasonMatches[currentMatch].day+pollingReady<new Date().getTime())){//No vote before the match, vote after 23h of the match day
    					//Has scored?
				      	$.post(
				    			window.location.pathname+'api/player-has-voted.json', 
				    			JSON.stringify({name:nameweb,date:selectedSeasonMatches[currentMatch].day,season:$("#season-selector").val()}), 
				    			function( data ) {
//									    				console.log("Played has voted? "+JSON.stringify(data));
				    				if(!data){
				    					$('#notification-score-button').slideDown();
				    				}else{
					  					$('#notification-bar').text('Resultados del partido del lunes disponibles '+$.timeago(selectedSeasonMatches[currentMatch].day+pollingLimit));
					  					$('#notification-bar').slideDown().delay(10000).slideUp();
				    				}
				    			}
				    	);
    				}
				}else if(data && data.newuser){
					console.info("New user created: "+data.newuser);
				}else{
					console.warn("Unknown user: "+JSON.stringify(data));
				}
			}
	    );
    }
}

function updateListTeamScorers(match){
	$.post(
  			window.location.pathname+'api/match-scorers.json', 
  			JSON.stringify({season: $("#season-selector").val(), match: match}), 
  			function( data1 ) {
  				$('.blue-scorers').empty();
  				$('.white-scorers').empty();
  				if(data1){
  					for(var i=0; i<match.data.length; i++){
  						if(data1[match.data[i].blue]!=null && 
  							data1[match.data[i].blue]!=0){
  							var scoresB='';
  							for(var j=0; j<data1[match.data[i].blue]; j++){ scoresB+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; }
  							scoresB = '<li class="player-goals">'+match.data[i].blue+' '+scoresB+'</li>';
  							$(scoresB).appendTo('.blue-scorers');
  						}
  						if(data1[match.data[i].white]!=null && 
  							data1[match.data[i].white]!=0){
  							var scoresA='';
  							for(var j=0; j<data1[match.data[i].white]; j++){ scoresA+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; }
  							scoresA = '<li class="player-goals">'+scoresA+' '+match.data[i].white+'</li>';
  							$(scoresA).appendTo('.white-scorers');
  							
  						}
  					}
  				}else{
					console.warn("Unknown exception updating team scorers: "+data1);
  				}
  			}
  	  );
}

function addPlayerToResult(x, t, data){
	var id = Date.now();
	var row = $('<div class="row punctuation-row"><div class="row-wrap col-xs-12 col-sm-12 col-md-12 col-lg-12"><div class="panel panel-default '+(x==0?'panel-success':x==t-1?'panel-danger':nameweb==data.key?'panel-info':'')+'"><div class="panel-body '+(x==0?'bg-success':x==t-1?'bg-danger':nameweb==data.key?'bg-info':'')+'"></div></div></div></div>').appendTo($('#last-match-winner .modal-body'));
	var scores = '';
	if(data.value.scores){
		for(var i=0; i<data.value.scores; i++){ scores+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; }
		scores = '<span class="player-goals">Goles: '+scores+'</span>';
	}
	$('<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">'+
		'<h3 class="player-name">'+data.key+'</h3>'+
		'<img class="player-picture img-rounded" src="'+((data.value.image)?data.value.image:'resources/images/unknown-player.jpg')+'"/>'+
		'<span class="player-score pull-right">'+data.value.avg+'</span>'+
		'<div class="badges pull-right">'+
			(x==0?
			'<div class="player-badge"><i class="fa fa-trophy" aria-hidden="true"></i></div>':
			x==t-1?
			'<div class="player-badge"><i class="fa fa-undo" aria-hidden="true"></i></div>':
			'')+
		'</div>'+
	'</div>').appendTo(row.find('.panel-body'));
	
//	Carousel
//	$('<div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">'+
//		'<div id="carousel-comments-scores-'+id+'" class="carousel slide" data-ride="carousel" data-interval="600000">'+
//		  '<ol class="carousel-indicators"></ol>'+
//		  '<div class="carousel-inner" role="listbox"></div>'+
//		  '<a class="left carousel-control" href="#carousel-comments-scores-'+id+'" role="button" data-slide="prev">'+
//		    '<span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>'+
//		    '<span class="sr-only">Previous</span>'+
//		  '</a>'+
//		  '<a class="right carousel-control" href="#carousel-comments-scores-'+id+'" role="button" data-slide="next">'+
//		    '<span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>'+
//		    '<span class="sr-only">Next</span>'+
//		  '</a>'+
//		'</div>'+
//	'</div>').appendTo(row.find('.panel-body'));

	
	if(data.value.punctuations){
	//	Acordeon
		$('<div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">'+
			'<div class="panel-group" id="accordion-'+id+'" role="tablist" aria-multiselectable="true">'+
			'</div>'+
		'</div>').appendTo(row.find('.panel-body'));	
	
		data.value.punctuations.sort(function(a, b) {
		    return parseFloat(b.score) - parseFloat(a.score);//Descending
		});
		
		for(var j=0; j<data.value.punctuations.length; j++){
	//		Carousel
	//		$('<li data-target="#carousel-comments-scores-'+id+'" data-slide-to="'+j+'" class="'+(j==0?'active':'')+'"></li>').appendTo(row.find('.carousel-indicators'));
	//		$('<div class="item '+(j==0?'active':'')+'">'+
	//	    	'<h5 class="voter-name">'+data.value.punctuations[j].voter+' <span class="voter-score pull-right">'+data.value.punctuations[j].score+'</span></h5>'+
	//	    	'<div class="voter-comment">'+data.value.punctuations[j].comment+'</div>'+
	//	    '</div>').appendTo(row.find('.carousel-inner'));
			
	//		Acordeon
			$('<div class="panel panel-default">'+
				  '<div class="panel-heading" role="tab" id="heading-'+id+'-'+j+'">'+
				      '<h4 class="panel-title">'+
				        '<a role="button" data-toggle="collapse" data-parent="#accordion-'+id+'" href="#collapse-'+id+'-'+j+'" aria-expanded="true" aria-controls="collapse-'+id+'-'+j+'">'+
				        	data.value.punctuations[j].voter+' <span class="voter-score pull-right">'+data.value.punctuations[j].score+'</span></a>'+
				       '</h4>'+
				   '</div>'+
				   '<div id="collapse-'+id+'-'+j+'" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-'+id+'-'+j+'">'+
				        '<div class="panel-body">'+data.value.punctuations[j].comment+'</div>'+
				   '</div>'+
		     '</div>').appendTo(row.find('#accordion-'+id));
		}
	}else{
		$('<div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">'+
			'<p>Media de Fubles: <b>'+data.value.avgFubles+'</b></p>'+
			'<a class="" href="'+data.value.linkFubles+'">Más datos en el partido de Fubles</a>'+
		'</div>').appendTo(row.find('.panel-body'));
	}
}


function createPollingForm(){
	$('.polling-form-group').remove();
	for(var i=selectedSeasonMatches[currentMatch].data.length-1; i>=0; i--){	
		$('<div class="form-group polling-form-group"><div class=row>'+
			'<div class="col-md-6"><div class=row style="margin-bottom: 15px">'+
				'<div class="col-md-4">'+
					'<img id="blue-player-picture-'+i+'" class="player-picture img-rounded" style="height: 100px;width: 100px;" src="'+
					(playersPictures[selectedSeasonMatches[currentMatch].data[i].blue]?playersPictures[selectedSeasonMatches[currentMatch].data[i].blue]:'resources/images/logo50.png')+'"/>'+
					'<i class="fa fa-flag fa-2x text-primary flag-blue" aria-hidden="true"></i>'+
			    '</div>'+
				'<div class="col-md-8">'+
					'<h4 id="blue-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[currentMatch].data[i].blue+'</h4>'+
					'<input type="text" name="blue-player-punctuation-'+i+'" '+
					'data-provide="slider" '+
					'data-slider-id="slider-blue-'+i+'" '+
					'data-slider-min="1"'+
					'data-slider-max="10"'+
					'data-slider-step="1"'+
					'data-slider-value="5"'+
					'data-slider-tooltip="hide" ><span id="blue-player-punctuation-number-'+i+'" class="player-punctuation-number pull-right"></span>'+
					'<textarea name="blue-player-comment-'+i+'" class="form-control" rows="3" placeholder="Comentario"  '+((selectedSeasonMatches[currentMatch].data[i].blue==nameweb)?'disabled':'')+'></textarea>'+
			    '</div>'+
			'</div></div>'+
			'<div class="col-md-6"><div class=row>'+
				'<div class="col-md-4">'+
					'<img id="white-player-picture-'+i+'" class="player-picture img-rounded" style="height: 100px;width: 100px;" src="'+
					(playersPictures[selectedSeasonMatches[currentMatch].data[i].white]?playersPictures[selectedSeasonMatches[currentMatch].data[i].white]:'resources/images/unknown-player.jpg')+'"/>'+
					'<i class="fa fa-flag-o fa-2x flag-white" aria-hidden="true"></i>'+
			    '</div>'+
				'<div class="col-md-8">'+
					'<h4 id="white-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[currentMatch].data[i].white+'</h4>'+
					'<input type="text" name="white-player-punctuation-'+i+'" '+
					'data-provide="slider" '+
					'data-slider-id="slider-white-'+i+'" '+
					'data-slider-min="1"'+
					'data-slider-max="10"'+
					'data-slider-step="1"'+
					'data-slider-value="5"'+
					'data-slider-tooltip="hide"><span id="white-player-punctuation-number-'+i+'" class="player-punctuation-number pull-right"></span>'+
					'<textarea name="white-player-comment-'+i+'" class="form-control" rows="3" placeholder="Comentario"  '+((selectedSeasonMatches[currentMatch].data[i].white==nameweb)?'disabled':'')+'></textarea>'+
			    '</div>'+
			'</div></div>'+
		'</div></div>').prependTo("#polling-form");
		var mySliderB = $('input[name="blue-player-punctuation-'+i+'"]').slider();
		var mySliderW = $('input[name="white-player-punctuation-'+i+'"]').slider();
		$('#blue-player-punctuation-number-'+i).text(mySliderB.val());
		$('#white-player-punctuation-number-'+i).text(mySliderW.val());
		mySliderB.on("slideStop", function(slideEvt) {
			$('#'+slideEvt.target.name.replace("punctuation-","punctuation-number-")).text(slideEvt.value);
		});
		mySliderW.on("slideStop", function(slideEvt) {
			$('#'+slideEvt.target.name.replace("punctuation-","punctuation-number-")).text(slideEvt.value);
		});
		if(selectedSeasonMatches[currentMatch].data[i].blue==nameweb){
			mySliderB.slider("disable");
			$('#blue-player-punctuation-number-'+i).hide();
		}
		if(selectedSeasonMatches[currentMatch].data[i].white==nameweb){
			mySliderW.slider("disable");
			$('#white-player-punctuation-number-'+i).hide();
		}
	}

	$('#polling-form').unbind('submit');
	$('#polling-form').submit(function( event ) {
		event.preventDefault();
		
		var request = {season:$("#season-selector").val(), date:selectedSeasonMatches[currentMatch].day, scores:[]};
		
		for(var i=0; i<7; i++){
			if($('#blue-player-name-'+i).text()!=nameweb){
				request.scores.push({voter:nameweb, 
									voted:$('#blue-player-name-'+i).text(), 
									score: $('input[name="blue-player-punctuation-'+i+'"]').val(), 
									comment: $('textarea[name="blue-player-comment-'+i+'"]').val()});
			}
			if($('#white-player-name-'+i).text()!=nameweb){
				request.scores.push({voter:nameweb, 
									voted:$('#white-player-name-'+i).text(), 
									score: $('input[name="white-player-punctuation-'+i+'"]').val(), 
									comment: $('textarea[name="white-player-comment-'+i+'"]').val()});
			}
		}		
//		console.log(request);

		$.post(
	  			window.location.pathname+'api/save-polling.json', 
	  			JSON.stringify(request), 
	  			function( data2 ) {
//	  				console.log(data2);
	  				if(!data2 || !data2.error){
	  					$('#polling-form button[type="submit"]').prop("disabled","disabled");
	  					$('#notification-score-button').slideUp();
	  					$('#polling').modal('hide');
	  					$('#notification-bar').text('Votación realizada. Resultados disponibles '+$.timeago(selectedSeasonMatches[currentMatch].day+pollingLimit));
	  					$('#notification-bar').slideDown().delay(10000).slideUp();
	  				}else{
	  					if(data2.error){
	  						console.error(data2.error);
	  					}else{
	  						console.warn("Unknown exception saving polling: "+data2);
	  					}
	  				}
	  			}
	  	);
	});
	
}

$(function () {
	window.fbAsyncInit = function() {
	  FB.init({
        appId      : '1606586212720990',
        status	   : true,
        xfbml      : true,
        version    : 'v2.10',
        cookie 	   : true
      });
      FB.AppEvents.logPageView(); 
      FB.getLoginStatus(function(response) {
    	  	facebookStatusChangeCallback(response);
      });
  };

  (function(d, s, id){
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "//connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));
  
  gapi.load('auth2', function(){
      // Retrieve the singleton for the GoogleAuth library and set up the client.
      auth2 = gapi.auth2.init({
        client_id: '517911210517-uupk9pfrbcsnce7qblohvpmog4hc6bfu.apps.googleusercontent.com',
        cookiepolicy: 'single_host_origin',
        // Request scopes in addition to 'profile' and 'email'
        //scope: 'additional_scope'
      });
      if(auth2.isSignedIn.get()){
		googleStatusChangeCallback();
      }
    });
  
	$('#facebook-login-button').click(function(){
		FB.login(function(response){
			facebookStatusChangeCallback(response);
		});
	});
	$('#google-login-button').click(function(){
		auth2.signIn().then(function () {
			googleStatusChangeCallback();
	    });
	});
	

	$('#logout-button, #player-name').click(function(event){
		event.preventDefault();
		try{
			FB.logout(function(response) {
			  if (response.status !== 'connected') {
				  loggedOut();
			  }
			});
		}catch (e) {
			console.info('Facebook logout ignored: '+e.message);
		}
		try{
			auth2.signOut().then(function () {
				loggedOut();
		    });
		}catch (e) {
			console.info('Google logout ignored: '+e.message);
		}
	});
	
	//Polling Overlay
	$('#polling').on('show.bs.modal', function (event) {
//		console.log('currentMatch: '+JSON.stringify(selectedSeasonMatches[currentMatch]));
		$('.blue-score').text(parseInt(selectedSeasonMatches[currentMatch].scoreBlues));
		$('.white-score').text(parseInt(selectedSeasonMatches[currentMatch].scoreWhites));
		$('.last-match-date').text($.timeago(selectedSeasonMatches[currentMatch].day));
		$('.polling-close-date').text($.timeago(selectedSeasonMatches[currentMatch].day+pollingLimit));
		
		updateListTeamScorers(selectedSeasonMatches[currentMatch]);
		
		createPollingForm();
	});
	
	//Last Match Winner Overlay
	$('#last-match-winner').on('show.bs.modal', function (event) {
		  var lastMatchResult = currentMatch-1;
		  if( selectedSeasonMatches[currentMatch].day+pollingLimit<new Date().getTime() ){
			  lastMatchResult = currentMatch;
		  }

		  $('.blue-score').text(parseInt(selectedSeasonMatches[lastMatchResult].scoreBlues));
		  $('.white-score').text(parseInt(selectedSeasonMatches[lastMatchResult].scoreWhites));
		  $('.last-match-date').text($.timeago(selectedSeasonMatches[lastMatchResult].day));
		  
		  updateListTeamScorers(selectedSeasonMatches[lastMatchResult]);
		  
		  var request = JSON.stringify({season: $("#season-selector").val(), match: selectedSeasonMatches[lastMatchResult]});
		  $.post(
	  			window.location.pathname+'api/last-match-result.json', 
	  			request, 
	  			function( data1 ) {
	  				console.log('last-match-result.json: '+JSON.stringify(data1));
	  				if(data1 && Array.isArray(data1)){
	  					$('.punctuation-row').remove();
	  					for(var i=0; i<data1.length; i++){
	  						addPlayerToResult(i, data1.length, data1[i]);
	  					}
	  				}else{
	  					console.warn('There is no match results for the last match: '+request);
	  				}
	  			}
	  	  );
			  
	});
});