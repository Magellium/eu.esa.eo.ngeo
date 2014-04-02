package int_.esa.eo.ngeo.downloadmanager.controller;

import int_.esa.eo.ngeo.downloadmanager.dar.DataAccessRequestManager;
import int_.esa.eo.ngeo.downloadmanager.download.DownloadMonitor;
import int_.esa.eo.ngeo.downloadmanager.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.downloadmanager.exception.DownloadOperationException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.WebServerMonitoringStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.rest.StatusResponse;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.UserOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DARMonitorController {
    private DataAccessRequestManager dataAccessRequestManager;
    private DownloadMonitor downloadMonitor;
    private WebServerMonitoringStatus webServerMonitoringStatus; 

    public DARMonitorController(DataAccessRequestManager dataAccessRequestManager, DownloadMonitor downloadMonitor) {
        this.dataAccessRequestManager = dataAccessRequestManager;
        this.downloadMonitor = downloadMonitor;
        this.webServerMonitoringStatus = new WebServerMonitoringStatus();
    }
    
    public StatusResponse getDataAccessRequestStatus(boolean includeManualProductDar) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.setDataAccessRequests(dataAccessRequestManager.getVisibleDARList(includeManualProductDar));
        return statusResponse;
    }

    public boolean sendUserOrder(UserOrder userOrder) throws DownloadOperationException {
        return sendUserOrder(userOrder, false);
    }
    public boolean sendUserOrder(UserOrder userOrder, boolean includeManualDownloads) throws DownloadOperationException {
        setDarMonitoringRunning(false);

        List<EDownloadStatus> statusesToCancel = new ArrayList<>();
        statusesToCancel.add(EDownloadStatus.NOT_STARTED);
        statusesToCancel.add(EDownloadStatus.IDLE);
        statusesToCancel.add(EDownloadStatus.PAUSED);

        if(userOrder == UserOrder.STOP_IMMEDIATELY) {
            statusesToCancel.add(EDownloadStatus.RUNNING);
        }
        return downloadMonitor.cancelDownloadsWithStatuses(statusesToCancel, includeManualDownloads);
    }

    public boolean isDarMonitoringRunning() {
        return webServerMonitoringStatus.isDarMonitoringRunning();
    }

    public void setDarMonitoringRunning(boolean darMonitoringRunning) {
        webServerMonitoringStatus.setDarMonitoringRunning(darMonitoringRunning);
    }
    
    public boolean addDataAccessRequest(DataAccessRequest dar) throws DataAccessRequestAlreadyExistsException {
        return dataAccessRequestManager.addDataAccessRequest(dar);
    }
    
    public void updateDataAccessRequest(DataAccessRequest dar, DataAccessMonitoringResp dataAccessMonitoringResponse, Date responseDate, ProductPriority priority) {
        dataAccessRequestManager.updateDataAccessRequest(dar, dataAccessMonitoringResponse, responseDate, priority);
    }

    public DataAccessRequest getDataAccessRequestByMonitoringUrl(String monitoringUrl) {
        DataAccessRequest searchDar = new DataAccessRequest();
        searchDar.setDarURL(monitoringUrl);

        return dataAccessRequestManager.getDataAccessRequest(searchDar);
    }
}
