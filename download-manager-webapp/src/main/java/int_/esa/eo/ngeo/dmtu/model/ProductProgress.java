package int_.esa.eo.ngeo.dmtu.model;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class ProductProgress implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer progressPercentage;
	private Long downloadedSize;
	
	@Enumerated(EnumType.STRING)
	private EDownloadStatus status;
	private String message;
	
	/*
	 * Default constructor needed for hibernate, not used by application
	 */
	public ProductProgress() {}
	
	public ProductProgress(Integer progressPercentage, Long downloadedSize, EDownloadStatus status, String message) {
		this.progressPercentage = progressPercentage;
		this.downloadedSize = downloadedSize;
		this.status = status;
		this.message = message;
	}

	public Integer getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(Integer progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public Long getDownloadedSize() {
		return downloadedSize;
	}

	public void setDownloadedSize(Long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

	public EDownloadStatus getStatus() {
		return status;
	}

	public void setStatus(EDownloadStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
