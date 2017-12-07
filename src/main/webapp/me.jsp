<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html prefix="og: http://ogp.me/ns#" lang="es">

<%@ include file="head.jsp" %>

  <body>
  
  <script>

  </script>

	<%@ include file="nav.jsp" %>
	
	<%@ include file="legend-modal.jsp" %>
	
	<%@ include file="login-modal.jsp" %>
	
	<%@ include file="last-match-winner-modal.jsp" %>
	
	<%@ include file="polling-modal.jsp" %>
	
	<div id="me-page" class="container-fluid">
		<div id="row1" class="row">
			<div class="col-xs-12 col-sm-3 col-md-2 col-lg-2">
				<img id="user-picture" class="user-picture img-rounded">
			</div>
			<div class="col-xs-12 col-sm-9 col-md-6 col-lg-6">
				<h1 id="user-name" class="user-name"></h1>
			</div>
			<div class="col-xs-6 col-sm-6 col-md-2 col-lg-2">
				<ul class="list-unstyled">
					<li>Puntos <span id="points" class="pull-right"></span></li>
					<li>P. Ganados <span id="win" class="pull-right"></span></li>
					<li>P. Empatados <span id="draw" class="pull-right"></span></li>
					<li>P. Perdidos <span id="lose" class="pull-right"></span></li>
					<li>Goles <span id="goals" class="pull-right"></span></li>
				</ul>
			</div>
			<div class="col-xs-6 col-sm-6 col-md-2 col-lg-2">
				<ul class="list-unstyled">
					<li>P. Jugados <span id="matches" class="pull-right"></span></li>
					<li>Media Puntos <span id="avg-points" class="pull-right"></span></li>
					<li>Media Puntuación <span id="avg-scores" class="pull-right"></span></li>
					<li>Media Goles <span id="avg-goals" class="pull-right"></span></li>
				</ul>
			</div>
		</div>
		<div id="row2" class="row">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Partidos</h3>
				  </div>
				  <div class="panel-body">
						<table id="table-player-matches" data-sort-name=date data-sort-order="desc" data-striped="true" data-row-style="rowStyle">
							<thead>
								<tr>
									<th data-field="date" data-sortable="true" data-sorter="dateSort" data-align="left">Fecha</th>
									<th data-field="team" data-formatter="teamFormatter" data-sortable="false" data-align="center">Equipo</th>
									<th data-field="result" data-formatter="scoreFormatter" data-cell-style="scoreStyle" data-align="center">Resultado</th>
									<th data-field="goals" data-sortable="true" data-align="right">Goles</th>
									<th data-field="score" data-sortable="true" data-align="right">Puntuación</th>
<!-- 									<th data-field="titles" data-align="center">Títulos</th> -->
								</tr>
							</thead>
						</table>
					</div>
				</div>
				
			</div>
				
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="panel panel-default">
				  <div class="panel-heading">
				    <h3 class="panel-title">Evolución de Puntos y Goles</h3>
				  </div>
				  <div class="panel-body">
				  		<div id="container-player-graph" style="min-width: 310px; height: 600px; margin: 0 auto">Próximamente</div>
					</div>
				</div>
			
			</div>
			
		</div>
	</div>
		
  </body>
</html>
