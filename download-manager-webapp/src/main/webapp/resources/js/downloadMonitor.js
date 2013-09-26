var DownloadMonitor = {
	communicationLost : false,
	sImageUrl : "resources/images/",
	monitorDownloadStatusTimer: null,
	monitorProductDownloadStatusTimer: {},
	darExpandColumnIndex: 0,
	darMonitoringURLColumnIndex: 1,
	darMonitoringStatusColumnIndex: 2,
	productAccessURLColumnIndex: 0,
    productOverallSizeColumnIndex: 1,
    productProgressDownloadedSizeColumnIndex: 2,
    productProgressProgressPercentageColumnIndex: 3,
    productProgressStatusColumnIndex: 4,
    productActionsColumnIndex: 5,
	initialiseDownloadForm : function(downloadForm,downloadStatusTable) {
		downloadForm.submit(function() {
	    	DownloadMonitor.addDownload(downloadForm.find("#downloadUrl").val(),downloadStatusTable);

	    	return false;
	    });
	},
	initialiseDownloadStatusTable : function(downloadStatusTable) {
		downloadStatusTable.dataTable({
			"sAjaxDataProp": "dataAccessRequests",
			"aoColumns": [
			              {
		                    	"mData": null, 
			                    "sDefaultContent": '<img class="darDetails" src="'+DownloadMonitor.sImageUrl+'details_open.png'+'" />'
		                  },
//			              { "mData": "uuid" },
			              { "mData": "monitoringURL" },
			              { "mData": "monitoringStatus" }
			          ],
			"aoColumnDefs": [
			                 { "sWidth": "20px", "aTargets": [ DownloadMonitor.darExpandColumnIndex ] },
			                 { "sWidth": "150px", "aTargets": [ DownloadMonitor.darMonitoringStatusColumnIndex ] },
			                 ],
			"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
				$(nRow).attr("data-dataAccessRequest-id",aData.uuid);

				var darStatusCell = $(nRow).find("td:eq(" + DownloadMonitor.darMonitoringStatusColumnIndex + ")");
  				var darStatus = aData.monitoringStatus;
  				var darStatusTranslated = messages['dar_status.' + darStatus];
  				darStatusCell.html(darStatusTranslated);
				return nRow;
			},
            "bPaginate": false,
            "bFilter" : false,
            "bInfo" : false,
            "bSort": false
		});
		DownloadMonitor.monitorDownloadStatus(downloadStatusTable);
	},		
	addDownload: function(productDownloadUrl, downloadStatusTable) {
		$.ajax({
			  type: "POST",
			  url: "manualProductDownload",
			  data: {productDownloadUrl : productDownloadUrl},
			  dataType: "json"})
		.done(function(response) {
			if(response.success === false) {
				DownloadMonitor.displayMessage(messages['error.unable_to_add_manual_download'] + ": " + response.message, "ruby");
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.download_failed'], jqXHR.responseText);
		});
	},
	monitorDownloadStatus : function(downloadStatusTable) {
		$.getJSON("dataAccessRequests")
		.done(function(data) {
			DownloadMonitor.displayDownloadStatus(downloadStatusTable,data);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.retrieve_dar_status'], jqXHR);
			clearTimeout(DownloadMonitor.monitorDownloadStatusTimer);
		})
		.done(function() {
			DownloadMonitor.monitorDownloadStatusTimer = setTimeout(function() { DownloadMonitor.monitorDownloadStatus(downloadStatusTable); } , 5000);
		});
	},	
	displayDownloadStatus : function(downloadStatusTable, dataAccessRequestList) {
		for(var i = 0; i < dataAccessRequestList.length; i++) {
			var dataAccessRequest = dataAccessRequestList[i];
			var dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + dataAccessRequest.uuid + "']").get(0);
								
			if(typeof dataAccessRequestRow === 'undefined') {
				downloadStatusTable.dataTable().fnAddData(dataAccessRequest);
				dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + dataAccessRequest.uuid + "']").get(0);

				rowExpandImage = $(dataAccessRequestRow).find('img.darDetails');
		        DownloadMonitor.expandDataAccessRequestRow(downloadStatusTable, dataAccessRequestRow, rowExpandImage);
			}else{
				downloadStatusTable.dataTable().fnUpdate(dataAccessRequest.monitoringStatus, dataAccessRequestRow, DownloadMonitor.darMonitoringStatusColumnIndex);
			}
		}
		DownloadMonitor.allowDataAccessRequestRowExpand(downloadStatusTable);
	},
	formatProductList : function(tableData, row ) {
	    var aData = tableData.fnGetData( row );
	    var sOut = '<table id="productList' + aData.uuid + '" style="padding-left:30px;"><thead><tr>';
	    sOut += '<th class="productAccessURL">' + messages['product_table.heading.product_access_url'] + '</th>';
	    sOut += '<th class="productDownloadedSize">' + messages['product_table.heading.total_size'] + '</th>';
	    sOut += '<th class="productDownloadedSize">' + messages['product_table.heading.downloaded_size'] + '</th>';
	    sOut += '<th class="productProgress">' + messages['product_table.heading.progress'] + '</th>';
	    sOut += '<th class="productStatus">' + messages['product_table.heading.product_status'] + '</th>';
	    sOut += '<th class="productMessage">' + messages['product_table.heading.message'] + '</th>';
	    sOut += '<th class="productActions">' + messages['product_table.heading.actions'] + '</th>';
	    sOut += '</tr></thead></table>';
	    return sOut;
	},
	allowDataAccessRequestRowExpand : function(downloadStatusTable) {
		$(downloadStatusTable).find('tbody tr').each(function() {
			var rowToExpand = this;
			$(rowToExpand).find('img.darDetails').off('click').on('click', function () {
				DownloadMonitor.expandDataAccessRequestRow(downloadStatusTable, rowToExpand, this);
			});
	    });
	},
	expandDataAccessRequestRow : function(downloadStatusTable, rowToExpand, rowExpandImage) {
		if (downloadStatusTable.dataTable().fnIsOpen(rowToExpand))
        {
            /* This row is already open - close it */
			$(rowExpandImage).attr("src", DownloadMonitor.sImageUrl+"/details_open.png");
            downloadStatusTable.dataTable().fnClose(rowToExpand);
        }else{
            /* Open this row */
			$(rowExpandImage).attr("src", DownloadMonitor.sImageUrl+"/details_close.png");
            downloadStatusTable.dataTable().fnOpen(rowToExpand, DownloadMonitor.formatProductList(downloadStatusTable.dataTable(), rowToExpand), 'details' );
            var aData = downloadStatusTable.dataTable().fnGetData(rowToExpand);
            DownloadMonitor.initialiseProductListTable(aData);
            DownloadMonitor.monitorProductStatus(downloadStatusTable, aData.uuid);
        }
	},
	initialiseProductListTable : function(aData) {
        $("#productList"+aData.uuid).dataTable({
    		"aoColumns": [
			              { "mData": "productAccessUrl" },
			              { "mData": "overallSize" },
			              { "mData": "productProgress.downloadedSize" },
			              { "mData": "productProgress.progressPercentage" },
			              { "mData": "productProgress.status" },
			              { "mData": "productProgress.message" },
			              { "mData": null }
			          ],
			 "aoColumnDefs": [
				              { "sWidth": "150px", "aTargets": [ 1 ] },
				              { "sWidth": "150px", "aTargets": [ 2 ] },
				              { "sWidth": "200px", "aTargets": [ 3 ] },
				              { "sWidth": "150px", "aTargets": [ 4 ] },
				              { "bVisible": false, "aTargets": [ 5 ] },
				              { "sWidth": "50px", "aTargets": [ 6 ] },
			                 ],
             "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
 				$(nRow).attr("data-product-id",aData.uuid);
  				var fileProductStatus = $(nRow).find("td:eq(" + DownloadMonitor.productAccessURLColumnIndex + ")");
  				fileProductStatus.html(decodeURIComponent(aData.productAccessUrl));
  				
  				var fileProductStatus = $(nRow).find("td:eq(" + DownloadMonitor.productProgressStatusColumnIndex + ")");
  				var productStatus = aData.productProgress.status;
  				productStatusTranslated = messages['download_status.' + productStatus];
  				fileProductStatus.html(productStatusTranslated);
 				if(productStatus == "IN_ERROR") {
 					var iconWarning = fileProductStatus.find(".iconWarning");
 					iconWarning.remove();
 					fileProductStatus.append("<span class=\"iconWarning ui-state-default ui-corner-all\"><span class=\"ui-icon ui-icon-alert\" title=\"" + messages['error.product_download'] + ": " + aData.productProgress.message + "\">!</span></span>");
 				}
 				var fileOverallSizeCell = $(nRow).find("td:eq(" + DownloadMonitor.productOverallSizeColumnIndex + ")");
 				fileOverallSizeCell.html(DownloadMonitor.getReadableFileSizeString(aData.overallSize));
 				var fileDownloadedSizeCell = $(nRow).find("td:eq(" + DownloadMonitor.productProgressDownloadedSizeColumnIndex + ")");
 				fileDownloadedSizeCell.html(DownloadMonitor.getReadableFileSizeString(aData.productProgress.downloadedSize));
            	 
 				var progressCell = $(nRow).find("td:eq(" + DownloadMonitor.productProgressProgressPercentageColumnIndex + ")");
 				progressCell.addClass("productProgress");
 				progressCell.html("<div class=\"progressbar\"><div class=\"progress-label\"></div></div>");
 				progressCell.find(".progressbar").progressbar({
 					value: Math.floor(aData.productProgress.progressPercentage),
 					create: function() {
 						if(aData.overallSize === -1 && aData.productProgress.progressPercentage === -1) {
 	 						$(this).children(".progress-label").text( messages['label.unknown'] );
 						}else{
 	 						$(this).children(".progress-label").text( $(this).progressbar( "value" ) + "%" );
 						}
 					}
 				});
 				var actionsCell = $(nRow).find("td:eq(" + DownloadMonitor.productActionsColumnIndex + ")");
 				actionsCell.html(DownloadMonitor.getActions(aData));
 			},
			 "bPaginate": false,
             "bFilter" : false,
             "bInfo" : false,
             "bSort": false
		});
	},
	getActions : function(product) {
		var productProgress = product.productProgress.status;
		var actionsHtml = '<ul id="actions" class="ui-widget ui-helper-clearfix">';
		if(productProgress == "NOT_STARTED" || productProgress == "IDLE" || productProgress == "RUNNING") {
			actionsHtml += DownloadMonitor.displayAction("default", "pause", product.uuid);
			actionsHtml += DownloadMonitor.displayAction("default", "cancel", product.uuid);
		} else if(productProgress == "PAUSED") {
			actionsHtml += DownloadMonitor.displayAction("default", "resume", product.uuid);
			actionsHtml += DownloadMonitor.displayAction("default", "cancel", product.uuid);
		} else if(productProgress == "IN_ERROR") {
			actionsHtml += DownloadMonitor.displayAction("default", "resume", product.uuid);
		}
		actionsHtml += '</ul>';

		return actionsHtml;
	},
	displayAction: function(state, actionType, productUuid) {
		var iconClass = "ui-icon-";
		if(actionType == "pause") {
			iconClass += "pause";
		}else if(actionType == "resume") {
			iconClass += "play";
		}else if(actionType == "cancel") {
			iconClass += "stop";
		}
		var actionHtml = '<li class="ui-state-' + state + ' ui-corner-all">';
		if(state === "default") {
			actionHtml += '<a href=\'javascript:DownloadMonitor.productDownloadCommand("' + productUuid + '","' + actionType + '");\'>';
		}
		actionHtml += '<span class="ui-icon ' + iconClass + '">';
		if(state === "default") {
			actionHtml += '</a>';
		}
		actionHtml += '</span></li>';
		return actionHtml;
	},
	productDownloadCommand : function(productUuid, action) {
		var sending_product_download_command_error = messages['error.sending_product_download_command'];
		$.getJSON("products/" + productUuid, { action : action })
		.done(function(response) {
			if(response.success === false) {
				sending_product_download_command_error = sending_product_download_command_error.replace("*a*", action);
				sending_product_download_command_error = sending_product_download_command_error.replace("*p*", productUuid);
				
				DownloadMonitor.displayMessage(sending_product_download_command_error + ": " + response.message);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			sending_product_download_command_error = sending_product_download_command_error.replace("*a*", action);
			sending_product_download_command_error = sending_product_download_command_error.replace("*p*", productUuid);

			DownloadMonitor.displayErrorMessage(sending_product_download_command_error, jqXHR);
		});
	},
	monitorProductStatus : function (downloadStatusTable, darUuid) {
		var dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + darUuid + "']").get(0);
		$.getJSON("dataAccessRequests/" + darUuid)
		.done(function(data) {
			if(downloadStatusTable.dataTable().fnIsOpen(dataAccessRequestRow)) {
				DownloadMonitor.displayProductDetails(downloadStatusTable, darUuid, data);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.retrieve_product_status'] + " " + darUuid, jqXHR);
			clearTimeout(DownloadMonitor.monitorProductDownloadStatusTimer[darUuid]);
		})
		.done(function() {
			if(downloadStatusTable.dataTable().fnIsOpen(dataAccessRequestRow)) {
				DownloadMonitor.monitorProductDownloadStatusTimer[darUuid] = setTimeout(function() { DownloadMonitor.monitorProductStatus(downloadStatusTable , darUuid); } , 500);
			}
		});
	},
	displayProductDetails : function(downloadStatusTable, darUuid, productList) {
		//TODO: modify so that after first display we are only updating the downloaded size and progress
//		$("#productList"+darUuid).dataTable().fnClearTable();
//		$("#productList"+darUuid).dataTable().fnAddData(productList);
		
		var productDataTable = $("#productList"+darUuid).dataTable();
		for(var i = 0; i < productList.length; i++) {
			var product = productList[i];
			var productRow = productDataTable.$("tr[data-product-id='" + product.uuid + "']").get(0);
								
			if(typeof productRow === 'undefined') {
				productDataTable.fnAddData(product);
			}else{
				productDataTable.fnUpdate(product, productRow);
/*
 				var currentProductStatus = productDataTable.fnGetData(productRow, DownloadMonitor.productProgressStatusColumnIndex);
				var newProductStatus = product.productProgress.status;
				if(currentProductStatus != newProductStatus) {
					productDataTable.fnUpdate(newProductStatus, productRow, DownloadMonitor.productProgressStatusColumnIndex);
				}
				productDataTable.fnUpdate(product.productProgress.message, productRow, DownloadMonitor.productMessageColumnIndex);
				productDataTable.fnUpdate(product.productProgress.downloadedSize, productRow, DownloadMonitor.productProgressDownloadedSizeColumnIndex);
				productDataTable.fnUpdate(product.productProgress.progressPercentage, productRow, DownloadMonitor.productProgressProgressPercentageColumnIndex);
*/
			}
		}

	},
	getReadableFileSizeString : function(fileSizeInBytes) {
		if(fileSizeInBytes === -1) {
			return messages['label.unknown'];
		}
	    var i = -1;
	    var byteUnits = [' KB', ' MB', ' GB', ' TB', ' PB', ' EB', ' ZB', ' YB'];
	    do {
	        fileSizeInBytes = fileSizeInBytes / 1024;
	        i++;
	    } while (fileSizeInBytes > 1024);

	    return Math.max(fileSizeInBytes, 0.0).toFixed(1) + byteUnits[i];
	},
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
				errorMessage = errorObject.response.message;
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
	clearActivityHistory : function(downloadStatusTable) {
		$.getJSON("clearActivityHistory")
		.done(function(response) {
			if(response.success === false) {
				DownloadMonitor.displayMessage(messages['error.clear_activity_history'] + ": " + response.message, "lemon");
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.download_failed'], jqXHR.responseText);
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
				DownloadMonitor.displayMessage(messages['error.stop_download_function'] + ": " + response.message, "ruby");
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage(messages['error.stop_download_function'], jqXHR.responseText);
		});
	},
};