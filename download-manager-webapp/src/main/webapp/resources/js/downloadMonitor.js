var DownloadMonitor = {
	sImageUrl : "resources/images/",
	monitorDownloadStatusTimer: null,
	monitorProductDownloadStatusTimer: {},
	darExpandColumnIndex: 0,
	darMonitoringURLColumnIndex: 1,
	darMonitoringStatusColumnIndex: 2,
	productAccessURLColumnIndex: 0,
    productProgressStatusColumnIndex: 1,
    productProgressDownloadedSizeColumnIndex: 2,
    productProgressProgressPercentageColumnIndex: 3,
    productActionsColumnIndex: 4,
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
				DownloadMonitor.displayMessage("Error with retrieving download status: " + response.message);
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage("Download Failed! ", jqXHR.responseText);
		});
	},
	monitorDownloadStatus : function(downloadStatusTable) {
		$.getJSON("dataAccessRequests")
		.done(function(data) {
			DownloadMonitor.displayDownloadStatus(downloadStatusTable,data);
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage("Error with retrieving download status", jqXHR);
			clearTimeout(monitorDownloadStatusTimer);
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
	    sOut += '<th class="productAccessURL">Product Access URL</th>';
	    sOut += '<th class="productStatus">Product Status</th>';
	    sOut += '<th class="productDownloadedSize">Downloaded Size</th>';
	    sOut += '<th class="productProgress">Progress</th>';
	    sOut += '<th class="productActions">Actions</th>';
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
			              { "mData": "productProgress.status" },
			              { "mData": "productProgress.downloadedSize" },
			              { "mData": "productProgress.progressPercentage" },
			              { "mData": null }
			          ],
			 "aoColumnDefs": [
				              { "sWidth": "150px", "aTargets": [ 1 ] },
				              { "sWidth": "150px", "aTargets": [ 2 ] },
				              { "sWidth": "200px", "aTargets": [ 3 ] },
				              { "sWidth": "50px", "aTargets": [ 4 ] },
			                 ],
             "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
 				$(nRow).attr("data-product-id",aData.uuid);
  				var fileProductStatus = $(nRow).find("td:eq(" + DownloadMonitor.productProgressStatusColumnIndex + ")");
 				if(aData.productProgress.status == "IN_ERROR") {
 					var iconWarning = fileProductStatus.find(".iconWarning");
 					iconWarning.remove();
 					fileProductStatus.append("<span class=\"iconWarning ui-state-default ui-corner-all\"><span class=\"ui-icon ui-icon-alert\" title=\"Product download error: " + aData.productProgress.message + "\">!</span></span>");
 				}
 				var fileDownloadedSizeCell = $(nRow).find("td:eq(" + DownloadMonitor.productProgressDownloadedSizeColumnIndex + ")");
 				fileDownloadedSizeCell.html(DownloadMonitor.getReadableFileSizeString(aData.productProgress.downloadedSize));
            	 
 				var progressCell = $(nRow).find("td:eq(" + DownloadMonitor.productProgressProgressPercentageColumnIndex + ")");
 				progressCell.addClass("productProgress");
 				progressCell.html("<div class=\"progressbar\"><div class=\"progress-label\"></div></div>");
 				progressCell.find(".progressbar").progressbar({
 					value: Math.floor(aData.productProgress.progressPercentage),
 					create: function() {
 						$(this).children(".progress-label").text( $(this).progressbar( "value" ) + "%" );
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
		} else if(productProgress == "CANCELLED" || productProgress == "IN_ERROR") {
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
		$.getJSON("products/" + productUuid, { action : action })
		.done(function(response) {
			if(response.success === false) {
				DownloadMonitor.displayMessage("Error with sending product download command " + action + " for product uuid " + productUuid + ": " + response.message);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage("Error with sending product download command " + action + " for product uuid " + productUuid, jqXHR);
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
			DownloadMonitor.displayErrorMessage("Error with retrieving product status for DAR " + darUuid, jqXHR);
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
				var currentProductStatus = productDataTable.fnGetData(productRow, DownloadMonitor.productProgressStatusColumnIndex);
				var newProductStatus = product.productProgress.status;
				if(currentProductStatus != newProductStatus) {
					productDataTable.fnUpdate(newProductStatus, productRow, DownloadMonitor.productProgressStatusColumnIndex);
				}
				productDataTable.fnUpdate(product.productProgress.downloadedSize, productRow, DownloadMonitor.productProgressDownloadedSizeColumnIndex);
				productDataTable.fnUpdate(product.productProgress.progressPercentage, productRow, DownloadMonitor.productProgressProgressPercentageColumnIndex);
			}
		}

	},
	getReadableFileSizeString : function(fileSizeInBytes) {

	    var i = -1;
	    var byteUnits = [' KiB', ' MiB', ' GiB', ' TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
	    do {
	        fileSizeInBytes = fileSizeInBytes / 1024;
	        i++;
	    } while (fileSizeInBytes > 1024);

	    return Math.max(fileSizeInBytes, 0.0).toFixed(1) + byteUnits[i];
	},
	displayMessage : function(message) {
		$("#message").append("<p>" + message + "</p>");
	},
	displayErrorMessage : function(message, error) {
		if(error.status !== 0) {
			$("#message").append("<p>");
			$("#message").append(message + ": ");
	
			if(typeof error === 'object') {
				$("#message").append("Error: HTTP " + error.status + ", "+ error.statusText);
			}else{
				var errorObject = jQuery.parseJSON(error);
				$("#message").append(errorObject.response.message);
			}
			$("#message").append("</p>");
		}
	},
	clearActivityHistory : function(downloadStatusTable) {
		$.getJSON("clearActivityHistory")
		.done(function(response) {
			if(response.success === false) {
				DownloadMonitor.displayMessage("Error with clearing activity history: " + response.message);
			}else{
				DownloadMonitor.resetDownloadDisplay(downloadStatusTable);
			}
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			DownloadMonitor.displayErrorMessage("Download Failed! ", jqXHR.responseText);
		});
	},
	resetDownloadDisplay: function(downloadStatusTable) {
		$(downloadStatusTable).dataTable().fnClearTable();
		clearTimeout(DownloadMonitor.monitorDownloadStatusTimer);
		$.each(DownloadMonitor.monitorProductDownloadStatusTimer, function(key, value) {
			clearTimeout(value);
		});
		DownloadMonitor.monitorDownloadStatus(downloadStatusTable);
	}
};