function startCmdProcessing() {
	str = $('#cmd').val();
	$('#cmdshell').append("<div class='cmdUserString'>"+str+"</div>");
	str = str.toUpperCase();
	
	processCmd(str);
}
function processCmd(str) {
	if(str=="WHO ARE YOU?")  {
		cmdResponse("I am Scorpius, a robot.");
		return;
    }
    if(str=="WHERE ARE YOU?") {
		cmdResponseShowMap();
		return;
	}	
	if(str=="HELP") {
		cmdResponseHelp();
		return;
	}
	if(str=="CLEAR") {
		cmdClear();
		return;
	}
	strArr = str.split(" ");
	arrLen = strArr.length;
	
	if((strArr[0]="MOVE")&&(arrLen==3)) {
		if(!isNaN(strArr[1])) {
			var dist = null;
			if((strArr[2]=="METRES")||(strArr[2]=="METRE")||(strArr[2]=="M")) dist=parseInt(100*strArr[1]);
			else if((strArr[2]=="CENTIMETRES")||(strArr[2]=="CENTIMETRE")||(strArr[2]=="CM")) dist=parseInt(strArr[1]);
			if(dist!=null) {
				cmdResponse("Sending command to move "+dist+" centimetres.");
				changeMode("1",""+dist);
				return;
			}
		}
	}

	if((strArr[0]="ROTATE")&&(arrLen==3)) {
		if(!isNaN(strArr[1])) {
			var dist = null;
			if((strArr[2]=="RADIAN")||(strArr[2]=="RADIANS")||(strArr[2]=="R")) dist=(180/Math.PI)*parseFloat(strArr[1]);
			else if((strArr[2]=="DEGREE")||(strArr[2]=="DEGREE")||(strArr[2]=="D")) dist=parseInt(strArr[1]);
			if(dist!=null) {
				cmdResponse("Sending command to rotate "+dist+" degree.");
				changeMode("2",""+dist);
				return;
			}
		}
	}
	if((strArr[0]="GO")&&(arrLen==3)) {
		if(strArr[2]=="MODE") {
			if ((strArr[1]=="PHOTO")||
				(strArr[1]=="SURVEY")||
				(strArr[1]=="LINEFOLLOWER")||
				(strArr[1]=="FACEDETECTOR")||
				(strArr[1]=="PANORAMA")) {
				changeMode("3",strArr[1]);
				cmdResponseChangeMode(strArr[1]);
				return;
			}
		}
	}
	if((strArr[0]="SAY")&&(arrLen>=2)) {
		var tempStr = "";
		for(i=1;i<arrLen;i+=1) {
			tempStr+=strArr[i]+" ";
		}
		tempStr=tempStr.toLowerCase();
		cmdResponse("Sendind cammand to voice synthesize : "+tempStr);
		changeMode("4",tempStr);
		return;
	}

	cmdResponse("I didn't understand the command. Type 'help' for a list of commands.");
}
function cmdResponse(str) {
	$('#cmdshell').append("<div class='cmdResponseString'>"+str+"</div>");
	var objControl=document.getElementById('cmdshell');
	objControl.scrollTop = objControl.scrollHeight;
	$("#cmd").val("");
}
function cmdResponseShowMap() {
	cmdResponse("<iframe width=\"230\" height=\"150\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\" src=\"https://maps.google.co.in/maps?f=q&amp;source=s_q&amp;hl=en&amp;geocode=&amp;q=sri+siddhartha+institute+of+technology&amp;aq=&amp;sll=13.290738,77.016907&amp;sspn=0.82061,0.883026&amp;t=h&amp;ie=UTF8&amp;hq=sri+siddhartha+institute+of+technology&amp;hnear=&amp;ll=13.290738,77.016907&amp;spn=0.515747,0.648388&amp;output=embed\"></iframe><br /><small><a href=\"https://maps.google.co.in/maps?f=q&amp;source=embed&amp;hl=en&amp;geocode=&amp;aq=&amp;sll=13.290738,77.016907&amp;sspn=0.82061,0.883026&amp;t=h&amp;ie=UTF8&amp;hq=sri+siddhartha+institute+of+technology&amp;hnear=&amp;ll=13.290738,77.016907&amp;spn=0.515747,0.648388\" style=\"color:#0000FF;text-align:left\">View Larger Map</a></small>");
}
function cmdResponseHelp() {
	cmdResponse("<strong>LIST OF COMMANDS</strong><br />1. who are you?<br />2. where are you?<br />3. move INT UNIT<br />4. rotate INT UNIT<br />5. go MODE_NAME mode<br />6. say STRING<br />7. clear<br />INT is an integer. UNIT can be metre, centimetre, degree or radian. MODE_NAME can be photo, survey, linefollower, facedetector or panorama. STRING is any string.");
}
function cmdClear() {
	$("#cmdshell").html("");	
	$("#cmd").val("");
}
function cmdResponseChangeMode(str) {
	cmdResponse("Sending command to change mode to "+str);
}
function changeMode(mode_p,param_p) {
	//if(modeActive==false) {
		$.post('./writeCmd',{ 
			mode : mode_p, 
			param : param_p
		},changeModeSucc);
	//} else {
	//	alert("Mode change in action...");
	//}
}
function changeModeSucc() {
	modeActive=true;
}
