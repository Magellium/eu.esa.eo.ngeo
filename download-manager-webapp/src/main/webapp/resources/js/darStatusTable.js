var DarStatusTable = {
	darExpandColumnIndex: 0,
	darURLColumnIndex: 1,
	darMonitoringStatusColumnIndex: 2,
	initialiseDarStatusTable : function(downloadStatusTable) {
		downloadStatusTable.dataTable({
			"sAjaxDataProp": "dataAccessRequests",
			"aoColumns": [
			              {
		                    	"mData": null, 
			                    "sDefaultContent": '<span class=\"darDetails ui-state-default ui-corner-all\"><span class=\"ui-icon ui-icon-plus\">+</span></span>',
			                    "sWidth": "25px"
		                  },
//			              { "mData": "uuid" },
			              { "mData": null },
			              { "mData": "monitoringStatus", "sWidth": "120px" }
			          ],
			"bAutoWidth": false,
			"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
				$(nRow).attr("data-dataAccessRequest-id", aData.uuid);

				DarStatusTable.formatDarCell(nRow, aData);
				DarStatusTable.formatDarStatusCell(nRow, aData);
				return nRow;
			},
            "bPaginate": false,
			"sPaginationType": "full_numbers",
			"bLengthChange": false,
			"bFilter" : false,
            "bInfo" : false,
            "bSort": false
		});
		$(downloadStatusTable).find('tbody').on('click', 'tr span.darDetails', function () {
			var rowToExpand = $(this).parents("tr").get(0);
			DarStatusTable.expandDataAccessRequestRow(downloadStatusTable, rowToExpand, this);
	    });
        DownloadMonitor.monitorDownloadStatus(downloadStatusTable);
	},
	displayDownloadStatus : function(downloadStatusTable, dataAccessRequestList) {
		for(var i = 0; i < dataAccessRequestList.length; i++) {
			var dataAccessRequestData = dataAccessRequestList[i];
			var dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + dataAccessRequestData.uuid + "']").get(0);
								
			if(typeof dataAccessRequestRow === 'undefined') {
				downloadStatusTable.dataTable().fnAddData(dataAccessRequestData);
				dataAccessRequestRow = downloadStatusTable.dataTable().$("tr[data-dataAccessRequest-id='" + dataAccessRequestData.uuid + "']").get(0);

				rowExpandImage = $(dataAccessRequestRow).find('span.darDetails');
				DarStatusTable.expandDataAccessRequestRow(downloadStatusTable, dataAccessRequestRow, rowExpandImage);
			}else{
				DarStatusTable.updateDownloadStatusRow(downloadStatusTable, dataAccessRequestData, dataAccessRequestRow);
			}
		}
	},
	updateDownloadStatusRow : function(downloadStatusTable, newDarData, dataAccessRequestRow) {
		oldDarData = downloadStatusTable.dataTable().fnGetData(dataAccessRequestRow);
		
		if(newDarData.monitoringStatus !== oldDarData.monitoringStatus) {
			downloadStatusTable.dataTable().fnUpdate(newDarData.monitoringStatus, dataAccessRequestRow, DarStatusTable.darMonitoringStatusColumnIndex, false, false);
			DarStatusTable.formatDarStatusCell(dataAccessRequestRow, newDarData);
		}
	},
	formatDarCell : function(nRow, darData) {
		var darCell = $(nRow).find("td:eq(" + DarStatusTable.darURLColumnIndex + ")");
		if(darData.darName !== undefined) {
				darCell.html(darData.darName);
		}else{
				darCell.html(darData.darURL);
		}
	},
	formatDarStatusCell : function(nRow, darData) {
		var darStatusCell = $(nRow).find("td:eq(" + DarStatusTable.darMonitoringStatusColumnIndex + ")");
		var darStatus;
			if(darData.monitored) {
  				darStatus = darData.monitoringStatus;
		}else{
			darStatus = "NA";
		}
		var darStatusTranslated = messages['dar_status.' + darStatus];
		darStatusCell.html(darStatusTranslated);
	},
	expandDataAccessRequestRow : function(downloadStatusTable, rowToExpand, rowExpandSpan) {
		if (downloadStatusTable.dataTable().fnIsOpen(rowToExpand))
        {
            /* This row is already open - close it */
			$(rowExpandSpan).find("span:first-child").toggleClass("ui-icon-minus ui-icon-plus");
            downloadStatusTable.dataTable().fnClose(rowToExpand);
        }else{
            /* Open this row */
			$(rowExpandSpan).find("span:first-child").toggleClass("ui-icon-plus ui-icon-minus");
            downloadStatusTable.dataTable().fnOpen(rowToExpand, ProductStatusTable.formatProductList(downloadStatusTable.dataTable(), rowToExpand), 'details' );
            var aData = downloadStatusTable.dataTable().fnGetData(rowToExpand);
            ProductStatusTable.initialiseProductListTable(aData);
            DownloadMonitor.monitorProductStatus(downloadStatusTable, aData.uuid);
        }
	}
};