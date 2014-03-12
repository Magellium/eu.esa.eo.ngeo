var DownloadMonitor = {
	communicationLost : false,
	sImageUrl : "resources/images/",
	monitorDownloadStatusTimer: null,
	monitorProductDownloadStatusTimer: {},
	initialiseDownloadForm : function(manualDownloadForm, downloadStatusTable) {
		manualDownloadForm.find("#addProductDownload").click(function() {
	    	DownloadMonitor.addProductDownload(manualDownloadForm.find("#productDownloadUrl").val(),downloadStatusTable);
	    	return false;
	    });
		manualDownloadForm.find("#addDAR").click(function() {
	    	DownloadMonitor.addDAR(manualDownloadForm.find("#darUrl").val(),downloadStatusTable);
	    	return false;
	    });
	},
	addProductDownload: function(productDownloadUrl, downloadStatusTable) {
		$.ajax({
			  type: "POST",
			  url: "download",
			  data: {productDownloadUrl : productDownloadUrl},
			  dataType: "json"})
		.done(function(response) {
			if(response.success === false) {
				MessageDisplay.displayMessage(messages['error.unable_to_add_manual_download'] + ": " + response.errorMessage, "ruby");
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
				$("#productDownloadUrl").val("");
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			MessageDisplay.displayErrorMessage(messages['error.download_failed'], jqXHR.responseText);
		});
	},
	addDAR: function(darUrl, downloadStatusTable) {
		$.ajax({
			  type: "POST",
			  url: "download",
			  data: {darUrl : darUrl},
			  dataType: "json"})
		.done(function(response) {
			if(response.success === false) {
				MessageDisplay.displayMessage(messages['error.unable_to_add_manual_download'] + ": " + response.errorMessage, "ruby");
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
				$("#darUrl").val("");
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			MessageDisplay.displayErrorMessage(messages['error.download_failed'], jqXHR.responseText);
		});
	},
	monitorDownloadStatus : function(downloadStatusTable) {
		$.getJSON("dataAccessRequests")
		.done(function(data) {
			DarStatusTable.displayDownloadStatus(downloadStatusTable,data.dataAccessRequests);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			MessageDisplay.displayErrorMessage(messages['error.retrieve_dar_status'], jqXHR);
			clearTimeout(DownloadMonitor.monitorDownloadStatusTimer);
		})
		.done(function() {
			DownloadMonitor.monitorDownloadStatusTimer = setTimeout(function() { DownloadMonitor.monitorDownloadStatus(downloadStatusTable); } , 5000);
		});
	},	
	monitorProductStatus : function (downloadStatusTable, darUuid) {
		var dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + darUuid + "']").get(0);
		$.getJSON("dataAccessRequests/" + darUuid)
		.done(function(data) {
			if(downloadStatusTable.dataTable().fnIsOpen(dataAccessRequestRow)) {
				ProductStatusTable.displayProductDetails(downloadStatusTable, darUuid, data);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			MessageDisplay.displayErrorMessage(messages['error.retrieve_product_status'] + " " + darUuid, jqXHR);
			clearTimeout(DownloadMonitor.monitorProductDownloadStatusTimer[darUuid]);
		})
		.done(function() {
			if(downloadStatusTable.dataTable().fnIsOpen(dataAccessRequestRow)) {
				DownloadMonitor.monitorProductDownloadStatusTimer[darUuid] = setTimeout(function() { DownloadMonitor.monitorProductStatus(downloadStatusTable , darUuid); } , 500);
			}
		});
	},
	clearActivityHistory : function(downloadStatusTable) {
		$.getJSON("clearActivityHistory")
		.done(function(response) {
			if(response.success === false) {
				MessageDisplay.displayMessage(messages['error.clear_activity_history'] + ": " + response.errorMessage, "lemon");
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			MessageDisplay.displayErrorMessage(messages['error.download_failed'], jqXHR.responseText);
		});
	},
	resetDownloadDisplay: function(downloadStatusTable) {
		$(downloadStatusTable).dataTable().fnClearTable();
		clearTimeout(DownloadMonitor.monitorDownloadStatusTimer);
		$.each(DownloadMonitor.monitorProductDownloadStatusTimer, function(key, value) {
			clearTimeout(value);
		});
		DownloadMonitor.monitorDownloadStatus(downloadStatusTable);
	},
	stopDownloads : function(stopType) {
		$.getJSON("monitoring/stop?type=" + stopType)
		.done(function(response) {
			if(response.success === false) {
				DownloadMonitor.displayMessage(messages['error.stop_download_function'] + ": " + response.errorMessage, "ruby");
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.stop_download_function'], jqXHR.responseText);
		});
	},
	productDownloadCommand : function(productUuid, action) {
		var sending_product_download_command_error = messages['error.sending_product_download_command'];
		$.getJSON("products/" + productUuid, { action : action })
		.done(function(response) {
			if(response.success === false) {
				sending_product_download_command_error = sending_product_download_command_error.replace("*a*", action);
				sending_product_download_command_error = sending_product_download_command_error.replace("*p*", productUuid);
				
				MessageDisplay.displayMessage(sending_product_download_command_error + ": " + response.errorMessage);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			sending_product_download_command_error = sending_product_download_command_error.replace("*a*", action);
			sending_product_download_command_error = sending_product_download_command_error.replace("*p*", productUuid);

			MessageDisplay.displayErrorMessage(sending_product_download_command_error, jqXHR);
		});
	}
};