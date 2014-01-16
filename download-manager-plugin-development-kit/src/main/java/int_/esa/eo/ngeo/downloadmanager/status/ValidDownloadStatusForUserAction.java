package int_.esa.eo.ngeo.downloadmanager.status;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.util.ArrayList;
import java.util.List;

public class ValidDownloadStatusForUserAction {

    public List<EDownloadStatus> getValidDownloadStatusesToExecutePauseAction() {
        return getValidDownloadStatusesForNewStatus(EDownloadStatus.PAUSED);
    }

    public List<EDownloadStatus> getValidDownloadStatusesToExecuteResumeAction() {
        return getValidDownloadStatusesForNewStatus(EDownloadStatus.NOT_STARTED);
    }

    public List<EDownloadStatus> getValidDownloadStatusesToExecuteCancelAction() {
        return getValidDownloadStatusesForNewStatus(EDownloadStatus.CANCELLED);
    }

    public List<EDownloadStatus> getValidDownloadStatusesForNewStatus(EDownloadStatus downloadStatus) {
        List<EDownloadStatus> statusesToUpdate = new ArrayList<>();
        statusesToUpdate.add(EDownloadStatus.IDLE);

        switch (downloadStatus) {
        case NOT_STARTED:
            statusesToUpdate.add(EDownloadStatus.NOT_STARTED);
            statusesToUpdate.add(EDownloadStatus.PAUSED);
            statusesToUpdate.add(EDownloadStatus.IN_ERROR);
            break;
        case PAUSED:
            statusesToUpdate.add(EDownloadStatus.NOT_STARTED);
            statusesToUpdate.add(EDownloadStatus.RUNNING);
            break;
        case CANCELLED:
            statusesToUpdate.add(EDownloadStatus.NOT_STARTED);
            statusesToUpdate.add(EDownloadStatus.RUNNING);
            statusesToUpdate.add(EDownloadStatus.PAUSED);
            break;
        default:
            break;
        }

        return statusesToUpdate;
    }

}
