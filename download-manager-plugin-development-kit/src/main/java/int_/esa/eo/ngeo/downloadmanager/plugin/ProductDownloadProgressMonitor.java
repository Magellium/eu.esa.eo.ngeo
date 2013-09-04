package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.thread.AbortableFileDownload;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDownloadProgressMonitor implements FilesDownloadProgressListener {
	private static final double PERCENTAGE = 100.0;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDownloadProgressMonitor.class);

	private List<AbortableFileDownload> fileDownloadList;
	private Map<String, Long> fileDownloadProgressMap;
	private Map<String, Path> fileDownloadCompleteMap;
	private IProductDownloadListener productDownloadListener;
	private long totalFileSize, totalFileDownloadedSize;
	private int percentageComplete;
	private EDownloadStatus status;
	private EDownloadStatus statusWhenDownloadWasAborted;
	private String message;
	
	public ProductDownloadProgressMonitor(IProductDownloadListener productDownloadListener) {
		this.status = EDownloadStatus.NOT_STARTED;
		this.productDownloadListener = productDownloadListener;
		this.fileDownloadProgressMap = new HashMap<>();
		this.fileDownloadCompleteMap = new HashMap<>();
		this.fileDownloadList = new ArrayList<>();
	}

	@Override
	public synchronized void notifySomeBytesTransferred(String fileDownloadMetadataUuid, long numberOfBytes) {
		Long currentFileDownloadProgress = fileDownloadProgressMap.get(fileDownloadMetadataUuid);
		if(currentFileDownloadProgress == null) {
			fileDownloadProgressMap.put(fileDownloadMetadataUuid, numberOfBytes);
		}else{
			fileDownloadProgressMap.put(fileDownloadMetadataUuid, currentFileDownloadProgress + numberOfBytes);
		}
		updateProductFileDownloadStatus();
	}

	@Override
	public void notifyOfFileStatusChange(EDownloadStatus fileDownloadStatus, String message) {
		switch (fileDownloadStatus) {
		case IN_ERROR:
			if(statusWhenDownloadWasAborted == null || statusWhenDownloadWasAborted != EDownloadStatus.IN_ERROR) {
				abortFileDownloads(fileDownloadStatus);
			}
			this.message = message;
			break;
		default:
			break;
		}
	}

	public void abortFileDownloads(EDownloadStatus downloadStatus) {
		for (AbortableFileDownload fileDownload: fileDownloadList) {
			fileDownload.abortFileDownload(downloadStatus);
		}
		setStatusWhenDownloadWasAborted(downloadStatus);
	}

	@Override
	public void notifyOfCompletedPath(String fileDownloadMetadataUuid, Path completedPath) {
		fileDownloadCompleteMap.put(fileDownloadMetadataUuid, completedPath);
	}
	
	
	public void updateProductFileDownloadStatus() {
		long totalBytesDownloaded = 0;
		for (Long fileDownloadProgress : fileDownloadProgressMap.values()) {
			totalBytesDownloaded += fileDownloadProgress;
		}
		setTotalFileDownloadedSize(totalBytesDownloaded);
		int floor = (int) Math.floor((totalBytesDownloaded * PERCENTAGE) / totalFileSize);
		setPercentageComplete(floor);
		notifyProgressListener();
	}

	public void setTotalFileSize(List<FileDownloadMetadata> fileDownloadMetadataList) {
		totalFileSize = 0;
		for (FileDownloadMetadata fileDownloadMetadata : fileDownloadMetadataList) {
			totalFileSize += fileDownloadMetadata.getDownloadSize();
		}
	}
	
	public synchronized long getTotalFileDownloadedSize() {
		return totalFileDownloadedSize;
	}

	public synchronized void setTotalFileDownloadedSize(long totalFileDownloadedSize) {
		this.totalFileDownloadedSize = totalFileDownloadedSize;
	}
	
	public synchronized int getPercentageComplete() {
		return percentageComplete;
	}

	public synchronized void setPercentageComplete(int percentageComplete) {
		this.percentageComplete = percentageComplete;
	}

	public synchronized EDownloadStatus getStatus() {
		return status;
	}

	public void setStatus(EDownloadStatus downloadStatus) {
		setStatus(downloadStatus, null);
	}

	public synchronized void setStatus(EDownloadStatus downloadStatus, String message) {
		this.status = downloadStatus;
		this.message = message;
		notifyProgressListener();
	}

	public void confirmCancelAfterTidyUp() {
		this.fileDownloadProgressMap.clear();
		setPercentageComplete(0);
		setTotalFileDownloadedSize(0);
		setStatus(EDownloadStatus.CANCELLED);
	}

	private void notifyProgressListener() {
		productDownloadListener.progress(getPercentageComplete(), getTotalFileDownloadedSize(), getStatus(), message);
	}


	public boolean isDownloadComplete(String fileDownloadMetadataUuid) {
		return fileDownloadCompleteMap.get(fileDownloadMetadataUuid) != null;
	}
	
	public int getNumberOfCompletedFiles() {
		return fileDownloadCompleteMap.size();
	}
	
	public void setError(Exception ex) {
		setStatus(EDownloadStatus.IN_ERROR, ex.getLocalizedMessage());
		LOGGER.error(ex.getLocalizedMessage(), ex); 
	}

	public List<AbortableFileDownload> getFileDownloadList() {
		return fileDownloadList;
	}

	public EDownloadStatus getStatusWhenDownloadWasAborted() {
		return statusWhenDownloadWasAborted;
	}

	public void setStatusWhenDownloadWasAborted(
			EDownloadStatus statusWhenDownloadWasAborted) {
		this.statusWhenDownloadWasAborted = statusWhenDownloadWasAborted;
	}
}
