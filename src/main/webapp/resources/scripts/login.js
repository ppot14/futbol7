/**
 * login.js
 */
var nameweb;
var playersPictures;
var usertype;
var pollingLimit = 4*24*60*60*1000 + 12*60*60*1000;//4 days and half. Friday at midday
var pollingReady = 23*60*60*1000;//Polling ready at 23:00
var matchScores;

function facebookStatusChangeCallback(response) {
	if(response.status && response.status == 'connected'){
		FB.api("/me",
				{fields: "picture,email,name"},
				function(response) {
//					setTimeout(function(){//Hack to avoid season selector load before automatic login
						loggedIn(response,'facebook');
//					},1000);
				});
	}
}

function googleStatusChangeCallback(){
	var response = {name: auth2.currentUser.get().getBasicProfile().getName(),
				id: auth2.currentUser.get().getBasicProfile().getId(),
				picture: auth2.currentUser.get().getBasicProfile().getImageUrl()};
//	setTimeout(function(){//Hack to avoid season selector load before automatic login
		loggedIn(response,'google');
//	},1000);
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
			window.location.pathname+'api/player.request', 
			JSON.stringify(data), 
			function( data ) {
				if(data && data.nameweb){
					
					var waiter = setInterval(waitForMain, 1000);
						
					function waitForMain(){
						
						if(selectedSeasonMatches){
					
		    				nameweb = data.nameweb;
		    				usertype = data.usertype;
		    				showAdmin();
		    				updateUserName();
		    				
		    				
		    				//verify if match played and no score and current date<last match date + 5
		    				console.log("Played? "+JSON.stringify(selectedSeasonMatches[numMatches-1].data).includes(nameweb));
		    				console.log("Valid period to vote? "+(selectedSeasonMatches[numMatches-1].day+pollingLimit>new Date().getTime()));
		    				console.log("Now is after Match? "+(selectedSeasonMatches[numMatches-1].day+pollingReady<new Date().getTime()));
		    				
		    				if(JSON.stringify(selectedSeasonMatches[numMatches-1].data).includes(nameweb) && //Has played
		    						(selectedSeasonMatches[numMatches-1].day+pollingLimit>new Date().getTime()) && //Expired 5 days to vote
		    						(selectedSeasonMatches[numMatches-1].day+pollingReady<new Date().getTime())){//No vote before the match, vote after 23h of the match day
		    					//Has scored?
						      	$.post(
						    			window.location.pathname+'api/player-has-voted.request', 
						    			JSON.stringify({name:nameweb,date:selectedSeasonMatches[numMatches-1].day,season:$("#season-selector").val()}), 
						    			function( data ) {
		//									console.log("Played has voted? "+JSON.stringify(data));
						    				if(!data){
						    					$('#notification-score-button').slideDown();
						    				}else{
							  					$('#notification-bar').text('Resultados del partido del lunes disponibles '+$.timeago(selectedSeasonMatches[numMatches-1].day+pollingLimit));
							  					$('#notification-bar').slideDown().delay(10000).slideUp();
						    				}
						    			}
						    	);
		    				}

							console.log('Finished waiting for selectedSeasonMatches');
		    				clearInterval(waiter);
	    				
						}else{
							console.log('Waiting for selectedSeasonMatches...');
						}
					
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
  			window.location.pathname+'api/match-scorers.request', 
  			JSON.stringify({season: $("#season-selector").val(), match: match}), 
  			function( data1 ) {
  				matchScores=data1;
  				$('.blue-scorers').empty();
  				$('.white-scorers').empty();
  				if(data1){
  					var scoreBlues=0;
  					var scoreWhites=0;
  					for(var i=0; i<match.data.length; i++){
  						if(data1[match.data[i].blue]!=null && 
  							data1[match.data[i].blue]!=0){
  							var scoresB='';
  							for(var j=0; j<data1[match.data[i].blue]; j++){ scoresB+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; scoreBlues++;}
  							scoresB = '<li class="player-goals">'+match.data[i].blue+' '+scoresB+'</li>';
  							$(scoresB).appendTo('.blue-scorers');
  						}
  						if(data1[match.data[i].white]!=null && 
  							data1[match.data[i].white]!=0){
  							var scoresA='';
  							for(var j=0; j<data1[match.data[i].white]; j++){ scoresA+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; scoreWhites++;}
  							scoresA = '<li class="player-goals">'+scoresA+' '+match.data[i].white+'</li>';
  							$(scoresA).appendTo('.white-scorers');
  						}
  					}
					if(scoreBlues<match.scoreBlues){
						var scoresB='';
						for(var j=scoreBlues; j<match.scoreBlues; j++){ scoresB+='<i class="fa fa-futbol-o" aria-hidden="true"></i>';}
						scoresB = '<li class="player-goals">P.P./S.D. '+scoresB+'</li>';
						$(scoresB).appendTo('.blue-scorers');
					}
					if(scoreWhites<match.scoreWhites){
						var scoresA='';
						for(var j=scoreWhites; j<match.scoreWhites; j++){ scoresA+='<i class="fa fa-futbol-o" aria-hidden="true"></i>';}
						scoresA = '<li class="player-goals">'+scoresA+' P.P./S.D.</li>';
						$(scoresA).appendTo('.white-scorers');
					}
  				}else{
					console.warn("Unknown exception updating team scorers: "+data1);
  				}
  			}
  	  );
}

/*
 * LAST MATCH RESULTS
 */
function lastMatchResultRequest(lastMatchResult){

	var request = JSON.stringify({season: $("#season-selector").val(), match: selectedSeasonMatches[lastMatchResult]});
	$.post(
			window.location.pathname+'api/last-match-result.request', 
			request, 
			function( data1 ) {
				if(data1 && Array.isArray(data1)){
					$('.punctuation-row').remove();
					for(var i=0; i<data1.length; i++){
						addPlayerToResult(i, data1);
						$('[data-toggle="tooltip"]').tooltip();
					}
				}else{
					$('.punctuation-row').remove();
					console.warn('There is no match results for the last match: '+request);
				}
			}
	);
}

function addBadges(x, data, pValueAvg, max, min){
	var html = '';
	var dataX = data[x].value;
	if(pValueAvg==max){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al MVP"><i class="fa fa-trophy" aria-hidden="true"></i></div>';
	}else if(pValueAvg==min){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al Cabra"><i class="fa fa-thumbs-down" aria-hidden="true"></i></div>';
	}
	if(dataX.trompito){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más chupón"><i class="fa fa-undo" aria-hidden="true"></i></div>';
	}
	if(dataX.dandy){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al jugador con más clase y elegante"><i class="fa fa-glass" aria-hidden="true"></i></div>';
	}
	if(dataX.frances){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más guarro"><i class="fa fa-wheelchair-alt" aria-hidden="true"></i></div>';
	}
	if(dataX.sillegas){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más impuntual e impresentable"><i class="fa fa-clock-o" aria-hidden="true"></i></div>';
	}
	if(dataX.porculero){
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más protestón y bocazas"><i class="fa fa-bullhorn" aria-hidden="true"></i></div>';
	}
	if(matchScores[data[x].key]>2){//killer
		html += '<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al Killer (3 o más goles)"><i class="fa fa-futbol-o" aria-hidden="true"></i></div>';
	}
	return html;
}

function addPlayerToResult(x, data){
	var l = data.length;
	var pValue = data[x].value;
	var max = data[0].value.avg;
	var min = data[l-1].value.avg;
	var id = Date.now();
	var row = $('<div class="row punctuation-row"><div class="row-wrap col-xs-12 col-sm-12 col-md-12 col-lg-12">'+
					'<div class="panel panel-default '+(pValue.avg==max?'panel-success':pValue.avg==min?'panel-danger':nameweb==data[x].key?'panel-info':'')+'">'+
						'<div class="panel-body '+(pValue.avg==max?'bg-success':pValue.avg==min?'bg-danger':nameweb==data[x].key?'bg-info':'')+'">'+
				'</div></div></div></div>').appendTo($('#last-match-winner .modal-body'));
	var scores = '';
	if(pValue.scores){
		for(var i=0; i<pValue.scores; i++){ scores+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; }
		scores = '<span class="player-goals">Goles: '+scores+'</span>';
	}
	$('<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">'+
		'<h3 class="player-name">'+data[x].key+'</h3>'+
		'<img class="player-picture img-rounded" alt="'+data[x].key+' picture" src="'+((pValue.image)?pValue.image:'resources/images/unknown-player.jpg')+'"/>'+
		'<span class="player-score pull-right">'+pValue.avg.toFixed(2)+'</span>'+
		'<div class="badges pull-right">'+addBadges(x, data, pValue.avg, max, min)+'</div>'+
	'</div>').appendTo(row.find('.panel-body'));
	
	if(pValue.punctuations){
	//	Acordeon
		$('<div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">'+
			'<div class="panel-group" id="accordion-'+id+'" role="tablist" aria-multiselectable="true">'+
			'</div>'+
		'</div>').appendTo(row.find('.panel-body'));	
	
		pValue.punctuations.sort(function(a, b) {
		    return parseFloat(b.score) - parseFloat(a.score);//Descending
		});
		
		for(var j=0; j<pValue.punctuations.length; j++){
	//		Acordeon
			$('<div class="panel panel-default">'+
				  '<div class="panel-heading" role="tab" id="heading-'+id+'-'+j+'">'+
				      '<h4 class="panel-title">'+
				        '<a role="button" data-toggle="collapse" data-parent="#accordion-'+id+'" href="#collapse-'+id+'-'+j+'" aria-expanded="true" aria-controls="collapse-'+id+'-'+j+'">'+
				        	pValue.punctuations[j].voter+' <span class="voter-score pull-right">'+pValue.punctuations[j].score+'</span></a>'+
				       '</h4>'+
				   '</div>'+
				   '<div id="collapse-'+id+'-'+j+'" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading-'+id+'-'+j+'">'+
				        '<div class="panel-body">'+pValue.punctuations[j].comment+'</div>'+
				   '</div>'+
		     '</div>').appendTo(row.find('#accordion-'+id));
		}
	}else{
		$('<div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">'+
			'<p>Media de Fubles: <b>'+pValue.avgFubles.toFixed(2)+'</b></p>'+
			'<a class="" href="'+pValue.linkFubles+'" target="_blank">Más datos en el partido de Fubles</a>'+
		'</div>').appendTo(row.find('.panel-body'));
	}
}


/*
 * POLLING FORM
 */
function createPollingForm(){
	$('.polling-form-group').remove();
	var matchPlayerNames = [];
	for(var i=selectedSeasonMatches[numMatches-1].data.length-1; i>=0; i--){	
		matchPlayerNames.push(selectedSeasonMatches[numMatches-1].data[i].blue);
		matchPlayerNames.push(selectedSeasonMatches[numMatches-1].data[i].white);
		$('<div class="form-group polling-form-group"><div class=row>'+
			'<div class="col-md-6"><div class=row style="margin-bottom: 15px">'+
				'<div class="col-md-4">'+
					'<img id="blue-player-picture-'+i+'" alt="'+selectedSeasonMatches[numMatches-1].data[i].blue+' picture" class="player-picture img-rounded" style="height: 100px;width: 100px;" src="'+
					(playersPictures[selectedSeasonMatches[numMatches-1].data[i].blue]?playersPictures[selectedSeasonMatches[numMatches-1].data[i].blue]:'resources/images/unknown-player.jpg')+'"/>'+
					'<i class="fa fa-flag fa-2x text-primary flag-blue" aria-hidden="true"></i>'+
			    '</div>'+
				'<div class="col-md-8">'+
					'<h4 id="blue-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[numMatches-1].data[i].blue+'</h4>'+
					'<input type="text" name="blue-player-punctuation-'+i+'" '+
					'data-provide="slider" '+
					'data-slider-id="slider-blue-'+i+'" '+
					'data-slider-min="1"'+
					'data-slider-max="10"'+
					'data-slider-step="0.5"'+
					'data-slider-value="5"'+
					'data-slider-tooltip="hide" ><span id="blue-player-punctuation-number-'+i+'" class="player-punctuation-number pull-right"></span>'+
					'<textarea name="blue-player-comment-'+i+'" class="form-control" rows="3" maxlength="1000" placeholder="Comentario"  '+((selectedSeasonMatches[numMatches-1].data[i].blue==nameweb)?'disabled':'')+'></textarea>'+
			    '</div>'+
			'</div></div>'+
			'<div class="col-md-6"><div class=row>'+
				'<div class="col-md-4">'+
					'<img id="white-player-picture-'+i+'" alt="'+selectedSeasonMatches[numMatches-1].data[i].white+' picture" class="player-picture img-rounded" style="height: 100px;width: 100px;" src="'+
					(playersPictures[selectedSeasonMatches[numMatches-1].data[i].white]?playersPictures[selectedSeasonMatches[numMatches-1].data[i].white]:'resources/images/unknown-player.jpg')+'"/>'+
					'<i class="fa fa-flag-o fa-2x flag-white" aria-hidden="true"></i>'+
			    '</div>'+
				'<div class="col-md-8">'+
					'<h4 id="white-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[numMatches-1].data[i].white+'</h4>'+
					'<input type="text" name="white-player-punctuation-'+i+'" '+
					'data-provide="slider" '+
					'data-slider-id="slider-white-'+i+'" '+
					'data-slider-min="1"'+
					'data-slider-max="10"'+
					'data-slider-step="0.5"'+
					'data-slider-value="5"'+
					'data-slider-tooltip="hide"><span id="white-player-punctuation-number-'+i+'" class="player-punctuation-number pull-right"></span>'+
					'<textarea name="white-player-comment-'+i+'" class="form-control" rows="3" maxlength="1000" placeholder="Comentario"  '+((selectedSeasonMatches[numMatches-1].data[i].white==nameweb)?'disabled':'')+'></textarea>'+
			    '</div>'+
			'</div></div>'+
		'</div></div>').prependTo("#polling-form");
		var mySliderB = $('input[name="blue-player-punctuation-'+i+'"]').slider();
		var mySliderW = $('input[name="white-player-punctuation-'+i+'"]').slider();
		$('#blue-player-punctuation-number-'+i).text(mySliderB.val());
		$('#white-player-punctuation-number-'+i).text(mySliderW.val());
		mySliderB.on("change", function(slideEvt) {
			$('#'+slideEvt.target.name.replace("punctuation-","punctuation-number-")).text(slideEvt.value.newValue);
		});
		mySliderW.on("change", function(slideEvt) {
			$('#'+slideEvt.target.name.replace("punctuation-","punctuation-number-")).text(slideEvt.value.newValue);
		});
		if(selectedSeasonMatches[numMatches-1].data[i].blue==nameweb){
			mySliderB.slider("disable");
			$('#blue-player-punctuation-number-'+i).hide();
		}
		if(selectedSeasonMatches[numMatches-1].data[i].white==nameweb){
			mySliderW.slider("disable");
			$('#white-player-punctuation-number-'+i).hide();
		}
	}
	$('<div class="form-group polling-form-group"><div id="title-selector" class="row">'+
			'<div class="col-md-1"></div>'+
			'<div class="col-md-2">'+
				'<h4 data-toggle="tooltip" data-placement="top" title="Título al más chupón">El Trompito <div class="player-badge" ><i class="fa fa-undo" aria-hidden="true"></i></div></h4>'+
				'<select id="trompito-selector" class="matchtitle-selector form-control"></select>'+
			'</div>'+
			'<div class="col-md-2">'+
				'<h4 data-toggle="tooltip" data-placement="top" title="Título al jugador con más clase y elegante">El Dandy <div class="player-badge"><i class="fa fa-glass" aria-hidden="true"></i></div></h4>'+
				'<select id="dandy-selector" class="matchtitle-selector form-control"></select>'+
			'</div>'+
			'<div class="col-md-2">'+
				'<h4 data-toggle="tooltip" data-placement="top" title="Título al más guarro">El Francés <div class="player-badge"><i class="fa fa-wheelchair-alt" aria-hidden="true"></i></div></h4>'+
				'<select id="frances-selector" class="matchtitle-selector form-control"></select>'+
			'</div>'+
			'<div class="col-md-2">'+
				'<h4 data-toggle="tooltip" data-placement="top" title="Título al más impuntual e impresentable">El Sillegas <div class="player-badge"><i class="fa fa-clock-o" aria-hidden="true"></i></div></h4>'+
				'<select id="sillegas-selector" class="matchtitle-selector form-control"></select>'+
			'</div>'+
			'<div class="col-md-2">'+
				'<h4 data-toggle="tooltip" data-placement="top" title="Título al más protestón y bocazas">El Porculero <div class="player-badge"><i class="fa fa-bullhorn" aria-hidden="true"></i></div></h4>'+
				'<select id="porculero-selector" class="matchtitle-selector form-control"></select>'+
			'</div>'+
			'<div class="col-md-1"></div>'+
	'</div></div>').prependTo("#polling-form");
	var select = $("select.matchtitle-selector");
	matchPlayerNames.sort(function (a, b) {return a.localeCompare(b);});
	matchPlayerNames.reverse();
	for (var i in matchPlayerNames) {
		if(matchPlayerNames[i]!=nameweb){ 
			select.prepend($('<option/>').val(matchPlayerNames[i]).text(matchPlayerNames[i]));
		}
	}
	select.prepend($('<option selected/>').val("").text("Seleccione uno"));
	$('[data-toggle="tooltip"]').tooltip();

	$('#polling-form').unbind('submit');
	$('#polling-form').submit(function( event ) {
		event.preventDefault();
		
		var request = {season:$("#season-selector").val(), 
						date:selectedSeasonMatches[numMatches-1].day, 
						scores:[],
						trompito: $('#trompito-selector').val(),
						dandy: $('#dandy-selector').val(),
						frances: $('#frances-selector').val(),
						sillegas: $('#sillegas-selector').val(),
						porculero: $('#porculero-selector').val()};
		
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

		$.post(
	  			window.location.pathname+'api/save-polling.request', 
	  			JSON.stringify(request), 
	  			function( data2 ) {
	  				if(!data2 || !data2.error){
	  					ga('send', 'event', { eventCategory: 'form', eventAction: 'submit', eventLabel: 'vote'});
	  					$('#polling-form button[type="submit"]').prop("disabled","disabled");
	  					$('#notification-score-button').slideUp();
	  					$('#polling').modal('hide');
	  					$('#notification-bar').text('Votación realizada. Resultados disponibles el '+
	  							new Date(selectedSeasonMatches[numMatches-1].day+pollingLimit).toLocaleString('es-ES',{ day:'numeric',weekday: 'long', hour:'numeric',minute:'numeric' }));
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
		$('.blue-score').text(parseInt(selectedSeasonMatches[numMatches-1].scoreBlues));
		$('.white-score').text(parseInt(selectedSeasonMatches[numMatches-1].scoreWhites));
		$('.last-match-date').text($.timeago(selectedSeasonMatches[numMatches-1].day));
		$('.polling-close-date').text($.timeago(selectedSeasonMatches[numMatches-1].day+pollingLimit));
		
		updateListTeamScorers(selectedSeasonMatches[numMatches-1]);
		
		createPollingForm();
	});
	
	//Last Match Winner Overlay
	$('#last-match-winner').on('show.bs.modal', function (event) {
		var matchDay = $(event.relatedTarget).attr('data-match-day');
		var id = $(event.relatedTarget).attr('id');
		var lastMatchResult;
		if("match-winner-button"==id){
			  if( currentMatch==matchDay && selectedSeasonMatches[currentMatch].day+pollingLimit>new Date().getTime() && usertype != 'admin'){
				  $("#match-winner-button").addClass("disabled");
				  return;
			  }else{
				  lastMatchResult = matchDay;
			  }
		}else if("last-match-winner-button"==id){
			  if( selectedSeasonMatches[numMatches-1].day+pollingLimit<new Date().getTime() || usertype == 'admin'){
				  lastMatchResult = numMatches-1;
			  }else{
				  lastMatchResult = numMatches-2;
			  }
		}

		  $('.blue-score').text(parseInt(selectedSeasonMatches[lastMatchResult].scoreBlues));
		  $('.white-score').text(parseInt(selectedSeasonMatches[lastMatchResult].scoreWhites));
		  $('.last-match-date').text($.timeago(selectedSeasonMatches[lastMatchResult].day));
		  
		  updateListTeamScorers(selectedSeasonMatches[lastMatchResult]);
		  
		  lastMatchResultRequest(lastMatchResult);
			  
	});
});