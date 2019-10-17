/**
 * login.js
 */
var usertype;
var pollingLimit = 4*24*60*60*1000 + 12*60*60*1000;//4 days and half. Friday at midday
var pollingReady = 23*60*60*1000;//Polling ready at 23:00
var matchScores;
var t2;
var hasVoted = false;

function facebookStatusChangeCallback(response) {
	if(response.status && response.status == 'connected'){
		FB.api("/me",
				{fields: "picture.type(large),email,name"},
				function(response) {
					loggedIn(response,'facebook');
				});
	}
}

function googleStatusChangeCallback(){
	var response = {name: auth2.currentUser.get().getBasicProfile().getName(),
				id: auth2.currentUser.get().getBasicProfile().getId(),
				picture: auth2.currentUser.get().getBasicProfile().getImageUrl()};
	loggedIn(response,'google');
}

function loggedOut(){
  	$('#notification-score-button').slideUp();
  	$('#logout-button').slideUp(function (){ $('#login-button').slideDown() });
  	$('#user-menu-item').slideUp();
  	$('#player-picture').slideUp();
  	$( "td:contains('"+nameweb+"')" ).css( "font-weight", "normal" );
	$( "tr:contains('"+nameweb+"')" ).removeClass( "info" );
	hideAdmin();
	nameweb = null;
	usertype = null;
	hasVoted = false;
}

function loggedIn(response,loginType) {
	var t0 = performance.now();
//	console.log("StatusChange, loggedIn " + (t0 - t2) + "ms");
    if (response && !response.error) {
    	
    	var data = {};
		if(loginType=='facebook'){
			data.facebook = response;
			data.id = response.id;
			data.name = response.name;
			data.picture = response.picture.data.url;
		}else if(loginType=='google'){
			data.google = response;
			data.id = response.id;
			data.name = response.name;
			data.picture = response.picture;
		}
		
  	  $('#player-name').text(data.name);
  	  $('#player-picture').attr("src",data.picture);
  	  $('#login-button').slideUp(function() {$('#logout-button').slideDown();});
  	  $('#user-menu-item').slideDown();
  	  $('#player-picture').slideDown();
	  $('#login-selector').modal('hide');
  	  
	  $.post(
			'api/login.request', 
			JSON.stringify(data), 
			function( data ) {
				if(data && data.nameweb){
				  	$('#player-name').text(data.nameweb);
    				nameweb = data.nameweb;
    				usertype = data.usertype;
    				showAdmin();
    				updateUserName();
				}else if(data && data.newuser){
					console.info("New user created: "+data.newuser);
				}else{
					console.warn("Unknown user: "+JSON.stringify(data));
				}
			}
	    );
    }
}

/**
 * Init
 * 
 * @returns
 */
$(function () {
	t2 = performance.now();
	window.fbAsyncInit = function() {
	  FB.init({
        appId      : '1606586212720990',
        status	   : true,
        xfbml      : true,
        version    : 'v2.10',
        cookie 	   : true
      });
      FB.AppEvents.logPageView(); 
      FB.getLoginStatus(function(response) {
    	  	facebookStatusChangeCallback(response);
      });
  };

  (function(d, s, id){
     var js, fjs = d.getElementsByTagName(s)[0];
     if (d.getElementById(id)) {return;}
     js = d.createElement(s); js.id = id;
     js.src = "//connect.facebook.net/en_US/sdk.js";
     fjs.parentNode.insertBefore(js, fjs);
   }(document, 'script', 'facebook-jssdk'));
  
  gapi.load('auth2', function(){
      // Retrieve the singleton for the GoogleAuth library and set up the client.
      auth2 = gapi.auth2.init({
        client_id: '517911210517-uupk9pfrbcsnce7qblohvpmog4hc6bfu.apps.googleusercontent.com',
        cookiepolicy: 'single_host_origin',
      });
      if(auth2.isSignedIn.get()){
		googleStatusChangeCallback();
      }
    });
  
	$('#facebook-login-button').click(function(){
		FB.login(function(response){
			facebookStatusChangeCallback(response);
		});
	});
	$('#google-login-button').click(function(){
		auth2.signIn().then(function () {
			googleStatusChangeCallback();
	    });
	});
	

	$('#logout-button').click(function(event){
		event.preventDefault();
		$.get(window.location.pathname+'api/logout.request'); 
		try{
			FB.logout(function(response) {
			  if (response.status !== 'connected') {
				  loggedOut();
			  }
			});
		}catch (e) {
			console.info('Facebook logout ignored: '+e.message);
		}
		try{
			auth2.signOut().then(function () {
				loggedOut();
		    });
		}catch (e) {
			console.info('Google logout ignored: '+e.message);
		}
	});

});