	<nav class="navbar navbar-inverse" id="top-navbar">
	  <div class="container-fluid">
	    <div class="navbar-header">
	      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse-id" aria-expanded="false">
	        <span class="sr-only">Toggle navigation</span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	      </button>
	      <img id="logo" alt="Futbol7.pro logo" title="Futbol7.pro logo" src="resources/images/logo50.png"/>
	      <a id="web-name" class="navbar-brand" href="<%=request.getContextPath()+"/"%>">Futbol 7</a>
<!-- 	      <form class="navbar-form navbar-right" role="search"> -->
<!-- 	        <div class="form-group"> -->
<!-- 	        </div> -->
<!-- 	      </form> -->
<!-- 		  <form class="navbar-form navbar-right"> -->
<!--   			 <div class="form-group"> -->
<!-- 		     </div> -->
<!-- 		  </form> -->
	    	
	    </div>
	    
	    <div class="collapse navbar-collapse" id="navbar-collapse-id">
	      <ul id="menu-list-2" class="nav navbar-nav navbar-right">
			<li id="refresh" class="" style="display:none">
				<a class="navbar-link"><i class="fa fa-refresh" aria-hidden="true"></i>&nbsp; Actualizar datos</a>
			</li>
			<li id="user-menu-item" style="display:none">
				<a class="navbar-link" href="me">
					<img id="player-picture" alt="player picture" class="user-picture img-rounded"/>&nbsp; 
					<span id="player-name" class="user-name"></span>
				</a>
			</li>
			<li id="logout-button" style="display:none">
	            <a class="navbar-link"><i class="fa fa-sign-out" aria-hidden="true"></i>&nbsp; Salir</a>
			</li>
			<li id="login-button" class="" data-toggle="modal" data-target="#login-selector">
				<a class="navbar-link"><i class="fa fa-user" aria-hidden="true"></i>&nbsp; Entrar</a>
			</li>
<!-- 			<li class="" data-toggle="modal" data-target="#help"> -->
<!-- 				<a class="navbar-link"><i class="fa fa-question-circle" aria-hidden="true"></i>&nbsp; Ayuda</a> -->
<!-- 			</li> -->
	      </ul>
<!-- 		  <form class="navbar-form navbar-right"> -->
<!--         	<div class="form-group"> -->
<!-- 	  			<select id="season-selector" class="form-control"></select> -->
<!--         	</div> -->
<!-- 	  	  </form> -->
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