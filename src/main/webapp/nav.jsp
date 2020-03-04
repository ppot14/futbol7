	<nav class="navbar navbar-expand-lg navbar-dark bg-dark" id="top-navbar">
	  <div class="container-fluid">
	      
	    <a id="web-name" class="navbar-brand h1 mb-0" href="<%=request.getContextPath()+"/"%>">
	      	<img id="logo" alt="Futbol7.pro logo" title="Futbol7.pro logo" src="resources/images/logo50.png" class="d-inline-block align-top"/>
	      	Futbol 7
	    </a>
		<button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbar-collapse-id" aria-controls="navbar-collapse-id" aria-expanded="false" aria-label="Toggle navigation">
			 <span class="navbar-toggler-icon"></span>
		</button>
	    
	    <div class="collapse navbar-collapse justify-content-end" id="navbar-collapse-id">
	      <ul id="menu-list-2" class="navbar-nav">
			<li id="refresh" class="nav-item" style="display:none">
				<a class="nav-link"><i class="fa fa-refresh" aria-hidden="true"></i>&nbsp; Actualizar datos</a>
			</li>
			<li id="user-menu-item" class="nav-item" style="display:none">
				<a class="nav-link">
					<img id="player-picture" alt="player picture" class="user-picture rounded"/>&nbsp; 
					<span id="player-name" class="user-name"></span>
				</a>
			</li>
			<li id="logout-button" class="nav-item" style="display:none">
	            <a class="nav-link"><i class="fa fa-sign-out" aria-hidden="true"></i>&nbsp; Salir</a>
			</li>
			<li id="login-button" class="nav-item" data-toggle="modal" data-target="#login-selector">
				<a class="nav-link"><i class="fa fa-user" aria-hidden="true"></i>&nbsp; Entrar</a>
			</li>
	      </ul>
	<%
	final Boolean enableVote = (Boolean) request.getAttribute("enableVote");
	if(enableVote!=null && enableVote){
		%>
	      <ul id="menu-list-1" class="nav navbar-nav navbar-right">
	      	<li id="notification-score-button" class="" data-toggle="modal" data-target="#polling" style="display:none">
				<a class="navbar-link"><i class="fa fa-envelope" aria-hidden="true"></i>&nbsp; Votar</a>
			</li><li id="last-match-winner-button" class="navbar-link" data-toggle="modal" data-target="#last-match-winner">
	            <a class="navbar-link"><i class="fa fa-shield" aria-hidden="true"></i>&nbsp; Votación último partido</a>
			</li>
	      </ul>
	<%}%>
	    </div>
	  </div>
	</nav>
	
	<div id="notification-bar" class="alert" role="alert" style="display:none"></div>