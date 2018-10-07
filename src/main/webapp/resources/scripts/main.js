/**
 * main.js
 */

/*
 * Variables and Constants
 */
var options;
var season;
var selectedSeasonMatches, numMatches, currentMatch;
var chart;
var nameweb;
var titles = [{
		id: 'trompito', icon: 'undo', name:'El Trompito', title:'Título al más chupón', desc:'El Trompito. Título al más chupón del partido, ese que es capaz de recorrer los cuatro corners con el balon en los pies antes de tirar a puerta. Homenaje del Sr Bordas al Sr Rivera'
	},{
		id: 'dandy', icon: 'glass', name:'El Dandy', title:'Título al jugador con más clase y elegante', desc:'El Dandy. Título al jugador con más clase y elegancia, ese que no necesita la posesión y deja unas asistencias a la derecha mirando a la izquierda que más quisiera Michael Laudrup. Homenaje a Sr Revuelta'
	},{
		id: 'frances', icon: 'wheelchair-alt', name:'El Francés', title:'TTítulo al más guarro', desc:'El Francés. Título al más guarro, ese que es capaz de dejarte un cardenal del tamaño del obispo de Jerez durante temporada y media y encima dice que no es falta. En honor al Sr Mativet'
	},{
		id: 'sillegas', icon: 'clock-o', name:'El Sillegas', title:'Título al más impuntual e impresentable', desc:'El Sillegas. Título al más impuntual e impresentable, ese que es capaz de buscar atascos en Google Maps para no llegar a su hora aunque trabaje menos que un funcionario de la junta. Homenaje a Sr Pozo y Sr Villegas'
	},{
		id: 'porculero', icon: 'bullhorn', name:'El Porculero', title:'Título al más protestón y bocazas', desc:'El Porculero. Título al más protestón y bocazas, ese que es capaz de pelearse hasta con los del equipo contrario, que protesta hasta cuando tiene el balón o que tiene a su equipo desquiciado aúnque ganen 10-0. Honorífico a los Sr Bordas, Sr Tristán y Sr Rivera'
	}];

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
function userScoreFormatter(value,row,index){
	return ( (parseDateToLong(row.date)+pollingLimit)>=new Date().getTime() )?
			'<i class="fa fa-ban" aria-hidden="true"></i>' : value;
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


function updateUserName(){
	if(nameweb){
		$( "td:contains('"+nameweb+"')" ).css( "font-weight", "bold" );
		$( "tr:contains('"+nameweb+"')" ).addClass( "info" );
	}
}

/*
 * Admin Functions
 */
function showAdmin(){

	if(window.location.pathname.includes('/me')){
		
	}else{
		
		if(nameweb && usertype == 'admin' ){
			$('#refresh').slideDown();
		}	
		
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'realPoints');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'wins');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'draws');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'loses');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'matches');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsFor');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'pointsAVG');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'goalsForAVG');
    	$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('showColumn', 'scoreAVG');

    	$('#row1').show();
    	$('#row3').show();
    	$('#row4').show();
    	
		createChart(pointsSeries, 'container-graph');
		
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
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'pointsAVG');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'goalsForAVG');
		$('#table-full, #table-permanents, #table-substitutes').bootstrapTable('hideColumn', 'scoreAVG');
	
		$('#row1').hide();
		$('#row3').hide();
		$('#row4').hide();
		
		try{ chart.destroy(); }catch(e){console.warn(e.message)};
	}
}

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

        series: pointsSeries[season]
    });
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
    	var num = players[season].length;
    	players[season].sort(function (a, b) {return a.localeCompare(b);});
    	$('#player-one,#player-two').empty();
    	for(var i=0; i<num; i++) {
    	    $('#player-one,#player-two').append($('<option/>').val(players[season][i]).text(players[season][i]));
    	};
    }
    
    //Matches AJAX request data
    var matchesFunction = function() {
    	
    	selectedSeasonMatches = matches[season];
	    
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    var lastMatch = selectedSeasonMatches[numMatches-1].data;
	    var remarks = (selectedSeasonMatches[numMatches-1].remarks.indexOf('RD') > -1) ?' (Número de goles desconocido, por defecto 1-0 o 0-0)':'';   
	    
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
  			
	    	selectedSeasonMatches = matches[season];
		    numMatches = selectedSeasonMatches.length;
		    currentMatch = numMatches-1;

		    $('#table-player-matches').bootstrapTable('load', userMatches[season]);
			createChart(pointsSeries, 'container-player-graph');
	    	$('#points[data-toggle="tooltip"]').tooltip();
		    
  		}else{
  			
	    	$('#table-full').bootstrapTable('load', fullRanking[season]);
	    	$('#table-permanents').bootstrapTable('load', permanentsRanking[season]);
		    $('#table-substitutes').bootstrapTable('load', substitutesRanking[season]);
			$('#table-vs').bootstrapTable('load', vs[season]);
			$('#table-pair').bootstrapTable('load', pair[season]);
		    chart.destroy();
			createChart(pointsSeries, 'container-graph');
		    matchesFunction();
		    playersFunction();
			$('#table-scorers').bootstrapTable('load', scorers[season]);
		    
  	    }
    	
    };
  
	var select = $("#season-selector");
	for (var prop in options.permanents) {
		select.prepend($('<option '+(prop=='2018-2019'?' selected="selected"':'')+'/>').val(prop).text("Temporada "+prop));
	}
	season = $("#season-selector").val();
	select.on('change', function() {
		changeSeason(this.value);
	});
    
	if(window.location.pathname.includes('/me')){
		
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
	    
    	selectedSeasonMatches = matches[season];
	    numMatches = selectedSeasonMatches.length;
	    currentMatch = numMatches-1;
	    
	    $('#table-player-matches').bootstrapTable({
	        data: userMatches[season],
	        locale:'es-ES'
	    });
		if(pointsSeries){ createChart(pointsSeries, 'container-player-graph'); }
    	$('#points[data-toggle="tooltip"]').tooltip();
	    
	}else{
		
	    matchesFunction();
	    playersFunction();
    	
	    $('#table-full').bootstrapTable({
	        data: fullRanking[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-permanents').bootstrapTable({
	        data: permanentsRanking[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-substitutes').bootstrapTable({
	        data: substitutesRanking[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
		    
	    $('#table-vs').bootstrapTable({
	        data: vs[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-pair').bootstrapTable({
	        data: pair[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
	    $('#table-scorers').bootstrapTable({
	        data: scorers[season],
	        locale:'es-ES',
	        onAll: function(name, args){ updateUserName() }
	    });
		
	}
    
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