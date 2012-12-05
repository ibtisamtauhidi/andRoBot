function start() {
	sensorUpdate();
	imageUpdate();
	
	$("#cmd").keyup(function(event){
    if(event.keyCode == 13){
        $("#cmdSend").click();
    }
	});
}

