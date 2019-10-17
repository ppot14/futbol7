<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html prefix="og: http://ogp.me/ns#" lang="es">

<%@ include file="head.jsp" %>

  <body>
  
  <script>
  </script>

	<%@ include file="nav.jsp" %>
	
<%-- 	<%@ include file="legend-modal.jsp" %> --%>
	
	<%@ include file="login-modal.jsp" %>
	
	<div id="home-page" class="container-fluid">
			
		<h1>Ligas de esta temporada</h1>
		<div id="current-season-row" class="row">
		</div>
		
		<h1>Ligas pasadas</h1>
		<div id="past-season-row" class="row">
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
