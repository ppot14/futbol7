  <head>
<% 
	final boolean minimized = request!=null && request.getAttribute("minimized")!=null? (Boolean) request.getAttribute("minimized") : false; 
	final boolean production = request!=null && request.getAttribute("production")!=null? (Boolean) request.getAttribute("production") : true;
	final String league = request!=null && request.getAttribute("league")!=null? (String) request.getAttribute("league") : "";
	final String player = request!=null && request.getAttribute("player")!=null? (String) request.getAttribute("player") : "";
%>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
	<meta name="description" content="Futbol7.pro<%=!"".equals(league)?" - Liga "+league:""%><%=!"".equals(player)?" - Jugador: "+player:""%> - página de la hermandad de las eternas promesas que dieronse a la cerveza.">
	<meta content="futbol7, san jeronimo, tabladilla" name="keywords"/>
	
	<meta property="fb:app_id" content="1606586212720990"/>
	<meta property="og:title" content="Futbol7.pro<%=!"".equals(league)?" - "+league:""%><%=!"".equals(player)?" - "+player:""%>" />
	<meta property="og:type" content="website" />
	<meta property="og:description" content="Futbol7.pro<%=!"".equals(league)?" - Liga "+league:""%><%=!"".equals(player)?" - Jugador: "+player:""%> - página de la hermandad de las eternas promesas que dieronse a la cerveza." />
	<meta property="og:locale" content="es_ES" />
	<meta property="og:url" content="https://www.futbol7.pro" />
	<meta property="og:image" content="https://www.futbol7.pro/resources/images/marchena.jpg" />
	<meta property="og:image:type" content="image/jpeg" />
	<meta property="og:image:width" content="1439" />
	<meta property="og:image:height" content="920" />
	
<!-- 	<meta name="google-signin-client_id" content="517911210517-uupk9pfrbcsnce7qblohvpmog4hc6bfu.apps.googleusercontent.com"> -->
    
    <title>Futbol7.pro<%=!"".equals(league)?" - "+league:""%><%=!"".equals(player)?" - "+player:""%></title>
  
	<%if(!minimized){%>
	<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" media="all"/>
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/bootstrap-table.min.css" media="all"/>
	<link rel="stylesheet" type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" media="all"/>
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/9.9.0/css/bootstrap-slider.min.css" media="all"/>
	
	<link rel="stylesheet" type="text/css" href="resources/styles/main.css" media="all"/>
	<%}else{%>
<!-- 	<link rel="stylesheet" type="text/css" href="resources/styles/merged.min.css" media="all"/> -->
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
	<script src="https://apis.google.com/js/platform.js" defer></script>
	<script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" defer></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/bootstrap-table.min.js" defer></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-table/1.9.1/locale/bootstrap-table-es-ES.min.js" defer></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-slider/9.9.0/bootstrap-slider.min.js" defer></script>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-timeago/1.6.1/jquery.timeago.min.js" defer></script>
	<script src="https://code.highcharts.com/highcharts.js" defer></script>
<!-- 	<script src="https://code.highcharts.com/highcharts-more.js"></script> -->

	<script src="resources/scripts/main.js" defer></script>
	<script src="resources/scripts/login.js" defer></script>
	<%}else{%>
<!-- 	<script src="resources/scripts/merged.min.js" defer></script> -->
	<%}%>
	
	<link rel="shortcut icon" href="resources/images/favicon.ico" />
	<link rel="apple-touch-icon" sizes="180x180" href="resources/images/apple-touch-icon.png">
	<link rel="icon" type="image/png" sizes="120x120" href="resources/images/favicon120.png">
	<link rel="icon" type="image/png" sizes="32x32" href="resources/images/favicon32.png">
	<link rel="icon" type="image/png" sizes="16x16" href="resources/images/favicon16.png">
	<link rel="mask-icon" href="resources/images/safari-pinned-tab.svg" color="#00a300">
	<link rel="manifest" href="/manifest.json">
	<meta name="theme-color" content="#ffffff">	
	<meta name="msapplication-TileColor" content="#00a300">
	<meta name="apple-mobile-web-app-capable" content="yes" />
	<meta name="apple-mobile-web-app-status-bar-style" content="#00a300" />
	<meta name="mobile-web-app-capable" content="yes" />
  </head>