function updateCamera() {
	imageUpdate();
}
function camFeedUpdate(img) {	
	var ctx=document.getElementById("camViewCamera").getContext("2d");
	image = new Image();
	image.onload = function() {
		ctx.drawImage(image,0,0,300,150); 
		addImageProcessing();
	}
	image.src=img;
}
function addImageProcessing() {
	if (window.document.imgProcessing.imgProcessingRed.checked) Pixastic.process(document.getElementById("camViewCamera"), "coloradjust", { red : 1.0 });
	if (window.document.imgProcessing.imgProcessingGreen.checked) Pixastic.process(document.getElementById("camViewCamera"), "coloradjust", { green : 1.0 });
	if (window.document.imgProcessing.imgProcessingBlue.checked) Pixastic.process(document.getElementById("camViewCamera"), "coloradjust", { blue : 1.0 });
	if (window.document.imgProcessing.imgProcessingNegative.checked) Pixastic.process(document.getElementById("camViewCamera"), "invert");
	if (window.document.imgProcessing.imgProcessingGray.checked) Pixastic.process(document.getElementById("camViewCamera"),"desaturate", { average: true });
	if (window.document.imgProcessing.imgProcessingNoise.checked) Pixastic.process(document.getElementById("camViewCamera"), "noise", { mono:true, amount:0.5, strength:0.5 });
	if (window.document.imgProcessing.imgProcessingEdge.checked) Pixastic.process(document.getElementById("camViewCamera"), "edges2");
	if (window.document.imgProcessing.imgProcessingCon.checked) Pixastic.process(document.getElementById("camViewCamera"), "brightness", { contrast : 1, legacy : true });
}
function imageUpdate() {
	var request = null;
	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		request = new ActiveXObject("Microsoft.XMLHTTP");
	}
	if (request) {
		request.open("GET", './readImage');
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				json = request.responseText;
				camFeedUpdate(json);
				setTimeout(startUpdates,2000);			
			}
		}
		request.send(null);
	} 
	else {
		alert("Sorry, you must update your browser before seeing Ajax in action.");
	}
}
