var ProductStatusTable = {
	productAccessURLColumnIndex: 0,
	productPriorityColumnIndex: 1,
    productTotalFileSizeColumnIndex: 2,
    productProgressDownloadedSizeColumnIndex: 3,
    productProgressProgressPercentageColumnIndex: 4,
    productProgressStatusColumnIndex: 5,
    productActionsColumnIndex: 6,
    productMessageColumnIndex: 7,
	formatProductList : function(tableData, row ) {
	    var aData = tableData.fnGetData( row );
	    var sOut = '<table id="productList' + aData.uuid + '" style="padding-left:30px;"><thead><tr>';
	    sOut += '<th class="productAccessURL">' + messages['product_table.heading.product_access_url'] + '</th>';
	    sOut += '<th class="productAccessURL">' + messages['product_table.heading.priority'] + '</th>';
	    sOut += '<th class="productDownloadedSize">' + messages['product_table.heading.total_size'] + '</th>';
	    sOut += '<th class="productDownloadedSize">' + messages['product_table.heading.downloaded_size'] + '</th>';
	    sOut += '<th class="productProgress">' + messages['product_table.heading.progress'] + '</th>';
	    sOut += '<th class="productStatus">' + messages['product_table.heading.product_status'] + '</th>';
	    sOut += '<th class="productActions">' + messages['product_table.heading.actions'] + '</th>';
	    sOut += '<th class="productMessage"></th>';
	    sOut += '</tr></thead></table>';
	    return sOut;
	},
	initialiseProductListTable : function(aData) {
        $("#productList"+aData.uuid).dataTable({
    		"aoColumns": [
			              { "mData": "productAccessUrl" },
			              { "mData": "priority", 							"sWidth": "90px"  },
			              { "mData": "totalFileSize", 						"sWidth": "70px"  },
			              { "mData": "productProgress.downloadedSize", 		"sWidth": "70px" },
			              { "mData": "productProgress.progressPercentage", 	"sWidth": "120px"  },
			              { "mData": "productProgress.status", 				"sWidth": "70px", "bSortable": false  },
			              { "mData": null, 									"sWidth": "50px", "bSortable": false  },
			              { "mData": "productProgress.message", "bVisible": false, "sDefaultContent" : ""}
			          ],
			 "bAutoWidth": false,
        	 "fnRowCallback": function( nRow, aData, iDisplayIndex ) {
 				$(nRow).attr("data-product-id", aData.uuid);
 				
 				ProductStatusTable.formatPriorityCell(nRow, aData);
  				ProductStatusTable.formatProductAccessUrlCell(nRow, aData);
  				ProductStatusTable.formatProductStatusCell(nRow, aData);
  				ProductStatusTable.formatTotalFileSizeCell(nRow, aData);
  				ProductStatusTable.formatDownloadedFileSizeCell(nRow, aData);
  				ProductStatusTable.formatProgressPercentageCell(nRow, aData);
  				ProductStatusTable.formatProductActionsCell(nRow, aData);
 			},
			 "bPaginate": false,
             "bFilter" : false,
             "bInfo" : false,
             "bSort": true
		});
	},
	getActions : function(product) {
		var productProgress = product.productProgress.status;
		var actionsHtml = '<ul id="actions" class="ui-widget ui-helper-clearfix">';
		if(productProgress == "NOT_STARTED" || productProgress == "IDLE" || productProgress == "RUNNING") {
			actionsHtml += ProductStatusTable.displayAction("default", "pause", product.uuid, productProgress);
			actionsHtml += ProductStatusTable.displayAction("default", "cancel", product.uuid, productProgress);
		} else if(productProgress == "PAUSED") {
			actionsHtml += ProductStatusTable.displayAction("default", "resume", product.uuid, productProgress);
			actionsHtml += ProductStatusTable.displayAction("default", "cancel", product.uuid, productProgress);
		} else if(productProgress == "IN_ERROR") {
			actionsHtml += ProductStatusTable.displayAction("default", "resume", product.uuid, productProgress);
		}
		actionsHtml += '</ul>';

		return actionsHtml;
	},
	displayAction: function(state, actionType, productUuid, productProgress) {
		var actionTooltip = actionType.charAt(0).toUpperCase() + actionType.slice(1);
		var iconClass = "ui-icon-";
		if(actionType === "pause") {
			iconClass += "pause";
		}else if(actionType === "resume") {
			if(productProgress === "IN_ERROR") {
				iconClass += "arrowrefresh-1-e";
				var actionTooltip = "Retry";
			}else{
				iconClass += "play";
			}
		}else if(actionType === "cancel") {
			iconClass += "stop";
		}
		var actionHtml = '<li class="ui-state-' + state + ' ui-corner-all">';
		if(state === "default") {
			actionHtml += '<a href=\'javascript:DownloadMonitor.productDownloadCommand("' + productUuid + '","' + actionType + '");\'>';
		}
		actionHtml += '<span class="ui-icon ' + iconClass + '" title="' + actionTooltip + '"></span>';
		if(state === "default") {
			actionHtml += '</a>';
		}
		actionHtml += '</li>';
		return actionHtml;
	},
	displayProductDetails : function(downloadStatusTable, darUuid, productList) {
		var productDataTable = $("#productList"+darUuid).dataTable();
		for(var i = 0; i < productList.length; i++) {
			var product = productList[i];
			var productRow = productDataTable.$("tr[data-product-id='" + product.uuid + "']").get(0);
								
			if(typeof productRow === 'undefined') {
				productDataTable.fnAddData(product);
			}else{
				ProductStatusTable.updateProductStatusRow(productDataTable, product, productRow);
			}
		}
	},
	updateProductStatusRow : function(productDataTable, newProductData, productRow) {
		oldProductData = productDataTable.dataTable().fnGetData(productRow);
		var priorityUpdated = false;
		
		if(newProductData.priority !== oldProductData.priority) {
			productDataTable.fnUpdate(newProductData.priority, productRow, ProductStatusTable.productPriorityColumnIndex, false, false);
			ProductStatusTable.formatPriorityCell(productRow, newProductData);
			priorityUpdated = true;
		}
		if(newProductData.totalFileSize !== oldProductData.totalFileSize) {
			productDataTable.fnUpdate(newProductData.totalFileSize, productRow, ProductStatusTable.productTotalFileSizeColumnIndex, false, false);
			ProductStatusTable.formatTotalFileSizeCell(productRow, newProductData);
		}
		if(newProductData.productProgress.downloadedSize !== oldProductData.productProgress.downloadedSize) {
			productDataTable.fnUpdate(newProductData.productProgress.downloadedSize, productRow, ProductStatusTable.productProgressDownloadedSizeColumnIndex, false, false);
			ProductStatusTable.formatDownloadedFileSizeCell(productRow, newProductData);
		}
		if(newProductData.productProgress.status !== oldProductData.productProgress.status) {
			productDataTable.fnUpdate(newProductData.productProgress.status, productRow, ProductStatusTable.productProgressStatusColumnIndex, false, false);
			//update product progress message, just in case it is needed to display the error
			productDataTable.fnUpdate(newProductData.productProgress.message, productRow, ProductStatusTable.productMessageColumnIndex, false, false);
			if(!priorityUpdated) {
				productDataTable.fnUpdate(newProductData.priority, productRow, ProductStatusTable.productPriorityColumnIndex, false, false);
				ProductStatusTable.formatPriorityCell(productRow, newProductData);
			}
			ProductStatusTable.formatProductStatusCell(productRow, newProductData);
			ProductStatusTable.formatProductActionsCell(productRow, newProductData);
		}
		
		if(newProductData.productProgress.progressPercentage !== oldProductData.productProgress.progressPercentage) {
			productDataTable.fnUpdate(newProductData.productProgress.progressPercentage, productRow, ProductStatusTable.productProgressProgressPercentageColumnIndex, false, false);
			ProductStatusTable.formatProgressPercentageCell(productRow, newProductData);
		}
	},
	formatProductAccessUrlCell : function(nRow, productData) {
		var productAccessUrlCell = $(nRow).find("td:eq(" + ProductStatusTable.productAccessURLColumnIndex + ")");
		productAccessUrlCell.html(decodeURIComponent(productData.productAccessUrl));
		productAccessUrlCell.attr("title", decodeURIComponent(productData.productAccessUrl));
		productAccessUrlCell.addClass("productAccessUrlCell");
	},
	formatPriorityCell : function(nRow, productData) {
		var priorityCell = $(nRow).find("td:eq(" + ProductStatusTable.productPriorityColumnIndex + ")");
		var productPriority = productData.priority;
		var productUuid = productData.uuid;
		var productStatus = productData.productProgress.status;
		var priorityArray = ["Very Low", "Low", "Normal", "High", "Very High"];
		
		var iconPrioritySpan = priorityCell.find(".iconPriority");
		iconPrioritySpan.remove();

		if(productStatus != "IN_ERROR" && productStatus != "CANCELLED" && productStatus != "COMPLETED") {
			//Display change in priority buttons - first work out the priority
			var currentPriorityIndex = $.inArray(productPriority, priorityArray);
			
			var buttonsHtml = "";
			
			//since the buttons are floated right, the decrease priority button is rendered first, to be placed on the right
			if(currentPriorityIndex > 0) {
				var decreasePriorityName = priorityArray[currentPriorityIndex - 1].toUpperCase().replace(" ", "_");
				
				buttonsHtml += "<span class=\"iconPriority ui-state-default ui-corner-all\">";
				buttonsHtml += '<a href=\'javascript:DownloadMonitor.changePriority("' + productUuid + '","' + decreasePriorityName + '");\'>';
				buttonsHtml += "<span class=\"ui-icon ui-icon-arrowthick-1-s\" title=\"Decrease priority\" alt=\"Decrease priority\">!</span>";
				buttonsHtml += "</a>";
				buttonsHtml += "</span>";
			}else{
				buttonsHtml += "<span class=\"iconPriority ui-state-disabled ui-corner-all\">";
				buttonsHtml += "<span class=\"ui-icon ui-icon-arrowthick-1-n\">!</span>";
				buttonsHtml += "</span>";
			}

			if(currentPriorityIndex < priorityArray.length -1) {
				var increasePriorityName = priorityArray[currentPriorityIndex + 1].toUpperCase().replace(" ", "_");

				buttonsHtml += "<span class=\"iconPriority ui-state-default ui-corner-all\">";
				buttonsHtml += '<a href=\'javascript:DownloadMonitor.changePriority("' + productUuid + '","' + increasePriorityName + '");\'>';
				buttonsHtml += "<span class=\"ui-icon ui-icon-arrowthick-1-n\" title=\"Increase priority\" alt=\"Increase priority\">!</span>";
				buttonsHtml += "</a>";
				buttonsHtml += "</span>";
			}else{
				buttonsHtml += "<span class=\"iconPriority ui-state-disabled ui-corner-all\">";
				buttonsHtml += "<span class=\"ui-icon ui-icon-arrowthick-1-s\">!</span>";
				buttonsHtml += "</span>";
			}

			priorityCell.append(buttonsHtml);
		}
	},
	formatProductStatusCell : function(nRow, productData) {
		var fileProductStatus = $(nRow).find("td:eq(" + ProductStatusTable.productProgressStatusColumnIndex + ")");
		var productStatus = productData.productProgress.status;
		productStatusTranslated = messages['download_status.' + productStatus];
		fileProductStatus.html(productStatusTranslated);
		if(productStatus == "IN_ERROR") {
			var iconWarning = fileProductStatus.find(".iconWarning");
			iconWarning.remove();
			fileProductStatus.append("<span class=\"iconWarning ui-state-default ui-corner-all\"><span class=\"ui-icon ui-icon-alert\" title=\"" + messages['error.product_download'] + ": " + productData.productProgress.message + "\" alt=\"" + messages['error.product_download'] + ": " + productData.productProgress.message + "\">!</span></span>");
		}
	},
	formatTotalFileSizeCell : function(nRow, productData) {
		var fileTotalFileSizeCell = $(nRow).find("td:eq(" + ProductStatusTable.productTotalFileSizeColumnIndex + ")");
		fileTotalFileSizeCell.html(ProductStatusTable.getReadableFileSizeString(productData.totalFileSize));
	},
	formatDownloadedFileSizeCell : function(nRow, productData) {
		var fileDownloadedSizeCell = $(nRow).find("td:eq(" + ProductStatusTable.productProgressDownloadedSizeColumnIndex + ")");
		fileDownloadedSizeCell.html(ProductStatusTable.getReadableFileSizeString(productData.productProgress.downloadedSize));
	},
	formatProgressPercentageCell : function(nRow, productData) {
		var progressCell = $(nRow).find("td:eq(" + ProductStatusTable.productProgressProgressPercentageColumnIndex + ")");
		progressCell.addClass("productProgress");
		progressCell.html("<div class=\"progressbar\"><div class=\"progress-label\"></div></div>");
		progressCell.find(".progressbar").progressbar({
			value: Math.floor(productData.productProgress.progressPercentage),
			create: function() {
				if(productData.totalFileSize === -1 && productData.productProgress.progressPercentage === -1) {
					$(this).children(".progress-label").text( messages['label.unknown'] );
				}else{
					$(this).children(".progress-label").text( $(this).progressbar( "value" ) + "%" );
				}
			}
		});
	},
	formatProductActionsCell : function(nRow, productData) {
		var actionsCell = $(nRow).find("td:eq(" + ProductStatusTable.productActionsColumnIndex + ")");
		actionsCell.html(ProductStatusTable.getActions(productData));
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
	}
};