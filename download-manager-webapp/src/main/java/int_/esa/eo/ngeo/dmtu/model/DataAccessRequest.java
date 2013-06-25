package int_.esa.eo.ngeo.dmtu.model;


import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name="data_access_request")
public class DataAccessRequest implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="data_access_request_id")
	private String uuid;

	private String monitoringURL;
	
	@Enumerated(EnumType.STRING)
	private MonitoringStatus monitoringStatus;
	
	@OneToMany(fetch = FetchType.EAGER)
	@JoinTable(
				name="data_access_request_product",
				joinColumns = @JoinColumn(name="data_access_request_id"),
				inverseJoinColumns = @JoinColumn(name="product_id"))
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	private List<Product> productList;
	
	private Timestamp lastResponseDate;
	
	private boolean visible;

	/*
	 * Default constructor needed for hibernate, not used by application
	 */
	public DataAccessRequest() {}
	
	public DataAccessRequest(String monitoringURL) {
		this.monitoringURL = monitoringURL;
		this.uuid = UUID.randomUUID().toString();
		this.productList = new ArrayList<Product>();
		this.setMonitoringStatus(MonitoringStatus.IN_PROGRESS);
		this.setVisible(true);
	}
	
	public List<Product> getProductList() {
		return productList;
	}
	
	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}

	public void addProduct(Product product) {
		productList.add(product);
	}

	public String getUuid() {
		return uuid;
	}

	public String getMonitoringURL() {
		return monitoringURL;
	}

	public MonitoringStatus getMonitoringStatus() {
		return monitoringStatus;
	}

	public void setMonitoringStatus(MonitoringStatus monitoringStatus) {
		this.monitoringStatus = monitoringStatus;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		if(!visible) {
			for (Product product : productList) {
				product.setVisible(false);
			}
		}
	}

	public Date getLastResponseDate() {
		return (this.lastResponseDate == null) ? null : new Date(this.lastResponseDate.getTime());
	}

	public void setLastResponseDate(Date lastResponseDate) {
		this.lastResponseDate = new java.sql.Timestamp(lastResponseDate.getTime());
	}
}
