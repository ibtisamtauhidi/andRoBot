function toggleCmdPrompt() {
	if(CmdPromptVisible==true) {
		$('#command_prompt').slideUp();
		$('#footer_command_prompt').html("<img src='./icons/terminal_up.png' class='footer_button_img' />");
		CmdPromptVisible=false;
	} else {
		$('#command_prompt').slideDown(500,function() {$("#cmd").focus();});
		$('#footer_command_prompt').html("<img src='./icons/terminal_down.png' class='footer_button_img'/>");
		CmdPromptVisible=true;
		

	}
}
function login() {
		var passwd = $('#loginPassword').val();
		var myMD5 = "99e9bae675b12967251c175696f00a70";
		var mySHA1 = "d787f56b080945c1ec0b3343cbf962ca427bb8ef";
		var pubKey = "m3g4d37h";
		if((CryptoJS.MD5(passwd)==myMD5)&&(CryptoJS.SHA1(passwd)==mySHA1)) {
			$('#loginFrame').fadeOut();
			loggedIn = true;
			passwdErr=0;
			var encryptedPasswd = CryptoJS.AES.encrypt(passwd,pubKey);
			$("#loginPassword").val("");
		} else if(passwdErr==0) {
			$('#loginBox').append("<div id='passwordError'>The password you entered is incorrect. Please enter correct password.</div>");
			passwdErr=1;
			$("#loginPassword").val("");
		} else if(passwdErr==1) {
			$('#passwordError').html("This is your last chance to enter the correct password.");
			passwdErr=2;
			$("#loginPassword").val("");
		} else {
			window.location='../';
		}
}
function logout() {
		$('#loginFrame').fadeIn();	
		loggedIn = false;
}
function changeColorScheme() {
	currentColorScheme+=1;
	currentColorScheme%=2;
	if(currentColorScheme==0) {
		$('#rover_name').css('background','#f74e00');
		$('#rover_name').css('color','#000');
		$('#view_name').css('background','#ffffff');
		$('#view_name').css('color','#000');
		$('#view_name').css('border-bottom','15px solid #1f4e00');
		$('#frame').css('background','#ceffae');
		$('#footer').css('background','#aeaeff');
	} else if(currentColorScheme==1) {
		$('#rover_name').css('background','#010101');
		$('#rover_name').css('color','#DDD');
		$('#view_name').css('background','#151515');
		$('#view_name').css('color','#AAA');
		$('#view_name').css('border-bottom','15px solid #333');
		$('#frame').css('background','#AAA');
		$('#footer').css('background','#555');
	}
}
function nextView() {
	currentView+=1;
	currentView%=6;
	setView();
}

function prevView() {
	currentView-=1;
	if(currentView<0) currentView=5;
	setView();
}
function setView() {
	if(currentView==0) setHomeView();
	else if(currentView==1) setCamView();
	else if(currentView==2) setSensorView();
	else if(currentView==3) setMapView();
	else if(currentView==4) setNNOutputView();
	else if(currentView==5) setAnalyticsView();
}
