function jsInit() {
	login();	
}
function login() {
	$.get(
		'./ajax/login.txt',	
		{},	
		function(data) { 
			$('body').html(data);
			$("#loginPassword").keyup(function(event){
				if(event.keyCode == 13){
					validate();
				}
			});
		}
	);
}
function validate() {
	var str = $("#loginPassword").val();
	if(str == "") return;
	var request = null;
	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		request = new ActiveXObject("Microsoft.XMLHTTP");
	}
	if (request) {
		request.open("GET", './passwd');
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				json = request.responseText;
				validate2(json,str);
			}
		}
		request.send(null);
	} 
	else {
		alert("Sorry, you must update your browser before seeing Ajax in action.");
	}
}
function validate2(jsonStr,str) {
	if(jsonStr!="") {
		var json = eval ("(" + jsonStr + ")");
		if((CryptoJS.MD5(str)==json.MD5) && (CryptoJS.SHA1(str)==json.SHA1)) {
			start();
		} else {
			alert("Password Error");
		}
	}
}
function start() {
	$.get(
		'./ajax/start.txt',	
		{},	
		function(data) { 
			$('body').html(data);
			sensorUpdate();
			$("body").keyup(function(event){
				if(event.keyCode == 27) login();
			});
		}
	);
}
function sensorUpdate() {
	var request = null;
	if (window.XMLHttpRequest) {
		request = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		request = new ActiveXObject("Microsoft.XMLHTTP");
	}
	if (request) {
		request.open("GET", './readSensor');
		request.onreadystatechange = function() {
			if (request.readyState == 4) {
				json = request.responseText;
				sensorPrint(json);
			}
		}
		request.send(null);
	} 
	else {
		alert("Sorry, you must update your browser before seeing Ajax in action.");
	}
}
function sensorPrint(jsonStr) {
	if(jsonStr!="") {
		var json = eval ("(" + jsonStr + ")");
		$("#batt").html("BATTERY<br /><br />"+json.BATTERY);
		$("#temp").html("TEMPERATURE<br /><br />"+json.TEMPERATURE);
	}
}
function loadFeature(data) {
	if(data=="sensor") loadWindow("sensor");
	else if(data=="remote") {
		$.get(
			'./viewmode/remote',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("image");
			}
		);
	} else if(data=="line") {
		$.get(
			'./viewmode/blob',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("image");
			}
		);
	} else if(data=="face") {
		$.get(
			'./viewmode/face',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("image");
			}
		);
	} else if(data=="obstacle") {
		$.get(
			'./viewmode/obstacle',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("image");
			}
		);
	} else if(data=="motion") {
		$.get(
			'./viewmode/motion',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("motion");
			}
		);
	} else if(data=="gyroremote") {
		$.get(
			'./gyroremote',	
			{},	
			function(data) { 
				if(data=="OK") loadWindow("gyroremote");
			}
		);
	}
	else if(data=="voice") loadWindow("voice");
	else if(data=="map") loadWindow("map");
	else if(data=="settings") loadWindow("settings");

}
function loadWindow(data) {
	$('#start').remove();
	if(data=="sensor") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Sensor Watch</span>						\
						</div> 																															\
						<iframe src='./html/sensor.htm' style='height: 500px; width: 700px; border: 0px;'> 												\
					</div> 																																\
			");
	} else if(data=="image") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Camera View</span>							\
						</div> 																															\
						<iframe src='./html/camera.htm' style='height: 380px; width: 840px; border: 0px;'></div> 										\
			");
	} else if(data=="motion") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Motion Detection</span>					\
						</div> 																															\
						<iframe src='./html/motion.htm' style='height: 380px; width: 840px; border: 0px;'></div> 										\
			");
	} else if(data=="gyroremote") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Motion Detection</span>					\
						</div> 																															\
						<iframe src='./sim' style='height: 600px; width: 840px; border: 0px;'></div> 										\
			");
	} else if(data=="voice") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Voice Syntesis</span>						\
						</div> 																															\
						<iframe src='./html/voice.htm' style='height: 150px; width: 340px; border: 0px;'>												\
					</div> 																																\
			");
	} else if(data=="map") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Map</span>									\
						</div> 																															\
						<iframe src='./html/map.htm' style='height: 500px; width: 840px; border: 0px;'> 												\
					</div> 																																\
			");
	} else if(data=="settings") {
			$('body').html(" 																															\
					<div id='sensor_window' class='sensor_window' style='position: absolute; z-index: 1; top: 30px; left: 50px;'>						\
						<div style='background: rgba(255,255,255,0.7); padding: 5px; border-radius-top-left: 5px; border-radius-top-right: 5px;'> 		\
							<button class='minButton' onClick=\"minimize('sensor_window');\">-</button><span>Map</span>									\
						</div> 																															\
						<iframe src='./html/settings.htm' style='height: 400px; width: 500px; border: 0px;'> 												\
					</div> 																																\
			");
	}
	$("body").keyup(function(event){
				if(event.keyCode == 27) start();
	});
}
function minimize(data) {
	$("#"+data).fadeOut(
		500,
		function() { 
			$("#"+data).remove(); 
			start();
		});
}
function openSim() {
	window.open('./sim');
}
