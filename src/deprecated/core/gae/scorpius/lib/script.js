function start() {
	$('#command_prompt').hide();
	$("#loginPassword").keyup(function(event){
		if(event.keyCode == 13){
			$("#loginButton").click();
		}
	});
	$("#cmd").keyup(function(event){
    if(event.keyCode == 13){
        $("#cmdSend").click();
    }
	});
	
	$("body").keyup(function(event){
		if(loggedIn == false) {
			return;
		}
		if(event.keyCode == 37){
			prevView();
		} else if(event.keyCode == 39){
			nextView();
		}
		startUpdates();
	});
		/*
	sensorUpdate();
	imageUpdate();
	loginPassword

	*/
}
function startUpdates() {
	if (currentView==0) return;
	else if(currentView==1) updateCamera();
	else if(currentView==2) updateSensor();
	else if(currentView==3) updateMap();
	else if(currentView==4) updateNN();
	else if(currentView==5) updateAnalytics();
}
