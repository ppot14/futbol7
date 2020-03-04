
var pollingLimit = 4*24*60*60*1000 + 12*60*60*1000;//4 days and half. Friday at midday or Sunday and middays
var pollingReady = 23*60*60*1000;//Polling ready at 23:00
var hasVoted = false;

function playerPlayedAMatch(player, index){
	var index = index || selectedSeasonMatches.length-1;
	var numRows = selectedSeasonMatches[index].data.length;
	for (i = 0; i < numRows; i++) { 
		if(selectedSeasonMatches[index].data[i].blue == player || selectedSeasonMatches[index].data[i].white == player){
			return true;
		}
	}
	return false;
}

function checkAndEnableVoteButton(){
	//verify if match played and no score and current date<last match date + 5
	if(window.location.pathname.includes('/league')){
//	    numMatches = selectedSeasonMatches.length;

//		console.log('Player '+nameweb+', match: '+(numMatches-1));
//		console.log('Played? '+playerPlayedAMatch(nameweb, numMatches-1));
//		console.log('Valid period to vote? '+(selectedSeasonMatches[numMatches-1].day+pollingLimit>new Date().getTime()));
//		console.log('Polling Ready? '+(selectedSeasonMatches[numMatches-1].day+pollingReady<new Date().getTime()));
		
		if(playerPlayedAMatch(nameweb, numMatches-1) && //Has played
				(selectedSeasonMatches[numMatches-1].day+pollingLimit>new Date().getTime()) && 
				(selectedSeasonMatches[numMatches-1].day+pollingReady<new Date().getTime())){
			//Has scored?
		  	$.post(
					'api/player-has-voted.request', 
					JSON.stringify({name:nameweb,date:selectedSeasonMatches[numMatches-1].day,season:season}), 
					function( data ) {
//						console.log("Played has voted? "+JSON.stringify(data));
//						var t1 = performance.now();
		//				console.log("loggedIn " + (t1 - t0) + "ms");
						if(!data){
							$('#notification-bar').removeClass("alert-info alert-success alert-warning").addClass("alert-warning").html('Puede votar el MVP del último partido ('+longToDate(selectedSeasonMatches[numMatches-1].day)+') hasta '+$.timeago(selectedSeasonMatches[numMatches-1].day+pollingLimit)+'. <a href="#" class="alert-link"  data-toggle="modal" data-target="#polling">CLICK AQUÍ</a>');
							$('#notification-bar').slideDown();
							hasVoted = false;
						}else{
							hasVoted = true;
		  					$('#notification-bar').removeClass("alert-info alert-success alert-warning").addClass("alert-info").text('MVP del último partido ('+longToDate(selectedSeasonMatches[numMatches-1].day)+') disponible '+$.timeago(selectedSeasonMatches[numMatches-1].day+pollingLimit));
		  					$('#notification-bar').slideDown().delay(30000).slideUp();
						}
					}
			);
		}else if(selectedSeasonMatches[numMatches-1].day+pollingLimit<new Date().getTime() &&
				!!mvpsByDate[longToDate(selectedSeasonMatches[numMatches-1].day)] &&
				mvpsByDate[longToDate(selectedSeasonMatches[numMatches-1].day)].length>0){
			$('#notification-bar').removeClass("alert-info alert-success alert-warning").addClass("alert-success").text('MVP del último partido ('+longToDate(selectedSeasonMatches[numMatches-1].day)+'): '+mvpsByDate[longToDate(selectedSeasonMatches[numMatches-1].day)]);
			$('#notification-bar').slideDown();
		}
	}
}

/**
 * POLLING FORM
 */
function createPollingForm(){
	$('.polling-form-group').remove();
	var matchPlayerNames = [];
	$('<div class="form-group polling-form-group row"><div id="blue-team-polling" class="col-md-6"></div><div id="white-team-polling" class="col-md-6"></div></div>').prependTo("#polling-form");
	for(var i=selectedSeasonMatches[numMatches-1].data.length-1; i>=0; i--){	
		matchPlayerNames.push(selectedSeasonMatches[numMatches-1].data[i].blue);
		matchPlayerNames.push(selectedSeasonMatches[numMatches-1].data[i].white);
		
		$('<div class="row">'+
			'<div class="form-check">'+
				'<input class="form-check-input" type="radio" name="playerVote" id="blue-player-vote-'+i+'" value="'+selectedSeasonMatches[numMatches-1].data[i].blue+'">'+
		    '</div>'+
				'<img id="blue-player-picture-'+i+'" class="player-picture rounded" src="'+
					(playersPictures[selectedSeasonMatches[numMatches-1].data[i].blue]?playersPictures[selectedSeasonMatches[numMatches-1].data[i].blue]:'resources/images/unknown-player.jpg')+
					'" onError="this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';"/>'+
				'<i class="fa fa-shield fa-2x text-blue-team blue-team-background team-shield" aria-hidden="true"></i>'+
				'<h4 id="blue-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[numMatches-1].data[i].blue+'</h4>'+
				goalsFormatter(selectedSeasonMatches[numMatches-1].data[i].blue)+
		'</div>').appendTo("#blue-team-polling");
		
		$('<div class="row">'+
			'<div class="form-check">'+
				'<input class="form-check-input" type="radio" name="playerVote" id="white-player-vote-'+i+'" value="'+selectedSeasonMatches[numMatches-1].data[i].white+'">'+
		    '</div>'+
				'<img id="white-player-picture-'+i+'" class="player-picture rounded" src="'+
					(playersPictures[selectedSeasonMatches[numMatches-1].data[i].white]?playersPictures[selectedSeasonMatches[numMatches-1].data[i].white]:'resources/images/unknown-player.jpg')+
					'" onError="this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';"/>'+
				'<i class="fa fa-shield fa-2x text-white-team white-team-background team-shield" aria-hidden="true"></i>'+
				'<h4 id="white-player-name-'+i+'" class="player-name">'+selectedSeasonMatches[numMatches-1].data[i].white+'</h4>'+
		'</div>').prependTo("#white-team-polling");
		
		if(selectedSeasonMatches[numMatches-1].data[i].blue==nameweb){
			$('#blue-player-vote-'+i).prop('disabled', true);
		}
		if(selectedSeasonMatches[numMatches-1].data[i].white==nameweb){
			$('#white-player-vote-'+i).prop('disabled', true);
		}
	}

	$('#polling-form').unbind('submit');
	$('#polling-form').submit(function( event ) {
		event.preventDefault();
		
		$('#polling-form button[type="submit"]').prop("disabled","disabled");
		
		var request = {season:season, 
						date:selectedSeasonMatches[numMatches-1].day, 
						voter:nameweb, 
						voted:$('input[name=playerVote]:checked','#polling-form').val()};
		
		console.log(request);

		$.post(
	  			'api/save-polling.request', 
	  			JSON.stringify(request), 
	  			function( data2 ) {
	  				if(!data2 || !data2.error){
	  					if(typeof ga === "function"){ ga('send', 'event', { eventCategory: 'form', eventAction: 'submit', eventLabel: 'vote'}); }
	  					$('#polling').modal('hide');
	  					$('#notification-bar').slideUp( "fast", function() {
		  					$('#notification-bar').removeClass("alert-info alert-success alert-warning").addClass("alert-success").text('Votación realizada. Resultados definitivos disponibles el '+
		  							longToDateTime(selectedSeasonMatches[numMatches-1].day+pollingLimit));
	  					});
	  					$('#notification-bar').slideDown().delay(30000).slideUp();
	  					hasVoted = true;
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

/**
 * Init
 * 
 * @returns
 */
$(function () {
	
	$('#polling').on('show.bs.modal', function (event) {
		$('.blue-score').text(parseInt(selectedSeasonMatches[numMatches-1].scoreBlues));
		$('.white-score').text(parseInt(selectedSeasonMatches[numMatches-1].scoreWhites));
		$('.last-match-date').text($.timeago(selectedSeasonMatches[numMatches-1].day)+' ('+longToDate(selectedSeasonMatches[numMatches-1].day)+')');
		$('.polling-close-date').text($.timeago(selectedSeasonMatches[numMatches-1].day+pollingLimit)+' ('+longToDateTime(selectedSeasonMatches[numMatches-1].day+pollingLimit)+')');

		createPollingForm();
	});
	$('#polling').on('shown.bs.modal', function (event) {
		updateTeamScorers(selectedSeasonMatches[currentMatch], ".polling-form-group .row");
	});

	listen('LOGIN', function(){ checkAndEnableVoteButton() });
	
});