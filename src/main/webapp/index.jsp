<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html prefix="og: http://ogp.me/ns#" lang="es">

<%@ include file="head.jsp" %>

  <body>
  
  <script>
	var fullRanking = <%=request.getAttribute("fullRanking")%>;
	var permanentsRanking = <%=request.getAttribute("permanentsRanking")%>;
	var substitutesRanking = <%=request.getAttribute("substitutesRanking")%>;
	var playersPictures = <%=request.getAttribute("playersPictures")%>;
	var vs = <%=request.getAttribute("vs")%>;
	var pair = <%=request.getAttribute("pair")%>;
	var scorers = <%=request.getAttribute("scorers")%>;
	var matches = <%=request.getAttribute("matches")%>;
	var players = <%=request.getAttribute("players")%>;
	var pointsSeries = <%=request.getAttribute("userPointsSeries")%>;
  </script>

	<%@ include file="nav.jsp" %>
	
	<%@ include file="legend-modal.jsp" %>
	
	<%@ include file="login-modal.jsp" %>
	
	<%@ include file="last-match-winner-modal.jsp" %>
	
	<%@ include file="polling-modal.jsp" %>
	
	<div id="home-page" class="container-fluid">
		<div id="row1" class="row" style="display:none">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Clasificación Completa</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-full" data-sort-name="realPoints" data-sort-order="desc"  data-striped="true">
							<thead>
								<tr>
									<th data-field="name" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
									<th data-field="points" data-sortable="true" data-align="right" data-visible="false">Pts*</th>
									<th data-field="wins" data-sortable="true" data-align="right" data-visible="false">G</th>
									<th data-field="draws" data-sortable="true" data-align="right" data-visible="false">E</th>
									<th data-field="loses" data-sortable="true" data-align="right" data-visible="false">P</th>
									<th data-field="matches" data-sortable="true" data-align="right" data-visible="false">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right" data-visible="false">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Gol</span></th>
									<th data-field="scoreAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pun</span></th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
		</div>
			
		<div id="row2" class="row">
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Titulares</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-permanents" data-sort-name="realPoints" data-sort-order="desc" data-striped="true">
							<thead>
								<tr>
									<th data-field="name" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
									<th data-field="points" data-sortable="true" data-align="right" data-visible="false">Pts*</th>
									<th data-field="wins" data-sortable="true" data-align="right" data-visible="false">G</th>
									<th data-field="draws" data-sortable="true" data-align="right" data-visible="false">E</th>
									<th data-field="loses" data-sortable="true" data-align="right" data-visible="false">P</th>
									<th data-field="matches" data-sortable="true" data-align="right" data-visible="false">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right" data-visible="false">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Gol</span></th>
									<th data-field="scoreAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pun</span></th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Jornadas</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-results">
							<thead>
								<tr>
									<th colspan="2"><span>Jornada X - Dia Y</span>
						            <button id="match-winner-button" type="button" class="btn btn-default btn-xs pull-right" data-match-day="0" data-toggle="modal" data-target="#last-match-winner">
									  <i class="fa fa-shield" aria-hidden="true"></i>
									</button>
									</th>
								</tr>
								<tr>
									<th data-field="blue" >Equipo Azul <span class="pull-right"></span></th>
									<th data-field="white" >Equipo Blanco <span class="pull-right"></span></th>
								</tr>
							</thead>
							<tfoot>
								<tr>
									<th><button id="previous-match" class="btn pull-left">Anterior</button></th>
									<th><button id="next-match" class="btn pull-right">Siguiente</button></th>
								</tr>
							</tfoot>
						</table>
					</div>
				</div>			
			
			</div>
			<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Suplentes</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-substitutes" data-sort-name="realPoints" data-sort-order="desc" data-striped="true">
							<thead>
								<tr>
									<th data-field="name" data-sortable="true" class="column-name">Nombre</th>
									<th data-field="lastMatches" data-formatter="lastMatchesFormat" data-sortable="false" data-align="center">UP</th>
									<th data-field="realPoints" data-sortable="true" data-align="right">Pts</th>
									<th data-field="points" data-sortable="true" data-align="right" data-visible="false">Pts*</th>
									<th data-field="wins" data-sortable="true" data-align="right" data-visible="false">G</th>
									<th data-field="draws" data-sortable="true" data-align="right" data-visible="false">E</th>
									<th data-field="loses" data-sortable="true" data-align="right" data-visible="false">P</th>
									<th data-field="matches" data-sortable="true" data-align="right" data-visible="false">J</th>
									<th data-field="goalsFor" data-sortable="true" data-align="right" data-visible="false">Gol</th>
									<th data-field="pointsAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pts</span></th>
									<th data-field="goalsForAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Gol</span></th>
									<th data-field="scoreAVG" data-sortable="true" data-align="right" data-formatter="decimal" data-visible="false"><span style="text-decoration:overline">Pun</span></th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Goleadores</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-scorers" data-sort-name="scores" data-sort-order="desc" data-striped="true" data-pagination="true">
							<thead>
								<tr>
									<th data-field="name" data-sortable="true">Nombre</th>
									<th data-field="scores" data-sortable="true" data-align="right">Goles</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
		</div>
		
		<div id="row3" class="row" style="display:none">
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4" >
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Veces como adversarios</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-vs" data-sort-name="vs" data-sort-order="desc"
               data-pagination="true">
							<thead>
								<tr>
									<th data-field="player1" data-sortable="true">Nombre</th>
									<th data-field="player2" data-sortable="true">Nombre</th>
									<th data-field="vs" data-sortable="true" data-align="right">J</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4" >
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Veces en el mismo equipo</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-pair"data-sort-name="pair" data-sort-order="desc"
               data-pagination="true">
							<thead>
								<tr>
									<th data-field="player1" data-sortable="true">Nombre</th>
									<th data-field="player2" data-sortable="true">Nombre</th>
									<th data-field="pair" data-sortable="true" data-align="right">J</th>
								</tr>
							</thead>
						</table>
					</div>
				</div>
			
			</div>
			<div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Comparación</h3>
				  </div>
				  <div class="panel-body">
				  		<table id="table-comparison" class="table table-hover table-bordered">
							<thead>
								<tr>
									<th><select id="player-one" name="player-one" class="form-control"></select></th>
									<th><select id="player-two" name="player-one" class="form-control"></select></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
				  </div>
				</div>
				
			</div>		
		</div>
		
		<div id="row4" class="row" style="display:none">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Evolución de Puntos</h3>
				  </div>
				  <div class="panel-body">
				  		<div id="container-graph" style="min-width: 310px; height: 600px; margin: 0 auto"></div>
					</div>
				</div>
			
			</div>
		</div>
		
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
