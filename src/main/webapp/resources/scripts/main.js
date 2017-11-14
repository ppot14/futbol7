/**
 * 
 */
var options;
var selectedSeasonMatches, numMatches, currentMatch;
var chart;

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

function showAdmin(){
	if($("#season-selector").val()){
		var seasonLastYear = $("#season-selector").val().substring(5);
		if(nameweb && (usertype == 'admin' || new Date()>new Date(seasonLastYear,6))){//All data public after 1st July at the end of the season
			$('#refresh').slideDown();
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'realPoints');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'wins');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'draws');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'loses');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'matches');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsFor');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsAgainst');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'pointsAVG');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsForAVG');
	    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsAgainstAVG');
	
	    	$('#row1').show();
	    	$('#row3').show();
	    	$('#row4').show();
	    	
	    	$.getJSON(window.location.pathname+'api/pointsSeries.json', function (pointsSeries) {
	    	
	    			createChart(pointsSeries);
		    
	    	});
		}
	}
}

function hideAdmin(){
	$('#refresh').slideUp();
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'realPoints');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'wins');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'draws');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'loses');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'matches');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsFor');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsAgainst');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'pointsAVG');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsForAVG');
	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsAgainstAVG');

	$('#row1').hide();
	$('#row3').hide();
	$('#row4').hide();
	
	try{ chart.destroy(); }catch(e){console.warn(e.message)};
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
// 		        tooltip: {
// 		            headerFormat: '<b>{series.name}</b><br>',
// 		            pointFormat: '{point.x:%e. %b}: {point.y:.2f} m'
// 		        },

// 		        plotOptions: {
// 		            line: {
// 		                step: false,
////		                pointStart: 0
// 		                connectNulls: true
// 		            }
// 		        },

        series: pointsSeries[$("#season-selector").val()]
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
    	var num = players[$("#season-selector").val()].length;
    	$('#player-one,#player-two').empty();
    	for(var i=0; i<num; i++) {
    	    $('#player-one,#player-two').append($('<option/>').val(players[$("#season-selector").val()][i]).text(players[$("#season-selector").val()][i]));
    	};
    }
    
    //Matches AJAX request data
    var matchesFunction = function(matches) {
    	
    	selectedSeasonMatches = matches[$("#season-selector").val()];
	    
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    var lastMatch = selectedSeasonMatches[numMatches-1].data;
	    var remarks = (selectedSeasonMatches[numMatches-1].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';

		//Receive players pictures
		$.getJSON(window.location.pathname+'api/playersPictures.json', function(data) {
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
	    
	    $('#table-results thead tr:first th div.th-inner').text('Jornada '+numMatches+' ('+$.timeago(selectedSeasonMatches[numMatches-1].day)+')'+remarks);
	    $('#table-results thead tr:last th:first div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreBlues));
	    $('#table-results thead tr:last th:last div.th-inner span').text(parseInt(selectedSeasonMatches[numMatches-1].scoreWhites));
	    
	    $('#next-match').addClass('disabled');
	    
	    var updateResult = function(currentMatch){
		    var newMatch = selectedSeasonMatches[currentMatch].data;
		    var remarks = (selectedSeasonMatches[currentMatch].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';
		    
		    $('#table-results').bootstrapTable('load',newMatch);
		    
		    $('#table-results thead tr:first th div.th-inner').text('Jornada '+(currentMatch+1)+' ('+$.timeago(selectedSeasonMatches[currentMatch].day)+')'+remarks);
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
	  
    var changeSeason = function(season){

	    $.getJSON(window.location.pathname+'api/full.json', function(full) {
	    	$('#table-full').bootstrapTable('load', full[season]);
	    });
	    $.getJSON(window.location.pathname+'api/permanents.json', function(permanents) {
	    	$('#table-permanents').bootstrapTable('load', permanents[season]);
	    });
	    $.getJSON(window.location.pathname+'api/substitutes.json', function(substitutes) {
		    $('#table-substitutes').bootstrapTable('load', substitutes[season]);
	    });
	    $.getJSON(window.location.pathname+'api/vs.json', function(vs) {
		    $('#table-vs').bootstrapTable('load', vs[season]);
	    });
	    $.getJSON(window.location.pathname+'api/pair.json', function(pair) {
			$('#table-pair').bootstrapTable('load', pair[season]);
	    });
	    chart.destroy();
    	$.getJSON(window.location.pathname+'api/pointsSeries.json', function (pointsSeries) {
			createChart(pointsSeries);
	    });
	    $.getJSON(window.location.pathname+'api/matches.json', matchesFunction);
	    $.getJSON(window.location.pathname+'api/players.json', playersFunction);
	    $.getJSON(window.location.pathname+'api/scorers.json', function(scorers) {
			$('#table-scorers').bootstrapTable('load', scorers[season]);
	    });
    	
    };
  
  	$.getJSON(window.location.pathname+'api/options.json', function(data) {
  		options = data;
  		var select = $("#season-selector");
  		for (var prop in options.permanents) {
//  			if(prop=='2017-2018'){//TODO change seasons
  				select.prepend($('<option '+(prop=='2017-2018'?' selected="selected"':'')+'/>').val(prop).text("Temporada "+prop));
//  			}
  		}
	    $.getJSON(window.location.pathname+'api/matches.json', matchesFunction);
	    $.getJSON(window.location.pathname+'api/players.json', playersFunction);
  		select.on('change', function() {
  			changeSeason(this.value);
  		});
    	
	  	var season = $("#season-selector").val();
	    $.getJSON(window.location.pathname+'api/full.json', function(full) {
		    $('#table-full').bootstrapTable({
		        data: full[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
	    });
	    $.getJSON(window.location.pathname+'api/permanents.json', function(permanents) {
		    $('#table-permanents').bootstrapTable({
		        data: permanents[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
	    });
	    $.getJSON(window.location.pathname+'api/substitutes.json', function(substitutes) {
		    $('#table-substitutes').bootstrapTable({
		        data: substitutes[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
	    });
	    $.getJSON(window.location.pathname+'api/vs.json', function(vs) {
		    $('#table-vs').bootstrapTable({
		        data: vs[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
	    });
	    $.getJSON(window.location.pathname+'api/pair.json', function(pair) {
		    $('#table-pair').bootstrapTable({
		        data: pair[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
	    });
	    $.getJSON(window.location.pathname+'api/scorers.json', function(scorers) {
		    $('#table-scorers').bootstrapTable({
		        data: scorers[season],
		        locale:'es-ES',
		        onAll: function(name, args){ updateUserName() }
		    });
//			$('#table-scorers').on('page-change.bs.table', function (number, size) {
//			    updateUserName();
//			});
	    });
  	});
    
    $('#player-one,#player-two').change(function() {
    	var playerOne = $( "#player-one option:selected" ).text();
    	var playerTwo = $( "#player-two option:selected" ).text();
    	$.post(
    			window.location.pathname+'api/comparison.json', 
    			JSON.stringify({season: $("#season-selector").val(), 
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
    	$.getJSON(window.location.pathname+'api/refresh.json', function (data) {
    		if(data=='true' || data==true){
//			    $('#table-full').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/full.json'
//			    });
//			    $('#table-permanents').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/permanents.json'
//			    });
//			    $('#table-substitutes').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/substitutes.json'
//			    });
//			    $('#table-vs').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/vs.json'
//			    });
//			    $('#table-pair').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/pair.json'
//			    });
//			    $.getJSON(window.location.pathname+'api/matches.json', matchesFunction);
//		    	$.getJSON(window.location.pathname+'api/pointsSeries.json', function (pointsSeries) {
//		    		if(chart){
//		    			chart.series = pointsSeries[$("#season-selector").val()];
//		    			chart.redraw();
//		    		}
//		    	});
//			    $.getJSON(window.location.pathname+'api/players.json', playersFunction);
//			    $('#table-scorers').bootstrapTable('refresh', {
//			    	url: window.location.pathname+'api/scorers.json'
//			    });
    			location.reload();
    		}
    	});
    });
    
});