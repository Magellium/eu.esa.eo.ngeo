package int_.esa.eo.ngeo.dmtu.model;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="product")
public class Product implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String productAccessUrl;
	private String downloadDirectory;
	
	@Id
	@Column(name="product_id")
	private String uuid;
	
	private long fileSize;

	private String fileName;

	@Embedded
	private ProductProgress productProgress;
	
	private boolean notified;

	private boolean visible;
	
	/*
	 * Default constructor needed for hibernate, not used by application
	 */
	public Product(){}
	
	public Product(String productAccessUrl) {
		this(productAccessUrl, null);
	}

	public Product(String productAccessUrl, String downloadDirectory) {
		this.productAccessUrl = productAccessUrl;
		this.uuid = UUID.randomUUID().toString();
		this.notified = false;
		ProductProgress productProgress = new ProductProgress(0, null, EDownloadStatus.NOT_STARTED, null);
		this.productProgress = productProgress;
		this.downloadDirectory = downloadDirectory;
		this.visible = true;
	}
	
	public String getProductAccessUrl() {
		return productAccessUrl;
	}

	public String getUuid() {
		return uuid;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ProductProgress getProductProgress() {
		return productProgress;
	}

	public void setProductProgress(ProductProgress productProgress) {
		this.productProgress = productProgress;
	}

	public boolean isNotified() {
		return notified;
	}

	public void setNotified(boolean notified) {
		this.notified = notified;
	}

	public String getDownloadDirectory() {
		return downloadDirectory;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
