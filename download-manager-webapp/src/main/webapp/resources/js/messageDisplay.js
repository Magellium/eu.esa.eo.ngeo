var MessageDisplay = {
	displayMessage : function(message, theme) {
		if(theme === "") {
			theme = "smoke";
		}
		$.notific8(message, {
			theme : theme,
			sticky : true,
			horizontalEdge : 'top',
			verticalEdge : 'right',
		});
	},
	displayErrorMessage : function(messageHeading, error) {
		var errorMessage = "";
		var heading = "";
		if(error.status !== 0) {
			if(typeof error === 'object') {
				errorMessage = "Error: HTTP " + error.status + ", "+ error.statusText;
			}else{
				var errorObject = jQuery.parseJSON(error);
				errorMessage = errorObject.response.errorMessage;
			}
		}else{
			if(!DownloadMonitor.communicationLost) {
				heading = messages['error.heading.communication_lost'];
				errorMessage = messages['error.message.communication_lost'];
				DownloadMonitor.communicationLost = true;
			}
		}
		if(errorMessage.length > 0) {
			$.notific8(errorMessage, {
				heading : heading,
				theme : "ruby",
				sticky : true,
				horizontalEdge : 'top',
				verticalEdge : 'right',
			});
		}
	},
};