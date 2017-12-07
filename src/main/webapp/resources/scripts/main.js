/**
 * main.js
 */
var options;
var season;
var selectedSeasonMatches, numMatches, currentMatch;
var chart;

function parseDate(s){
	var parts = s.split("/");
	return new Date(parseInt(parts[2], 10),
	                  parseInt(parts[1], 10) - 1,
	                  parseInt(parts[0], 10));
}
function parseDateToLong(s){
	return parseDate(s).getTime();
}
function getParameterByName(name) {
      name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
      var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
      results = regex.exec(location.search);
      return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
  
function decimal(value, row) {
	return parseFloat(Math.round(value * 100) / 100).toFixed(2);
}
	  
function lastMatchesFormat(value) {
	var ret = '';
	var len = value.length;
	var numMatches = 5;
	for (var i = len-numMatches; i < len; i++) {
        if(value[i]=='w') ret = ret + '<span class="label label-success label-lastmatch">V</span>';
        if(value[i]=='d') ret = ret + '<span class="label label-warning label-lastmatch">E</span>';
        if(value[i]=='l') ret = ret + '<span class="label label-danger label-lastmatch">D</span>';
        if(value[i]=='-') ret = ret + '<span class="label label-default label-lastmatch">-</span>';
	}
	return ret;
}
function teamFormatter(value) {
	var ret = '-';
	if(value=='blue'){
		ret = '<div class="team-bullet blue-team"><i class="fa fa-shield" aria-hidden="true"></i></div>';
	}else if(value=='white'){
		ret = '<div class="team-bullet white-team"><i class="fa fa-shield" aria-hidden="true"></i></div>';
	}
	return ret;
}
function scoreFormatter(value,row,index) {
	var res = value.split(',');
	return '<div class="team-bullet blue-team"><i class="fa fa-shield" aria-hidden="true"></i></div><div class="result">'+parseInt(res[0])+' - '+parseInt(res[1])+'</div><div class="team-bullet white-team"><i class="fa fa-shield" aria-hidden="true"></i></div>';
}
function scoreStyle(value, row, index, field){
	var res = row.result.split(',');
	var winner = 'draw';
	if(parseInt(res[0])>parseInt(res[1])){ winner = 'blue' }
	else if(parseInt(res[0])<parseInt(res[1])){ winner = 'white' }
	if(row.team!=''){
		if(row.team==winner){
			return { classes: 'success' };
		}else if(row.team!=winner && winner!='draw'){
			return { classes: 'danger' };
		}else if(row.team!=winner && winner=='draw'){
			return { classes: 'warning' };
		}
	}
	return { classes: '' };
}
function rowStyle(row, index){
	if(row.team==''){
		return { classes: 'active' };
	}
	return { classes: '' };
}
function dateSort(sortName, sortOrder){
	var r = parseDateToLong(sortName)-parseDateToLong(sortOrder);
	return r>0?1:r<0?-1:0;
}

function showAdmin(){

	if(window.location.pathname.includes('/me')){
		
	}else{
	
		if(season){
			var seasonLastYear = season.substring(5);
			if(nameweb && (usertype == 'admin' || new Date()>new Date(seasonLastYear,6))){//All data public after 1st July at the end of the season
				$('#refresh').slideDown();
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'realPoints');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'wins');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'draws');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'loses');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'matches');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsFor');
	//	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsAgainst');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'pointsAVG');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsForAVG');
	//	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsAgainstAVG');
		    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'scoreAVG');
		
		    	$('#row1').show();
		    	$('#row3').show();
		    	$('#row4').show();
		    	
		    	$.getJSON(window.location.pathname+'api/pointsSeries.request', function (pointsSeries) {
		    		createChart(pointsSeries);
		    	});
			}
		}	
		
	}
}

function hideAdmin(){
	$('#refresh').slideUp();

	if(window.location.pathname.includes('/me')){
		
	}else{
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'realPoints');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'wins');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'draws');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'loses');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'matches');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsFor');
	//	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsAgainst');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'pointsAVG');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsForAVG');
	//	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsAgainstAVG');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'scoreAVG');
	
		$('#row1').hide();
		$('#row3').hide();
		$('#row4').hide();
		
		try{ chart.destroy(); }catch(e){console.warn(e.message)};
	}
}

var createChart = function(pointsSeries){
	chart = new Highcharts.Chart({
        chart: {
        	renderTo: 'container-graph',
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
        yAxis: {
            title: {
                text: 'Puntos'
            },
            min: 0
        },

        series: pointsSeries[season]
    });
}
  
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
    
    var playersFunction = function(players) {
    	var num = players[season].length;
    	players[season].sort(function (a, b) {return a.localeCompare(b);});
    	$('#player-one,#player-two').empty();
    	for(var i=0; i<num; i++) {
    	    $('#player-one,#player-two').append($('<option/>').val(players[season][i]).text(players[season][i]));
    	};
    }
    
    //Matches AJAX request data
    var matchesFunction = function(matches) {
    	
    	selectedSeasonMatches = matches[season];
	    
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    var lastMatch = selectedSeasonMatches[numMatches-1].data;
	    var remarks = (selectedSeasonMatches[numMatches-1].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';

		//Receive players pictures
		$.getJSON(window.location.pathname+'api/playersPictures.request', function(data) {
	    	if(data){ playersPictures = data }
	    	else{ console.warn('Pictures from players not received')};
	    });	    
    	
    	if((selectedSeasonMatches[currentMatch].day+pollingLimit<new Date().getTime()) &&
    			(selectedSeasonMatches[currentMatch].day+pollingLimit+24*60*60*1000>new Date().getTime())){
    		setTimeout(function(){
        		$('#last-match-winner-button').removeClass('btn-default').addClass('btn-success');
    		},3000);
    	}	    
	    
	    $('#table-results').bootstrapTable({
	        data: lastMatch,
	        locale:'es-ES'
	    });
	    
	    $('#table-results thead tr:first th div.th-inner span').text('Jornada '+numMatches+' ('+$.timeago(selectedSeasonMatches[numMatches-1].day)+')'+remarks);
	    $('#table-results thead tr:first th div.th-inner #match-winner-button').attr('data-match-day',''+(numMatches-1));
	    $('#table-results thead tr:last th:first div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreBlues));
	    $('#table-results thead tr:last th:last div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreWhites));
	    
	    $('#next-match').addClass('disabled');
	    
	    var updateResult = function(currentMatch){
			$("#match-winner-button").removeClass("disabled");
		    var newMatch = selectedSeasonMatches[currentMatch].data;
		    var remarks = (selectedSeasonMatches[currentMatch].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';
		    
		    $('#table-results').bootstrapTable('load',newMatch);
		    
		    $('#table-results thead tr:first th div.th-inner span').text('Jornada '+(currentMatch+1)+' ('+$.timeago(selectedSeasonMatches[currentMatch].day)+')'+remarks);
		    $('#table-results thead tr:first th div.th-inner #match-winner-button').attr('data-match-day',''+currentMatch);
		    $('#table-results thead tr:last th:first div.th-inner span').text(parseInt(selectedSeasonMatches[currentMatch].scoreBlues));
		    $('#table-results thead tr:last th:last div.th-inner span').text(parseInt(selectedSeasonMatches[currentMatch].scoreWhites));
		    
		    updateUserName();
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
	  
    var changeSeason = function(newSeason){
    	season=newSeason;

  		if(window.location.pathname.includes('/me')){
  			
		    $.getJSON(window.location.pathname+'/api/matches.request', function(matches){
		    	selectedSeasonMatches = matches[season];
			    numMatches = selectedSeasonMatches.length;
			    currentMatch = numMatches-1;
		    });
		    $.getJSON(window.location.pathname+'api/userStats.request?season='+season, function(data) {
		    	if(data){
		    		$("#points").text(data.points+' ('+data.realPoints+')');
		    		$("#win").text(data.wins);
		    		$("#draw").text(data.draws);
		    		$("#lose").text(data.loses);
		    		$("#goals").text(data.goalsFor);
		    		$("#matches").text(data.matches);
		    		$("#avg-points").text(parseFloat(data.pointsAVG).toFixed(2));
		    		$("#avg-scores").text(parseFloat(data.scoreAVG).toFixed(2));
		    		$("#avg-goals").text(parseFloat(data.goalsForAVG).toFixed(2));
		    	}
		    });

		    $.getJSON(window.location.pathname+'api/userMatches.request', function(data) {
			    $('#table-player-matches').bootstrapTable('load', data[season]);
		    });
		    
  		}else{
  			
		    $.getJSON(window.location.pathname+'api/full.request', function(full) {
		    	$('#table-full').bootstrapTable('load', full[season]);
		    });
		    $.getJSON(window.location.pathname+'api/permanents.request', function(permanents) {
		    	$('#table-permanents').bootstrapTable('load', permanents[season]);
		    });
		    $.getJSON(window.location.pathname+'api/substitutes.request', function(substitutes) {
			    $('#table-substitutes').bootstrapTable('load', substitutes[season]);
		    });
		    $.getJSON(window.location.pathname+'api/vs.request', function(vs) {
			    $('#table-vs').bootstrapTable('load', vs[season]);
		    });
		    $.getJSON(window.location.pathname+'api/pair.request', function(pair) {
				$('#table-pair').bootstrapTable('load', pair[season]);
		    });
		    chart.destroy();
	    	$.getJSON(window.location.pathname+'api/pointsSeries.request', function (pointsSeries) {
				createChart(pointsSeries);
		    });
		    $.getJSON(window.location.pathname+'api/matches.request', matchesFunction);
		    $.getJSON(window.location.pathname+'api/players.request', playersFunction);
		    $.getJSON(window.location.pathname+'api/scorers.request', function(scorers) {
				$('#table-scorers').bootstrapTable('load', scorers[season]);
		    });	
		    
  	    }
    	
    };
  
  	$.getJSON(window.location.pathname+'api/options.request', function(data) {
  		options = data;
  		var select = $("#season-selector");
  		for (var prop in options.permanents) {
  			select.prepend($('<option '+(prop=='2017-2018'?' selected="selected"':'')+'/>').val(prop).text("Temporada "+prop));
  		}
  		season = $("#season-selector").val();
  		select.on('change', function() {
  			changeSeason(this.value);
  		});
	    
  		if(window.location.pathname.includes('/me')){

  		    $.getJSON(window.location.pathname+'/api/matches.request', function(matches){
		    	selectedSeasonMatches = matches[season];
			    numMatches = selectedSeasonMatches.length;
			    currentMatch = numMatches-1;
		    });
		    $.getJSON(window.location.pathname+'api/userStats.request?season='+season, function(data) {
		    	if(data){
		    		$("#points").text(data.points+' ('+data.realPoints+')');
		    		$("#win").text(data.wins);
		    		$("#draw").text(data.draws);
		    		$("#lose").text(data.loses);
		    		$("#goals").text(data.goalsFor);
		    		$("#matches").text(data.matches);
		    		$("#avg-points").text(parseFloat(data.pointsAVG).toFixed(2));
		    		$("#avg-scores").text(parseFloat(data.scoreAVG).toFixed(2));
		    		$("#avg-goals").text(parseFloat(data.goalsForAVG).toFixed(2));
		    	}
		    });

		    $.getJSON(window.location.pathname+'api/userMatches.request', function(data) {
			    $('#table-player-matches').bootstrapTable({
			        data: data[season],
			        locale:'es-ES'
			    });
		    });
  		    
  		}else{

  		    $.getJSON(window.location.pathname+'api/matches.request', matchesFunction);
		    $.getJSON(window.location.pathname+'api/players.request', playersFunction);
	    	
		    $.getJSON(window.location.pathname+'api/full.request', function(full) {
			    $('#table-full').bootstrapTable({
			        data: full[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
		    $.getJSON(window.location.pathname+'api/permanents.request', function(permanents) {
			    $('#table-permanents').bootstrapTable({
			        data: permanents[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
		    $.getJSON(window.location.pathname+'api/substitutes.request', function(substitutes) {
			    $('#table-substitutes').bootstrapTable({
			        data: substitutes[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
		    $.getJSON(window.location.pathname+'api/vs.request', function(vs) {
			    $('#table-vs').bootstrapTable({
			        data: vs[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
		    $.getJSON(window.location.pathname+'api/pair.request', function(pair) {
			    $('#table-pair').bootstrapTable({
			        data: pair[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
		    $.getJSON(window.location.pathname+'api/scorers.request', function(scorers) {
			    $('#table-scorers').bootstrapTable({
			        data: scorers[season],
			        locale:'es-ES',
			        onAll: function(name, args){ updateUserName() }
			    });
		    });
  			
  		}
  	});
    
    $('#player-one,#player-two').change(function() {
    	var playerOne = $( "#player-one option:selected" ).text();
    	var playerTwo = $( "#player-two option:selected" ).text();
    	$.post(
    			window.location.pathname+'api/comparison.request', 
    			JSON.stringify({season: season, 
    							playerOne: playerOne,
    							playerTwo: playerTwo}), 
    			function( data ) {
    				$('#table-comparison tbody').empty();
    				if(playerOne==playerTwo){
    					$('#table-comparison tbody').append('<tr><td colspan="2">¿Comparando el mismo jugador? Eres carajote</td></tr>');
    					return;
    				}
    				$('#table-comparison tbody').append('<tr><td colspan="2">Como rivales:</td></tr>');
    				$('#table-comparison tbody').append('<tr class="success"><td colspan="2">'+playerOne+' ha ganado <b>'+data.againstWin+'</b> partidos contra '+playerTwo+'</td></tr>');
    				$('#table-comparison tbody').append('<tr class="warning"><td colspan="2">'+playerOne+' ha empatado <b>'+data.againstDraw+'</b> partidos contra '+playerTwo+'</td></tr>');
    				$('#table-comparison tbody').append('<tr class="danger"><td colspan="2">'+playerOne+' ha perdido <b>'+data.againstLose+'</b> partidos contra '+playerTwo+'</td></tr>');
    				$('#table-comparison tbody').append('<tr class="info"><td colspan="2">'+playerOne+' ha jugado <b>'+(data.againstWin+data.againstDraw+data.againstLose)+'</b> partidos contra '+playerTwo+'</td></tr>');
    				$('#table-comparison tbody').append('<tr><td colspan="2">En el mismo equipo:</td></tr>');
    				$('#table-comparison tbody').append('<tr class="success"><td colspan="2">'+playerOne+' y '+playerTwo+' han ganado <b>'+data.sameWin+'</b></td></tr>');
    				$('#table-comparison tbody').append('<tr class="warning"><td colspan="2">'+playerOne+' y '+playerTwo+' han empatado <b>'+data.sameDraw+'</b></td></tr>');
    				$('#table-comparison tbody').append('<tr class="danger"><td colspan="2">'+playerOne+' y '+playerTwo+' han perdido <b>'+data.sameLose+'</b></td></tr>');
    				$('#table-comparison tbody').append('<tr class="info"><td colspan="2">'+playerOne+' y '+playerTwo+' han jugado <b>'+(data.sameWin+data.sameDraw+data.sameLose)+'</b></td></tr>');
    			}
    	);
    });
    
    $('#refresh').click(function(){
    	$.getJSON(window.location.pathname+'api/refresh.request', function (data) {
    		if(data=='true' || data==true){
    			location.reload();
    		}
    	});
    });
    
});