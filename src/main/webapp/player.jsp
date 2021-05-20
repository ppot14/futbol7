<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html prefix="og: http://ogp.me/ns#" lang="es">

<%@ include file="head.jsp" %>

  <body>
  
  <script>
	var matches = <%=request.getAttribute("matches")%>;
	var userMatches = <%=request.getAttribute("userMatches")%>;
	var pointsSeries = <%=request.getAttribute("userPointsSeries")%>;
	var mvps = <%=request.getAttribute("mvps")%>;
  </script>

	<%@ include file="nav.jsp" %>
	
<%-- 	<%@ include file="legend-modal.jsp" %> --%>
	
	<%@ include file="login-modal.jsp" %>
	
<%-- 	<%@ include file="last-match-winner-modal.jsp" %> --%>
	
<%-- 	<%@ include file="polling-modal.jsp" %> --%>
	
	<div id="me-page" class="container-fluid">
		<div id="row1" class="row">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
				<h2 id="title" class="text-center"></h2>
			</div>
			<div class="col-xs-12 col-sm-3 col-md-2 col-lg-2">
				<img id="user-picture" class="user-picture rounded">
			</div>
			<div class="col-xs-12 col-sm-9 col-md-4 col-lg-4">
				<h1 id="user-name" class="user-name"></h1>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-2 col-lg-2">
				<ul class="list-group list-group-player">
					<li class="list-group-item">Puntos <span id="points" class="float-right badge badge-primary badge-pill"  data-toggle="tooltip" data-placement="bottom" title="Puntuación: Ganado = 3pts, Empatado = 1pts, Perdido = 0pts, No jugado = 0pts (Puntuación antigua entre paréntesis 3-2-1-0)"></span></li>
					<li class="list-group-item">P. Jugados <span id="matches" class="float-right badge badge-info badge-pill"></span></li>
					<li class="list-group-item">Goles <span id="goals" class="float-right badge badge-dark badge-pill"></span></li>
				</ul>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-2 col-lg-2">
				<ul class="list-group list-group-player">
					<li class="list-group-item">P. Ganados <span id="win" class="float-right badge badge-success badge-pill"></span></li>
					<li class="list-group-item">P. Empatados <span id="draw" class="float-right badge badge-warning badge-pill"></span></li>
					<li class="list-group-item">P. Perdidos <span id="lose" class="float-right badge badge-danger badge-pill"></span></li>
				</ul>
			</div>
			<div class="col-xs-4 col-sm-4 col-md-2 col-lg-2">
				<ul class="list-group list-group-player">
<!-- 					<li class="list-group-item">MVPs <span id="mvps" class="float-right"></span></li> -->
					<li class="list-group-item">Media Puntos <span id="avg-points" class="float-right"></span></li>
					<li class="list-group-item">Media Goles <span id="avg-goals" class="float-right"></span></li>
				</ul>
			</div>
		</div>
		<div id="row2" class="row">
			<div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
			
				<div id="" class="card">
				  <h3 class="card-header">Partidos</h3>
				  <div class="card-body">
						<table id="table-player-matches" data-sort-name=date data-sort-order="desc" data-striped="true" data-row-style="rowStyle" class="table table-striped table-md">
							<thead>
								<tr>
									<th data-field="date" data-sortable="true" data-sorter="dateSort" data-align="left">Fecha</th>
									<th data-field="team" data-formatter="teamFormatter" data-sortable="false" data-align="center">Equipo</th>
									<th data-field="result" data-formatter="scoreFormatter" data-cell-style="scoreStyle" data-align="center">Resultado</th>
									<th data-field="goals" data-formatter="goalsFormatter" data-sortable="true" data-align="right">Goles</th>
									<th data-field="mvps" data-formatter="mvpsFormatter" data-sortable="true" data-align="right">MVP</th>
<!-- 									<th data-field="score" data-formatter="userScoreFormatter" data-sortable="true" data-align="right">Puntuación</th> -->
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
				    <h3 class="panel-title">Evolución de Puntos</h3>
				  </div>
				  <div class="panel-body">
				  		<div id="container-player-graph" style="min-width: 310px; height: 600px; margin: 0 auto"></div>
					</div>
				</div>
			
			</div>
			
		</div>
	</div>
		
  </body>
</html>
