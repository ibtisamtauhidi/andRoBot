function camFeedUpdate(img) {	
	var ctx=document.getElementById("camfeedimg").getContext("2d");
	image = new Image();
	image.onload = function() {
		ctx.drawImage(image,0,0,300,150); 
		addImageProcessing();
	}
	image.src=img;
}
function addImageProcessing() {
	if (window.document.imgProcessing.pixRed.checked) Pixastic.process(document.getElementById("camfeedimg"), "coloradjust", { red : 1.0 });
	if (window.document.imgProcessing.pixGreen.checked) Pixastic.process(document.getElementById("camfeedimg"), "coloradjust", { green : 1.0 });
	if (window.document.imgProcessing.pixBlue.checked) Pixastic.process(document.getElementById("camfeedimg"), "coloradjust", { blue : 1.0 });
	if (window.document.imgProcessing.pixNegative.checked) Pixastic.process(document.getElementById("camfeedimg"), "invert");
	if (window.document.imgProcessing.pixGray.checked) Pixastic.process(document.getElementById("camfeedimg"),"desaturate", { average: true });
	if (window.document.imgProcessing.pixNoise.checked) Pixastic.process(document.getElementById("camfeedimg"), "noise", { mono:true, amount:0.5, strength:0.5 });
	if (window.document.imgProcessing.pixEdge.checked) Pixastic.process(document.getElementById("camfeedimg"), "edges2");
	if (window.document.imgProcessing.pixCon.checked) Pixastic.process(document.getElementById("camfeedimg"), "brightness", { contrast : 1, legacy : true });
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
				setTimeout(imageUpdate,3000);			
			}
		}
		request.send(null);
	} 
	else {
		alert("Sorry, you must update your browser before seeing Ajax in action.");
	}
}

