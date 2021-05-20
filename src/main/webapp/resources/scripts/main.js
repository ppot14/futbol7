/**
 * main.js
 */

/*
 * Variables and Constants
 */
var options;
var season, player;
var selectedSeasonMatches, numMatches, currentMatch;
var chart, scoresPointsChart;
var nameweb;

/*
 * Static Utility Functions
 */
function ID() {
	  return Math.random().toString(36).substr(2, 8);
}
function parseDate(s){
	var parts = s.split("/");
	return new Date(parseInt(parts[2], 10),
	                  parseInt(parts[1], 10) - 1,
	                  parseInt(parts[0], 10));
}
function parseDateToLong(s){
	return parseDate(s).getTime();
}
function longToDateTime(l){
	return new Date(l).toLocaleString('es-ES',{ day:'numeric',weekday: 'long', hour:'numeric',minute:'numeric' });
}
function longToDate(l){
	return new Date(l).toLocaleDateString();
}
function getParameterByName(name) {
      name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
      var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
      return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
  
function percentajeString(value, total) {
	return total>0?(Math.floor((value / total) * 100)+'%'):'';
}
function decimalFormatter(value, row) {
	return value!=""?parseFloat(Math.round(value * 100) / 100).toFixed(2):'-';
}
	  
function lastMatchesFormat(value) {
	var ret = '';
	var len = value.length;
	var numMatches = 5;
	for (var i = len-numMatches; i < len; i++) {
        if(value[i]=='w') ret = ret + '<span class="badge badge-success badge-pill badge-lastmatch">V</span>';
        if(value[i]=='d') ret = ret + '<span class="badge badge-warning badge-pill badge-lastmatch">E</span>';
        if(value[i]=='l') ret = ret + '<span class="badge badge-danger badge-pill badge-lastmatch">D</span>';
        if(value[i]=='-') ret = ret + '<span class="badge badge-light badge-pill badge-lastmatch">-</span>';
	}
	return ret;
}
function trendingFormat(value) {
	var sum = 0;
	var len = value.length;
	var numMatches = 5;
	for (var i = len-numMatches; i < len; i++) {
        if(value[i]=='w') sum += 3; 
        if(value[i]=='d') sum += 1;
	}
	if(sum>=13){ return '<i class="trend-arrow fas fa-angle-double-up" style="color:Turquoise;"></i>'; }
	else if(sum>=10){ return '<i class="trend-arrow fas fa-angle-up" style="color:YellowGreen;"></i>'; }
	else if(sum>=7){ return '<i class="trend-arrow fas fa-equals" style="color:Gold;"></i>'; }
	else if(sum>=4){ return '<i class="trend-arrow fas fa-angle-down" style="color:DarkOrange;"></i>'; }
	else{ return '<i class="trend-arrow fas fa-angle-double-down" style="color:DarkRed;"></i>'; }
}
function teamFormatter(value) {
	var ret = '-';
	if(value=='blue'){
		ret = '<div class="team-bullet blue-team-background"><i class="fa fa-shield text-blue-team" aria-hidden="true"></i></div>';
	}else if(value=='white'){
		ret = '<div class="team-bullet white-team-background"><i class="fa fa-shield text-white-team" aria-hidden="true"></i></div>';
	}
	return ret;
}
function scoreFormatter(value,row,index) {
	var res = value.split(',');
	return '<div class="team-bullet blue-team-background"><i class="fa fa-shield text-blue-team" aria-hidden="true"></i></div><div class="result">'+parseInt(res[0])+' - '+parseInt(res[1])+'</div><div class="team-bullet white-team-background"><i class="fa fa-shield text-white-team" aria-hidden="true"></i></div>';
}
function goalsFormatter(value,row,index){
	var scores ='';
	for(var i=0; i<value; i++){ scores+='<i class="fa fa-futbol-o" aria-hidden="true"></i>'; }
	return scores;
}
function mvpsFormatter(value,row,index){
	if(value == 'true'){ 
		return '<i class="fa fa-star" aria-hidden="true"></i>'; 
	}
	return '';
}
function userScoreFormatter(value,row,index){
	return ( (parseDateToLong(row.date)+pollingLimit)>=new Date().getTime() )?
			'<i class="fa fa-ban" aria-hidden="true"></i>' : value;
}
function nameFormatter(value,row,index){
	return '<img class="player-picture rounded" src="'+getPlayerPicture(value)+'"  onError="this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';"> <a href="'+window.location.origin+'/player?player='+value+'&league='+season+'">'+value+'</a>';
}
function getPlayerPicture(value){
	var playerPicture = window.location.origin+'/resources/images/unknown-player.jpg';
	if(playersPictures[value]){
		playerPicture = playersPictures[value];
	}
	return playerPicture;
}
function scoreStyle(value, row, index, field){
	var res = row.result.split(',');
	var winner = 'draw';
	if(parseInt(res[0])>parseInt(res[1])){ winner = 'blue' }
	else if(parseInt(res[0])<parseInt(res[1])){ winner = 'white' }
	if(row.team!=''){
		if(row.team==winner){
			return { classes: 'table-success' };
		}else if(row.team!=winner && winner!='draw'){
			return { classes: 'table-danger' };
		}else if(row.team!=winner && winner=='draw'){
			return { classes: 'table-warning' };
		}
	}
	return { classes: '' };
}
function rowStyle(row, index){
	if(row.team==''){
		return { classes: 'table-row-not-played' };
	}
	return { classes: '' };
}
function dateSort(sortName, sortOrder){
	var r = parseDateToLong(sortName)-parseDateToLong(sortOrder);
	return r>0?1:r<0?-1:0;
}
function getParameter(name) {
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
    var results = regex.exec(location.search);
    return results == null? null: decodeURIComponent(results[1].replace(/\+/g, " "));
}


function updateUserName(){
	if(nameweb){
		$( "td:contains('"+nameweb+"')" ).css( "font-weight", "bold" );
		$( "tr:contains('"+nameweb+"')" ).addClass( "table-info" );
	}
}

/*
 * Admin Functions
 */
function showAdmin(){
	if(nameweb && usertype == 'admin' ){
		$('#refresh').slideDown();
	}
}

function hideAdmin(){
	$('#refresh').slideUp();
}

/*
 * Utility functions
 */
function getPlayerStats(name){
	for (var i in fullRanking) {
		if(fullRanking[i].name == name){
			return fullRanking[i];
		}
	}
}
function getMaxScorer(){
	var maxAux=0, maxScorer;
	for (var i in scorers) {
		var score = parseInt(scorers[i].scores);
		if(score>maxAux){
			maxAux = score;
			maxScorer = scorers[i];
		}
	}
	return maxScorer;
}

//create players chart 
var createChart = function(pointsSeries, id){
	chart = new Highcharts.Chart({
        chart: {
        	renderTo: id,
            type: 'line'
        },
        title: {
            text: 'Acumulación de Puntos',
            style: { "display": "none"}
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                month: '%e. %b',
                year: '%b'
            },
            title: {
                text: 'Dia'
            }
        },
        yAxis: [{
            title: {
                text: 'Puntos'
            },
            min: 0
        },{
            title: {
                text: 'Goles'
            },
            min: 0,
            opposite: true
        }],

        series: pointsSeries
    });
}
var scoresPointsChart = function(permanentsRanking, substitutesRanking, id){
	var series = [];
	var data = [];
	for (var player in permanentsRanking) {
		data.push([parseInt(permanentsRanking[player].goalsFor), parseInt(permanentsRanking[player].points)]);
	}
	series[0] = {name: 'Titulares',
	        color: 'rgba(223, 83, 83, .5)',
	        data:data};
	data = [];
	for (var player in substitutesRanking) {
		data.push([parseInt(substitutesRanking[player].goalsFor), parseInt(substitutesRanking[player].points)]);
	}
	series[1] = {name: 'Suplentes',
	        color: 'rgba(119, 152, 191, .5)',
	        data:data};
	
	scoresPointsChart = new Highcharts.Chart({
        chart: {
        	renderTo: id,
            type: 'scatter',
            zoomType: 'xy'
        },
        title: {
            text: 'Distribución Goles VS Puntos',
            style: { "display": "none"}
        },
        xAxis: {
            title: {
                text: 'Goles'
            }
        },
        yAxis: {
            title: {
                text: 'Puntos'
            }
        },

        series: series
    });
	
}

//Update table with data comparison between players
var updatePlayerComparison = function(playerOne,playerTwo){

	$.post(
		'api/comparison.request', 
		JSON.stringify({season: season, 
						playerOne: playerOne,
						playerTwo: playerTwo}), 
		function( data ) {
			$('#comparison-message').empty();
			if(playerOne==playerTwo){
				$('#comparison-message').html('¿Comparando el mismo jugador carajote? anda elige dos diferentes');
				return;
			}

			if(playersPictures[playerOne]){
				$('#comparison-player-one img').attr('src',playersPictures[playerOne]);
				$('#comparison-player-one img').attr('onError','this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';');
			}else{
				$('#comparison-player-one img').attr('src',window.location.origin+'/resources/images/unknown-player.jpg');
			}
			$('#comparison-player-one h3').text(playerOne);
			if(playersPictures[playerTwo]){
				$('#comparison-player-two img').attr('src',playersPictures[playerTwo]);
				$('#comparison-player-two img').attr('onError','this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';');
			}else{
				$('#comparison-player-two img').attr('src',window.location.origin+'/resources/images/unknown-player.jpg');
			}
			$('#comparison-player-two h3').text(playerTwo);
			
			var vsTotal = data.againstWin + data.againstDraw + data.againstLose;
			$('#comparison-player-one-win h4').html(data.againstWin);
			$('#comparison-player-one-win span').html('Victorias ('+percentajeString(data.againstWin,vsTotal)+')');
			$('#comparison-player-draw h4').html(data.againstDraw);
			$('#comparison-player-draw span').html('Empates ('+percentajeString(data.againstDraw,vsTotal)+')');
			$('#comparison-player-two-win h4').html(data.againstLose);
			$('#comparison-player-two-win span').html('Victorias ('+percentajeString(data.againstLose,vsTotal)+')');
			
			fullRanking.forEach(function (value, i) {
				if(value.name===playerOne){ $('#comparison-player-one-position h5').html((i+1)+'º'); }
				if(value.name===playerTwo){ $('#comparison-player-two-position h5').html((i+1)+'º'); }
			});

			var sameTotal = data.sameWin + data.sameDraw + data.sameLose;
			$('#comparison-players-win h6').html(data.sameWin);
			$('#comparison-players-win span').html('Victorias ('+percentajeString(data.sameWin,sameTotal)+')');
			$('#comparison-players-draw h6').html(data.sameDraw);
			$('#comparison-players-draw span').html('Empates ('+percentajeString(data.sameDraw,sameTotal)+')');
			$('#comparison-players-lose h6').html(data.sameLose);
			$('#comparison-players-lose span').html('Derrotas ('+percentajeString(data.sameLose,sameTotal)+')');
			
//			if(data.againstWin || data.againstDraw || data.againstLose){
//				var playersCircleGraph = new Highcharts.Chart({
//			        chart: {
//			        	renderTo: 'players-circle-graph',
//			            plotBackgroundColor: null,
//			            plotBorderWidth: null,
//			            plotShadow: false,
//			            type: 'pie'
//			        },
//			        title: {
//			            text: 'Comparación entre jugadores',
//			            style: { "display": "none"}
//			        },
//			        series: [{
//			            name: 'Enfrentamientos',
//			            data: [{
//			                name: playerOne,
//			                y: data.againstWin,
//			                color: 'DarkOrange'
//			            }, {
//			                name: 'Empates',
//			                y: data.againstDraw,
//			                color: 'DimGrey'
//			            },{
//			                name: playerTwo,
//			                y: data.againstLose,
//			                color: 'BlueViolet'
//			            }]
//			        }]
//			    });
//			}
		}
	);
	
	$('#players-spider-graph').empty();
	var maxScorer = getMaxScorer();
	var playerOneStats = getPlayerStats(playerOne);
	var playerTwoStats = getPlayerStats(playerTwo);
	var playersSpiderGraph = new Highcharts.Chart({
        chart: {
        	renderTo: 'players-spider-graph',
            polar: true,
            type: 'line'
        },
        title: {
            text: 'Comparación entre jugadores',
            style: { "display": "none"}
        },
        xAxis: {
        	categories: ['Media de Partidos','Media de P. Ganados','Media de P. Perdidos', 'Media de Puntos', 'Media de Goles'],
            tickmarkPlacement: 'on',
            lineWidth: 0
        },
        yAxis: {
            gridLineInterpolation: 'polygon',
            lineWidth: 0,
            min: 0,
            max: 1
        },
        series: [{
                name: playerOne,
                data: [playerOneStats.matches/numMatches, playerOneStats.wins/playerOneStats.matches, 1-playerOneStats.loses/playerOneStats.matches, parseFloat(playerOneStats.pointsAVG?playerOneStats.pointsAVG:0)/3, playerOneStats.goalsFor/maxScorer.scores],
                pointPlacement: 'on',
                color: 'DarkOrange'
            }, {
                name: playerTwo,
                data: [playerTwoStats.matches/numMatches, playerTwoStats.wins/playerTwoStats.matches, 1-playerTwoStats.loses/playerTwoStats.matches, parseFloat( playerTwoStats.pointsAVG?playerTwoStats.pointsAVG:0)/3, playerTwoStats.goalsFor/maxScorer.scores],
                pointPlacement: 'on',
                color: 'BlueViolet'
            }]
    });
}
  
//Update scores for players in matches panel
var updateTeamScorers = function(match, appendTo, withMVP){
	if(withMVP){
		 $(appendTo).filter(function() {
			var playerName = $(this).text().trim();
			var ret = false;
			if(mvpsByDate[longToDate(match.day)]){
				mvpsByDate[longToDate(match.day)].forEach(function (value, i) {
					if(playerName === value){
						ret = true; 
						return; 
					}
				});
			}
			return ret;
		 }).append(' <i class="fa fa-star" aria-hidden="true"></i>');
	}
	
	$.post(
			'api/match-scorers.request', 
			JSON.stringify({season: season, match: match}), 
			function( matchScores ) {
				if(matchScores){
					for (var name in matchScores) {
					  var score= matchScores[name];
					  var icons='';
					  for(var i=0;i<score;i++){
						icons += '<i class="fa fa-futbol-o" aria-hidden="true"></i>';
					  }
//					  $( "#table-results td:contains('"+name+"')" ).append(icons);
					  //TODO test this. must be equal not contains
					  $(appendTo).filter(function() {
  					    return $(this).text().trim() === name;
					  }).append(icons);
					}
				}else{
				console.warn("Unknown exception updating team scorers: "+matchScores);
				}
			}
  	);
}

/*
 * Initialization
 */
$(function () {
		  
	$.timeago.settings.strings = {
		   prefixAgo: "hace",
		   prefixFromNow: "dentro de",
		   suffixAgo: "",
		   suffixFromNow: "",
		   seconds: "menos de un minuto",
		   minute: "un minuto",
		   minutes: "unos %d minutos",
		   hour: "una hora",
		   hours: "%d horas",
		   day: "un día",
		   days: "%d días",
		   month: "un mes",
		   months: "%d meses",
		   year: "un año",
		   years: "%d años"
	};
	$.timeago.settings.allowFuture= true;
    
    var playersFunction = function() {
    	var num = players.length;
    	players.sort(function (a, b) {return a.localeCompare(b);});
    	$('#player-one,#player-two').empty();
    	for(var i=0; i<num; i++) {
    	    $('#player-one,#player-two').append($('<option/>').val(players[i]).text(players[i]));
    	};
    	var playerOne = players[Math.floor(Math.random() * num)];
    	$('#player-one option[value="'+playerOne+'"]').prop('selected', true);
    	var playerTwo = players[Math.floor(Math.random() * num)];
    	$('#player-two option[value="'+playerTwo+'"]').prop('selected', true);
    	updatePlayerComparison(playerOne,playerTwo);
    }
    
    //Matches Panel
    var matchesFunction = function() {
    	
    	selectedSeasonMatches = matches;
	    
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    var lastMatch = selectedSeasonMatches[numMatches-1].data;
	    var remarks = (selectedSeasonMatches[numMatches-1].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';   
	    
	    $('#table-results').bootstrapTable({
	        data: lastMatch,
	        locale:'es-ES'
	    });
	    
//	    $('#table-results-header span').text('Jornada '+numMatches);
	    $('#table-results-header span').text('Jornada '+numMatches+' ('+longToDate(selectedSeasonMatches[numMatches-1].day)+')'+remarks);
	    $('#table-results thead tr:last th:first div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreBlues));
	    $('#table-results thead tr:last th:last div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreWhites));
	    
	    $('#next-match').addClass('disabled');
	    updateUserName();
	    updateTeamScorers(selectedSeasonMatches[currentMatch], "#table-results td", true);
	    
	    var updateResult = function(currentMatch){
			$("#match-winner-button").removeClass("disabled");
		    var newMatch = selectedSeasonMatches[currentMatch].data;
		    var remarks = (selectedSeasonMatches[currentMatch].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';
		    
		    $('#table-results').bootstrapTable('load',newMatch);
		    
//		    $('#table-results-header span').text('Jornada '+(currentMatch+1));
		    $('#table-results-header span').text('Jornada '+(currentMatch+1)+' ('+longToDate(selectedSeasonMatches[currentMatch].day)+')'+remarks);
		    $('#table-results thead tr:last th:first div.th-inner span').text(parseInt(selectedSeasonMatches[currentMatch].scoreBlues));
		    $('#table-results thead tr:last th:last div.th-inner span').text(parseInt(selectedSeasonMatches[currentMatch].scoreWhites));
		    
		    updateUserName();
		    updateTeamScorers(selectedSeasonMatches[currentMatch], "#table-results td", true);
		};
		$('#next-match, #previous-match').unbind('click');
	    $('#next-match').click(function(){
	    	if(!$('#next-match').hasClass('disabled')){
		    	currentMatch = currentMatch + 1;
			    $('#previous-match').removeClass('disabled');
		    	if(currentMatch==(numMatches-1)){
				    $('#next-match').addClass('disabled');
		    	}
		    	updateResult(currentMatch);
	    	}
	    });
	    
	    $('#previous-match').click(function(){
	    	if(!$('#previous-match').hasClass('disabled')){
		    	currentMatch = currentMatch - 1;
			    $('#next-match').removeClass('disabled');
		    	if(currentMatch==0){
				    $('#previous-match').addClass('disabled');
		    	}
		    	updateResult(currentMatch);
	    	}
	    });
    }
    
	//Prepare page
    season = getParameter('league');
    player = getParameter('player');
	if(window.location.pathname.includes('/player')){
		
		$('#user-name').text(player);
		$('#user-picture').on('error', function () {
			console.warn('No se puede cargar fotos de '+player+': '+playersPictures[player]);
			$('#user-picture').attr('src',window.location.origin+'/resources/images/unknown-player.jpg');
		});
		if(playersPictures[player]){
			$('#user-picture').attr('src',playersPictures[player]);
		}else{
			$('#user-picture').attr('src',window.location.origin+'/resources/images/unknown-player.jpg');
		}
		$('#title').html('Liga '+season);

		listen('LOGIN', function(){
			var form = $('<form method="post" action="" encType="multipart/form-data" id="upload-picture-form"></form>');
			form.append('<input type="file" id="upload-picture-form-file" name="upload-picture-form-file"/>');
			$('#user-picture').after(form);
			$('#user-picture').click(function(event){
				console.log('click upload');
				$('#user-picture').addClass('uploading');
				$('#upload-picture-form-file').trigger('click');
				console.log('open upload');
				$('#upload-picture-form-file').change(function(){
					console.log('file added');
					var fd = new FormData();
					var files = $('#upload-picture-form-file')[0].files;
					if(files.length > 0 ){
						fd.append('file',files[0]);
						console.log('file sent');
						$.post(
							'api/upload-avatar.request',
							fd,
							function( data ) {
								$('#user-picture').removeClass('uploading');
								if(!data || !data.error){
									console.log('file added: '+data.imageURL);
									//TODO change image with new path
								}else{
									$('#user-picture').removeClass('error');
									console.log('file error');
								}
							}
						);
					}
				});
			});
		});
		
		$.post('api/userStats.request', 
				JSON.stringify({season: season, 
								player: player?player:''}), 
				function( data ) {
			    	if(data){
			    		$("#points").text(data.realPoints+' ('+data.points+')');
			    		$("#win").text(data.wins);
			    		$("#draw").text(data.draws);
			    		$("#lose").text(data.loses);
			    		$("#goals").text(data.goalsFor);
			    		$("#matches").text(data.matches);
			    		$("#avg-points").text(parseFloat(data.pointsAVG).toFixed(2));
			    		$("#mvps").text(mvps[player]?mvps[player]:'0');
			    		$("#avg-goals").text(parseFloat(data.goalsForAVG).toFixed(2));
			    	}
	    });
	    
    	selectedSeasonMatches = matches;
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    
	    $('#table-player-matches').bootstrapTable({
	        data: userMatches,
	        locale:'es-ES'
	    });
		if(pointsSeries){ createChart(pointsSeries, 'container-player-graph'); }
    	$('#points[data-toggle="tooltip"]').tooltip();
	    
	}else if(window.location.pathname.includes('/league')){
		
		$('#title').html('Liga: '+season);
		
	    matchesFunction();
	    playersFunction();
    	
	    $('#table-full').bootstrapTable({
	        data: fullRanking,
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-permanents').bootstrapTable({
	        data: permanentsRanking,
	        locale:'es-ES',
	        fixedColumns: true,
	        fixedNumber: 1,
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-substitutes').bootstrapTable({
	        data: substitutesRanking,
	        locale:'es-ES',
	        fixedColumns: true,
	        fixedNumber: 1,
	        onAll: function(name, args){ updateUserName() }
	    });
		    
	    $('#table-vs').bootstrapTable({
	        data: vs,
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-pair').bootstrapTable({
	        data: pair,
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-scorers').bootstrapTable({
	        data: scorers,
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
		if(pointsSeries){ createChart(pointsSeries, 'container-graph'); }
//		if(permanentsRanking && substitutesRanking){ scoresPointsChart(permanentsRanking, substitutesRanking, 'scores-points-graph'); }
		
	//index page
	}else{
		var todayDate = new Date();
		var year = todayDate.getFullYear();
		var month = todayDate.getMonth();//Starting in 0
		var thisSeason = (month>7?year:year-1);//7 is August
		for (var prop in options.permanents) {
			if(prop != 'default'){
				var res = prop.split(' ');
				var rowId = prop.substring(4,0)==thisSeason?'current-season-row':'past-season-row';
				var body = "Titulares: ";
				var imageCollage = '';
				options.permanents[prop].forEach(function (value, i) {
					body += value + (i<options.permanents[prop].length-1?', ':'.');
					imageCollage += '<a href="'+window.location.origin+'/player?player='+value+'&league='+prop+'" class="collage-image-link">'+
						'<img class="player-picture rounded" src="'+getPlayerPicture(value)+'" '+
						'onError="this.onerror=null;this.src=\''+window.location.origin+'/resources/images/unknown-player.jpg\';"></a>';
				});
				var leaguePanel = $('<div class="card text-center" >'+
										'<div class="card-header">'+imageCollage+'</div>'+
										'<div class="card-body">'+
											'<h2 class="card-title">'+prop+'</h2>'+
											'<p class="card-text">'+body+'</p>'+
											'<a href="/league?league='+prop+'" class="btn btn-dark btn-lg">Ir a la liga del '+(prop.substring(4,0)==thisSeason?res[1]:prop)+'</a>'+
										'</div>'+
									'</div>').prependTo('#'+rowId);	
			}
		}
	}
    
	//player comparison on change update data
    $('#player-one,#player-two').change(function() {
    	var playerOne = $( "#player-one option:selected" ).text();
    	var playerTwo = $( "#player-two option:selected" ).text();
    	updatePlayerComparison(playerOne,playerTwo);
    });
    
    //refresh and process data, only for admin
    $('#refresh').click(function(){
    	$('#refresh i').addClass('fa-spin');
    	$.getJSON('api/refresh.request', function (data) {
    		if(data=='true' || data==true){
    			location.reload();
    		}
    	});
    });
    
});