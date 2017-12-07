	<nav class="navbar navbar-inverse" id="top-navbar">
	  <div class="container-fluid">
	    <div class="navbar-header">
<!-- 	      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse" aria-expanded="false"> -->
<!-- 	        <span class="sr-only">Toggle navigation</span> -->
<!-- 	        <span class="icon-bar"></span> -->
<!-- 	        <span class="icon-bar"></span> -->
<!-- 	        <span class="icon-bar"></span> -->
<!-- 	      </button> -->
	      <img id="logo" alt="Futbol7.pro logo" src="resources/images/logo50.png"/>
	      <a id="web-name" class="navbar-brand" href="<%=request.getContextPath()+"/"%>">Futbol 7</a>
<!-- 	      <form class="navbar-form navbar-right" role="search"> -->
<!-- 	        <div class="form-group"> -->
<!-- 	        </div> -->
<!-- 	      </form> -->
		  <form class="navbar-form navbar-right">
  			 <div class="form-group">
	            <button id="notification-score-button" type="button" class="btn btn-danger" data-toggle="modal" data-target="#polling" style="display:none">
				  <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>
				</button>
	            <button id="last-match-winner-button" type="button" class="btn btn-default" data-toggle="modal" data-target="#last-match-winner">
				  <i class="fa fa-shield" aria-hidden="true"></i>
				</button>
		  		<select id="season-selector" class="form-control"></select>
	            <button id="refresh" type="button" class="btn btn-info" style="display:none">
				  <span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
				</button>
				<a id="player-name" class="user-name btn btn-link" style="display:none" href="me">
				</a>
				<img id="player-picture" alt="player picture" class="user-picture img-rounded" style="display:none"/>
	            <button id="logout-button" type="button" class="btn btn-default" style="display:none">
				  <span class="glyphicon glyphicon-log-out" aria-hidden="true"></span>
				</button>
	            <button id="login-button" type="button" class="btn btn-default" data-toggle="modal" data-target="#login-selector">
				  <span class="glyphicon glyphicon-user" aria-hidden="true"></span>
				</button>
	            <button type="button" class="btn btn-link" data-toggle="modal" data-target="#help">
				  <span class="glyphicon glyphicon-question-sign" aria-hidden="true"></span>
				</button>
		     </div>
		  </form>
		  
	      <button id="notification-bar" type="button" class="btn btn-info pull-right" style="margin-top: 8px; display:none"></button>
	    	
	    </div>
	    
<!-- 	    <div class="collapse navbar-collapse" id="navbar-collapse"> -->
<!-- 	      <ul id="menu-list" class="nav navbar-nav"> -->
	      
<!-- 	      </ul> -->
<!-- 	    </div> -->
	  </div>
	</nav>