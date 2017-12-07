<!-- Legend -->
	<div class="modal fade" id="help" tabindex="-1" role="dialog" aria-labelledby="modalLegend">
	  <div class="modal-dialog" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
	        <h4 class="modal-title" id="modalLegend">Ayuda</h4>
	      </div>
	      <div class="modal-body">
	      <h4>Leyenda</h4>
	      <ul>
	      	<li>UP - Últimos partidos ('V' Victoria, 'E' Empate, 'D' Derrota y '-' No ha jugado)</li>
	      	<li>Pts - Puntos (3 puntos victoria, 2 empate, 1 derrota y  0 no jugados)</li>
	      	<li>Pts* - Puntos (3 puntos victoria, 1 empate, 0 derrota y no jugados)</li>
	      	<li>G - Partidos Ganados</li>
	      	<li>E - Partidos Empatados</li>
	      	<li>P - Partidos Perdidos</li>
	      	<li>J - Partidos Jugados</li>
	      	<li>Gol - Goles</li>
	      	<li><span style="text-decoration:overline">Pts</span> - Media de Puntos (con al menos 25% partidos jugados)</li>
	      	<li><span style="text-decoration:overline">Gol</span> - Media de Goles (con al menos 25% partidos jugados)</li>
	      	<li><span style="text-decoration:overline">Pun</span> - Puntuación media de las votaciones</li>
	      </ul>
	      <h4>Como votar</h4>
	      <ol>
	      	<li>Registrarse o iniciar sesión usando Facebook o Gmail en el botton 
	      		<button class="btn btn-default btn-sm"><span class="glyphicon glyphicon-user" aria-hidden="true"></span></button>
	      	</li>
	      	<li>Solicitar al administrador que asigne su nombre al registro de Facebook o Gmail, si no se ha hecho ya.
	      	</li>
	      	<li>Si el jugador ha jugado el último partido, no ha votado aún y no han pasado las 12:00 del 5º dia, verá el botón 
	      		<button class="btn btn-danger btn-sm"><span class="glyphicon glyphicon-envelope" aria-hidden="true"></span></button>
	      		donde podrá votar. El sistema de puntuación va de 1 a 10 puntos utilizando la barra de desplazamiento. 
	      		Los comentarios admiten hasta 1000 caracteres para disfrute del Sr Pulido. Cada jugador puede asignar un título
	      		a otro jugador de manera opcional entre 5 títulos disponibles. Al final del plazo de votación el/los jugador/es
	      		más votados obtendrán el título para ese partido. A parte de esos trés títulos están los títulos de MVP
	      		<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al MVP"><i class="fa fa-trophy" aria-hidden="true"></i></div>
	      		, jugador
	      		mejor valorado y el de WVP
	      		<div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al Cabra"><i class="fa fa-thumbs-down" aria-hidden="true"></i></div>
	      		, jugador peor valorado.
		      <ul>
		      	<li><div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más chupón"><i class="fa fa-undo" aria-hidden="true"></i></div>
		      	El Trompito. Título al más chupón del partido, ese que es capaz de recorrer los cuatro corners con el balon
		      	en los pies antes de tirar a puerta. Homenaje del Sr Bordas al Sr Rivera</li>
		      	<li><div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al jugador con más clase y elegante"><i class="fa fa-glass" aria-hidden="true"></i></div>
		      	El Dandy. Título al jugador con más clase y elegancia, ese que no necesita la posesión y deja unas asistencias
		      	a la derecha mirando a la izquierda que más quisiera Michael Laudrup. Homenaje a Sr Revuelta</li>
		      	<li><div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más guarro"><i class="fa fa-wheelchair-alt" aria-hidden="true"></i></div>
		      	El Francés. Título al más guarro, ese que es capaz de dejarte un cardenal del tamaño del obispo de Jerez durante
		      	temporada y media y encima dice que no es falta. En honor al Sr Mativet</li>
		      	<li><div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más impuntual e impresentable"><i class="fa fa-clock-o" aria-hidden="true"></i></div>
		      	El Sillegas. Título al más impuntual e impresentable, ese que es capaz de buscar atascos en Google Maps para
		      	no llegar a su hora aunque trabaje menos que un funcionario de la junta. Homenaje a Sr Pozo y Sr Villegas</li>
		      	<li><div class="player-badge" data-toggle="tooltip" data-placement="bottom" title="Título al más protestón y bocazas"><i class="fa fa-bullhorn" aria-hidden="true"></i></div>
		      	El Porculero. Título al más protestón y bocazas, ese que es capaz de pelearse hasta con los del equipo contrario,
		      	que protesta hasta cuando tiene el balón o que tiene a su equipo desquiciado aúnque ganen 10-0. Honorífico a los 
		      	Sr Bordas, Sr Tristán y Sr Rivera</li>
		      </ul>
	      	</li>
	      	<li>Presione el botón <button class="btn btn-danger btn-sm">Votar</button> para enviar la votación. Esta opción no tiene vuelta atrás
	      	</li>
	      </ol>
	      <h4>Como ver la última votación</h4>
	      <ul>
	      	<li>La votación del último partido está disponible desde las 12:00 del 5º día del partido y durante 7 días. 
	      	Para los patidos del lunes estará disponible el viernes a las 12:00 de la mañana hasta las 12:00 del viernes siguiente</li>
	   		<li>Para ver la votación debe presionar el botón 
	        <button type="button" class="btn btn-default btn-sm" ><i class="fa fa-shield" aria-hidden="true"></i></button>.
	        Dicho botón estará en VERDIBLANCO cuando el resultado sea nuevo
	        <button type="button" class="btn btn-success btn-sm" ><i class="fa fa-shield" aria-hidden="true"></i></button></li>
	      </div>
<!-- 	      <div class="modal-footer"> -->
<!-- 	        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button> -->
<!-- 	        <button type="button" class="btn btn-primary">Save changes</button> -->
<!-- 	      </div> -->
	    </div>
	  </div>
	</div>