  <head>
<% 
	final boolean minimized = (Boolean) request.getAttribute("minimized"); 
	final boolean production = (Boolean) request.getAttribute("production");
%>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="description" content="Futbol 7 San Jerónimo, página de la hermandad de las eternas promesas que dieronse a la cerveza">
	<meta content="futbol7, san jeronimo" name="keywords"/>
	
	<meta property="og:title" content="Futbol 7 San Jerónimo" />
	<meta property="og:type" content="website" />
	<meta property="og:description" content="Futbol 7 San Jerónimo, página de la hermandad de las eternas promesas que dieronse a la cerveza... y Sí, el de arriba a la izquierda es Carlos Marchena y jugó con nosotros" />
	<meta property="og:locale" content="es_ES" />
	<meta property="og:url" content="http://futbol7.pro" />
	<meta property="og:image" content="http://futbol7.pro/resources/images/marchena.jpg" />
	<meta property="og:image:type" content="image/jpeg" />
	<meta property="og:image:width" content="1439" />
	<meta property="og:image:height" content="920" />
	
<!-- 	<meta name="google-signin-client_id" content="517911210517-uupk9pfrbcsnce7qblohvpmog4hc6bfu.apps.googleusercontent.com"> -->
    
    <title>Futbol 7 San Jerónimo</title>
  
	<%if(!minimized){%>
	<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" />
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/bootstrap-table.min.css" />
	<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" />
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/9.9.0/css/bootstrap-slider.min.css" />
	
	<link rel="stylesheet" type="text/css" href="resources/styles/main.css" />
	<%}else{%>
	<link rel="stylesheet" type="text/css" href="resources/styles/merged.min.css" />
	<%}%>
	
	<%if(production){%>
	<script>
	  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
	  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
	  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
	  })(window,document,'script','https://www.google-analytics.com/analytics.js','ga');

	  ga('create', 'UA-45198141-3', 'auto');
	  ga('send', 'pageview');

	</script>
	<%}%>
	<script>
	<%
	Object user = session.getAttribute("user");
	if(user!=null){
		%>
		var user = '<%=((org.bson.Document) user).get("nameweb")%>';
	<%}%>
		var options = <%=request.getAttribute("options")%>;
	</script>
	
	<%if(!minimized){%>
	<script src="https://apis.google.com/js/platform.js"></script>
	<script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/bootstrap-table.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/locale/bootstrap-table-es-ES.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/9.9.0/bootstrap-slider.min.js"></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.6.1/jquery.timeago.min.js"></script>
	<script src="https://code.highcharts.com/highcharts.js"></script>
<!-- 	<script src="https://code.highcharts.com/highcharts-more.js"></script> -->

	<script src="resources/scripts/main.js"></script>
	<script src="resources/scripts/login.js"></script>
	<%}else{%>
	<script src="resources/scripts/merged.min.js"></script>
	<%}%>
	
<!-- 	<link rel="shortcut icon" href="resources/images/logo.ico" /> -->
	<link rel="apple-touch-icon" sizes="120x120" href="resources/images/favicon120.png">
	<link rel="icon" type="image/png" sizes="32x32" href="resources/images/favicon32.png">
	<link rel="icon" type="image/png" sizes="16x16" href="resources/images/favicon16.png">
	<link rel="manifest" href="/manifest.json">
	<meta name="theme-color" content="#222">	

  </head>