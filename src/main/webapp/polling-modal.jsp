	<!-- Polling -->
	<div class="modal fade" id="polling" tabindex="-1" role="dialog" aria-labelledby="modalPolling">
	  <div class="modal-dialog modal-lg" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h4 class="modal-title" id="modalPolling">Votación del MVP del partido de <span class="last-match-date"></span>. La votación acaba <span class="polling-close-date text-dange"></span></h4>
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	      </div>
	      <div class="modal-body">
	      	<div id="polling-result-header" class="row">
	      		<div class="text-left">
	      			<h3><i class="fa fa-shield fa text-blue-team blue-team-background team-shield" aria-hidden="true"></i> Azules <span class="pull-right blue-score"></span></h3>
					<ul class="blue-scorers"></ul>     		
	      		</div>
	      		<div class="text-right">
	      			<h3 style="text-align: right;"><span class="pull-left white-score"></span> Blancos <i class="fa fa-shield fa text-white-team white-team-background team-shield" aria-hidden="true"></i></h3>
					<ul class="white-scorers"></ul>
	      		</div>
	      	</div>
	      	<div class="row">
		      	<form class="form-horizontal" id="polling-form">
					<div class="form-group">
					    <button type="submit" class="btn btn-danger btn-lg btn-block">Votar</button>
					</div>
				</form>
	      	</div>
	      </div>
	    </div>
	  </div>
	</div>	