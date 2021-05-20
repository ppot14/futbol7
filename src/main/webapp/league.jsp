<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html prefix="og: http://ogp.me/ns#" lang="es">

<%@ include file="head.jsp" %>

  <body>
  
  <script>
	var fullRanking = <%=request.getAttribute("fullRanking")%>;
	var permanentsRanking = <%=request.getAttribute("permanentsRanking")%>;
	var substitutesRanking = <%=request.getAttribute("substitutesRanking")%>;
	var vs = <%=request.getAttribute("vs")%>;
	var pair = <%=request.getAttribute("pair")%>;
	var scorers = <%=request.getAttribute("scorers")%>;
	var matches = <%=request.getAttribute("matches")%>;
	var players = <%=request.getAttribute("players")%>;
	var pointsSeries = <%=request.getAttribute("userPointsSeries")%>;
	var mvps = <%=request.getAttribute("mvps")%>;
	var mvpsByDate = <%=request.getAttribute("mvpsByDate")%>;
  </script>

	<%@ include file="nav.jsp" %>
	
<%-- 	<%@ include file="legend-modal.jsp" %> --%>
	
	<%@ include file="login-modal.jsp" %>
	
<%-- 	<%@ include file="last-match-winner-modal.jsp" %> --%>
	
	<%@ include file="polling-modal.jsp" %>
	
	<div id="league-page" class="container-fluid">
		<h1 id="title" class="text-center"></h1>
				
		<div id="row1" class="row">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="card">
				  <h3 class="card-header">Clasificación Completa</h3>
				  <div class="card-body">
						<table id="table-full" data-sort-name="realPoints" data-sort-order="desc"  data-striped="true" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="name" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="trendingMatches" data-formatter="trendingFormat" data-sortable="false" data-align="center">T</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
<!-- 									<th data-field="points" data-sortable="true" data-align="right">Pts*</th> -->
									<th data-field="wins" data-sortable="true" data-align="right">G</th>
									<th data-field="draws" data-sortable="true" data-align="right">E</th>
									<th data-field="loses" data-sortable="true" data-align="right">P</th>
									<th data-field="matches" data-sortable="true" data-align="right">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Gol</span></th>
<!-- 									<th data-field="MVPs" data-sortable="true" data-align="right">MVP</th> -->
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
		</div>
		
		<div id="row2" class="row">
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="card">
				   <h3 class="card-header">Titulares</h3>
				  <div class="card-body">
						<table id="table-permanents" data-sort-name="realPoints" data-sort-order="desc" data-striped="true" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="name" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="trendingMatches" data-formatter="trendingFormat" data-sortable="false" data-align="center">T</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
<!-- 									<th data-field="points" data-sortable="true" data-align="right">Pts*</th> -->
									<th data-field="wins" data-sortable="true" data-align="right">G</th>
									<th data-field="draws" data-sortable="true" data-align="right">E</th>
									<th data-field="loses" data-sortable="true" data-align="right">P</th>
									<th data-field="matches" data-sortable="true" data-align="right">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Gol</span></th>
<!-- 									<th data-field="MVPs" data-sortable="true" data-align="right">MVP</th> -->
								</tr>
							</thead>
						</table>
					</div>
				</div>
			</div>
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="card">
				  <h3 class="card-header">Suplentes</h3>
				  <div class="card-body">
						<table id="table-substitutes" data-sort-name="realPoints" data-sort-order="desc" data-striped="true" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="name" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="trendingMatches" data-formatter="trendingFormat" data-sortable="false" data-align="center">T</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
<!-- 									<th data-field="points" data-sortable="true" data-align="right">Pts*</th> -->
									<th data-field="wins" data-sortable="true" data-align="right">G</th>
									<th data-field="draws" data-sortable="true" data-align="right">E</th>
									<th data-field="loses" data-sortable="true" data-align="right">P</th>
									<th data-field="matches" data-sortable="true" data-align="right">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimalFormatter"><span style="text-decoration:overline">Gol</span></th>
<!-- 									<th data-field="MVPs" data-sortable="true" data-align="right">MVP</th> -->
								</tr>
							</thead>
						</table>
					</div>
				</div>
			</div>
		</div>
			
		<div id="row3" class="row">
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="card">
				  <h3 class="card-header">Jornadas</h3>
				  <div class="card-body">
						<div id="table-results-header" style="margin-button: 10px;">
							<button id="previous-match" class="btn btn-primary float-left">&lt;</button>
							<span class="float-center">Jornada X - Dia Y</span>
							<button id="next-match" class="btn btn-primary float-right">&gt;</button>
						</div>
						<table id="table-results" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="blue" data-formatter="nameFormatter"  class="column-name">Equipo Azul <span class="float-right"></span></th>
									<th data-field="white" data-formatter="nameFormatter"  class="column-name">Equipo Blanco <span class="float-right"></span></th>
								</tr>
							</thead>
						</table>
					</div>
				</div>			
			
			</div>
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="card">
				  <h3 class="card-header">Goleadores</h3>
				  <div class="card-body">
						<table id="table-scorers" data-sort-name="scores" data-sort-order="desc" data-striped="true" data-pagination="true" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="name" data-formatter="nameFormatter" data-sortable="true"  class="column-name">Nombre</th>
									<th data-field="scores" data-sortable="true" data-align="right">Goles</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
		</div>
		
		<div id="row4" class="row">
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
			
				<div id="" class="card">
				  <h3 class="card-header">Comparación</h3>
				  <div class="card-body">
				  		<table id="table-comparison" class="table table-hover table-bordered" class="table table-striped table-md">
							<thead>
								<tr>
									<th><select id="player-one" name="player-one" class="form-control"></select></th>
									<th><select id="player-two" name="player-one" class="form-control"></select></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
						<div class="comparison-content">
							<div id="comparison-message"></div>
							<div id="comparison-pictures" class="row">
								<div class="col comparison-player-one" id="comparison-player-one"><h3 class="text-center"></h3><img class="rounded mx-auto"/></div>
								<div class="col" ><h6>VS</h6><div id="players-circle-graph" style="margin: 0 auto"></div></div>
								<div class="col comparison-player-two" id="comparison-player-two"><h3 class="text-center"></h3><img class="rounded mx-auto"/></div>
							</div>
							<div class="comparison-title"><h5>Enfrentamientos</h5></div>
							<div id="comparison-matches" class="row">
								<div class="col comparison-player-one" id="comparison-player-one-win"><span></span><h4></h4></div>
								<div class="col comparison-player-draw" id="comparison-player-draw"><span></span><h4></h4></div>
								<div class="col comparison-player-two" id="comparison-player-two-win" ><span></span><h4></h4></div>
							</div>
							<div class="comparison-title"><h5>Clasificación</h5></div>
							<div id="comparison-ranking" class="row">
								<div class="col comparison-player-one" id="comparison-player-one-position"><span>Puesto</span><h5></h5></div>
								<div class="col" ><h6>&nbsp;</h6></div>
								<div class="col comparison-player-two" id="comparison-player-two-position"><span>Puesto</span><h5></h5></div>
							</div>
						</div>
						<div id="players-spider-graph" style="min-width: 310px; height: 400px; margin: 0 auto"><h5 class="text-center">Selecciona dos jugadores diferentes para compararlos</h5></div>
						<div class="comparison-content">
							<div class="comparison-title"><h5>Jugando en el mismo equipo</h5></div>
							<div id="comparison-same-team" class="row">
								<div class="col" id="comparison-players-win"><span class="text-success"></span><h6 class="text-success"></h6></div>
								<div class="col" id="comparison-players-draw"><span class="text-warning"></span><h6 class="text-warning"></h6></div>
								<div class="col" id="comparison-players-lose"><span class="text-danger"></span><h6 class="text-danger"></h6></div>
							</div>
						</div>
				  </div>
				</div>
				
			</div>	
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4" >
			
				<div id="" class="card">
				  <h3 class="card-header">Veces como adversarios</h3>
				  <div class="card-body">
						<table id="table-vs" data-sort-name="vs" data-sort-order="desc" data-pagination="true"  class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="player1" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="player2" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="vs" data-sortable="true" data-align="right">J</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4" >
			
				<div id="" class="card">
				  <h3 class="card-header">Veces en el mismo equipo</h3>
				  <div class="card-body">
						<table id="table-pair" data-sort-name="pair" data-sort-order="desc" data-pagination="true" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="player1" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="player2" data-formatter="nameFormatter" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="pair" data-sortable="true" data-align="right">J</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>	
		</div>
		
		<div id="row5" class="row">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="card">
				  <h3 class="card-header">Evolución de Puntos</h3>
				  <div class="card-body">
				  		<div id="container-graph" style="min-width: 310px; height: 600px; margin: 0 auto"></div>
					</div>
				</div>
			
			</div>
		</div>
		
<!-- 		<div id="row6" class="row"> -->
<!-- 			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12"> -->
			
<!-- 				<div id="" class="card"> -->
<!-- 				  <h3 class="card-header">Distribución goles-puntos</h3> -->
<!-- 				  <div class="card-body"> -->
<!-- 				  		<div id="scores-points-graph" style="min-width: 310px; height: 600px; margin: 0 auto"></div> -->
<!-- 					</div> -->
<!-- 				</div> -->
			
<!-- 			</div> -->
<!-- 		</div> -->
		
	</div>
	
	<script>
		if('serviceWorker' in navigator) {
		  navigator.serviceWorker
		           .register('/sw.js')
		           .then(function() { console.log("Service Worker Registered"); });
		}
	</script>
  </body>
</html>
