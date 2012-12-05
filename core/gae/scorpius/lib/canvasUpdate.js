function setCompassDirection(angle) {
	var currcanvas = document.getElementById("sensorViewCompassCanvas");
	currcanvas.width=currcanvas.width;
	var currcontext = currcanvas.getContext("2d");
	currcontext.beginPath();
	currcontext.arc(150, 75, 70, 0, Math.PI * 2, false);
	currcontext.closePath();
	currcontext.strokeStyle = "#000";
	currcontext.stroke();
	currcontext.font = "bold 15px verdana";
	currcontext.fillText("W", 85, 80);
	currcontext.fillText("E", 205, 80);
	currcontext.fillText("N", 145, 25);
	currcontext.fillText("S", 145, 140);
	currcontext.beginPath();
	currcontext.arc(150, 75, 5, 0, Math.PI * 2, false);
	currcontext.closePath();
	currcontext.fillStyle = "#000";
	currcontext.fill();
	angle*=Math.PI/180;
	angle-=Math.PI/2;
	x = 150+(40*Math.cos(angle));
	y = 75+(40*Math.sin(angle));
	currcontext.beginPath();
	currcontext.moveTo(150, 75);
	currcontext.lineTo(x, y);
	currcontext.strokeStyle = "#000";
	currcontext.stroke();
}

function setAccelerometerGraph() {	
	var currcanvas = document.getElementById("sensorViewAccelerometerCanvas");
	currcanvas.width=currcanvas.width;
	var currcontext = currcanvas.getContext("2d");
	currcontext.beginPath();
	currcontext.moveTo(10.5, 10.5);
	currcontext.lineTo(10.5, 140.5);
	currcontext.moveTo(10.5, 140.5);
	currcontext.lineTo(290.5, 140.5);
	currcontext.strokeStyle = "#000";
	currcontext.stroke();
	
	currcontext.beginPath();	
	for (x = 30.5; x < 300; x += 20) {
	  currcontext.moveTo(x, 30);
	  currcontext.lineTo(x, 140);
	}
	currcontext.strokeStyle = "#555";
	currcontext.stroke();

	currcontext.beginPath();
	x=10.5;
	currcontext.moveTo(10.5, 30);
	for (count = 0; count < 15; count+=1) {
	  currcontext.lineTo(x, 140-(((gyroX[count]/11)+2)*25));
	  x+=20;
	}
	currcontext.strokeStyle = "#AA0000";
	currcontext.stroke();

	currcontext.beginPath();
	x=10.5;
	currcontext.moveTo(10.5, 30);
	for (count = 0; count < 15; count+=1) {
	  currcontext.lineTo(x, 140-(((gyroY[count]/11)+2)*25));
	  x+=20;
	}
	currcontext.strokeStyle = "#00AA00";
	currcontext.stroke();
	
	currcontext.beginPath();
	x=10.5;
	currcontext.moveTo(10.5, 30);
	for (count = 0; count < 15; count+=1) {
	  currcontext.lineTo(x, 140-(((gyroZ[count]/11)+2)*25));
	  x+=20;
	}
	currcontext.strokeStyle = "#0000AA";
	currcontext.stroke();
}
