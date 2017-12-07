	<!-- Polling -->
	<div class="modal fade" id="polling" tabindex="-1" role="dialog" aria-labelledby="modalPolling">
	  <div class="modal-dialog modal-lg" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modalPolling">Votación para el partido de <span class="last-match-date"></span>. La votación acaba <span class="polling-close-date text-dange"></span></h4>
	      </div>
	      <div class="modal-body">
	      	<div class="row">
	      		<div class="col-xs-6 col-sm-6 col-md-6 col-lg-6">
	      			<h3>Azules <span class="pull-right blue-score"></span></h3>
					<ul class="blue-scorers"></ul>     		
	      		</div>
	      		<div class="col-xs-6 col-sm-6 col-md-6 col-lg-6">
	      			<h3 style="text-align: right;"><span class="pull-left white-score"></span> Blancos</h3>
					<ul class="white-scorers"></ul>
	      		</div>
	      	</div>
	      	<div class="row">
		      	<form class="form-horizontal" id="polling-form">
					<div class="form-group">
					    <div class="col-md-offset-4 col-lg-offset-4 col-md-4 col-lg-4">
					      <button type="submit" class="btn btn-danger btn-lg btn-block">Votar</button>
					    </div>
					</div>
				</form>
	      	</div>
	      </div>
	    </div>
	  </div>
	</div>	