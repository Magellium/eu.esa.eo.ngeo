package int_.esa.eo.ngeo.dmtu.manager;

import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.model.DataAccessRequest;
import int_.esa.eo.ngeo.dmtu.model.Product;
import int_.esa.eo.ngeo.dmtu.model.dao.DataAccessRequestDAO;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductSubject;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccess;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataAccessRequestManager implements ProductSubject {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessRequestManager.class);

	@Autowired
	private DataAccessRequestDAO dataAccessRequestDao;
	
	private List<ProductObserver> observers;
	private Map<String, DataAccessRequest> dataAccessRequestMap;
	
	private Map<String, String> productDarMap;

	private String manualDataAccessRequestUuid;

	public static final String MANUAL_DATA_REQUEST = "Manual Data Access Request";

	public DataAccessRequestManager() {
		this.dataAccessRequestMap = new LinkedHashMap<String, DataAccessRequest>();
		this.productDarMap = new HashMap<>();
		this.observers = new ArrayList<ProductObserver>();
	}

	public void loadDARs() {
		LOGGER.debug("Loading DARs from the database");
		List<DataAccessRequest> loadVisibleDars = this.dataAccessRequestDao.loadVisibleDars();
		for (DataAccessRequest dataAccessRequest : loadVisibleDars) {
			String uuid = dataAccessRequest.getUuid();
			LOGGER.debug(String.format("DAR: %s %s",uuid, dataAccessRequest.getMonitoringURL()));
			dataAccessRequestMap.put(uuid, dataAccessRequest);
			if(dataAccessRequest.getMonitoringURL().equals(MANUAL_DATA_REQUEST)) {
				manualDataAccessRequestUuid = uuid;
			}
			for (Product product : dataAccessRequest.getProductList()) {
				productDarMap.put(product.getUuid(), dataAccessRequest.getUuid());
				if(product.isVisible() && !product.isNotified()) {
					switch (product.getProductProgress().getStatus()) {
						case NOT_STARTED:
						case IDLE:
						case RUNNING:
						case PAUSED:
						case IN_ERROR:
						case CANCELLED:
							notifyObservers(product);
						break;
					default:
						break;
					}
				}
			}
		}
	}
	
	
	@Override
	public void registerObserver(ProductObserver o) {
		this.observers.add(o);
	}

	@Override
	public void removeObserver(ProductObserver o) {
		int i = observers.indexOf(o);
		if(i >= 0) {
			observers.remove(i);
		}
	}

	@Override
	public void notifyObservers(Product product) {
		for (ProductObserver o : observers) {
			o.update(product);
		}
	}

	private void notifyObservers(List<Product> productsAdded) {
		for (Product product : productsAdded) {
			notifyObservers(product);
		}
	}

	private void addNewProduct(DataAccessRequest dataAccessRequest, Product newProduct) {
		dataAccessRequest.addProduct(newProduct);
		productDarMap.put(newProduct.getUuid(), dataAccessRequest.getUuid());
	}
	
	public boolean addDataAccessRequest(URL monitoringUrl) throws DataAccessRequestAlreadyExistsException {
		DataAccessRequest retrievedDataAccessRequest = getDataAccessRequestByMonitoringUrl(monitoringUrl);
		if(retrievedDataAccessRequest != null) {
			throw new DataAccessRequestAlreadyExistsException(String.format("Data Access Request for url %s already exists.", monitoringUrl));
		}else{
			DataAccessRequest dataAccessRequest = new DataAccessRequest(monitoringUrl.toString());
			this.dataAccessRequestMap.put(dataAccessRequest.getUuid(), dataAccessRequest);
			dataAccessRequestDao.updateDataAccessRequest(dataAccessRequest);
			return true;
		}
	}
	
	public List<DataAccessRequest> getVisibleDARList(boolean includeManualDar) {
		List<DataAccessRequest> darList = new ArrayList<>();
		Collection<DataAccessRequest> dataAccessRequests = dataAccessRequestMap.values();
		for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
			//TODO: Write a unit test to ensure this is correct.
			//Include all visible DARs, except the manual DAR is specified.
			if(dataAccessRequest.isVisible() && (includeManualDar || !MANUAL_DATA_REQUEST.equals(dataAccessRequest.getMonitoringURL()))) {
				darList.add(dataAccessRequest);
			}
		}
		return darList;
	}

	public DataAccessRequest getDataAccessRequestByUuid(String darUuid) {
		DataAccessRequest dataAccessRequest = this.dataAccessRequestMap.get(darUuid);
		if(dataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Unable to find Data Access Request for uuid %s", darUuid));
		}
		return dataAccessRequest;
	}

	public DataAccessRequest getDataAccessRequestByMonitoringUrl(URL monitoringUrl) {
		Collection<DataAccessRequest> dataAccessRequests = this.dataAccessRequestMap.values();
		for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
			if(dataAccessRequest.getMonitoringURL().equals(monitoringUrl.toString())) {
				return dataAccessRequest;
			}
		}
		//a DAR with this monitoringUrl is not active, check if it is in the database
		return dataAccessRequestDao.getDarByMonitoringUrl(monitoringUrl.toString());
	}

	public List<Product> getProductList(String darUuid) {
		List<Product> displayableProductList = new ArrayList<>();
		DataAccessRequest dataAccessRequest = getDataAccessRequestByUuid(darUuid);
		List<Product> darProductList = dataAccessRequest.getProductList();
		for (Product product : darProductList) {
			if(product.isVisible()) {
				displayableProductList.add(product);
			}
		}
		return displayableProductList;
	}


	public boolean addManualProductDownload(String productDownloadUrl) throws ProductAlreadyExistsInDarException {
		DataAccessRequest manualDataAccessRequest;
		if(this.manualDataAccessRequestUuid == null) {
			manualDataAccessRequest = new DataAccessRequest(MANUAL_DATA_REQUEST);
			this.manualDataAccessRequestUuid = manualDataAccessRequest.getUuid();
			dataAccessRequestMap.put(manualDataAccessRequestUuid, manualDataAccessRequest);
		}else{
			manualDataAccessRequest = dataAccessRequestMap.get(manualDataAccessRequestUuid);

			Product productInDar = findProductInDar(manualDataAccessRequest, productDownloadUrl);
			if(productInDar != null) {
				throw new ProductAlreadyExistsInDarException(String.format("Product %s already exists in DAR %s", productDownloadUrl, manualDataAccessRequest.getMonitoringURL()));
			}
		}

		Product newProduct = new Product(productDownloadUrl);
		productDarMap.put(newProduct.getUuid(), manualDataAccessRequest.getUuid());

		addNewProduct(manualDataAccessRequest, newProduct);

		dataAccessRequestDao.updateDataAccessRequest(manualDataAccessRequest);
		notifyObservers(newProduct);
		return true;
	}
	
	private Product findProductInDar(DataAccessRequest dataAccessRequest, String productAccessUrl) {
		for (Product product : dataAccessRequest.getProductList()) {
			if(product.getProductAccessUrl().equals(productAccessUrl)) {
				return product;
			}
		}
		return null;
	}
	
	public void updateDataAccessRequest(URL darMonitoringUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessListObject) {
		DataAccessRequest retrievedDataAccessRequest = getDataAccessRequestByMonitoringUrl(darMonitoringUrl);
		if(retrievedDataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Data Access Request for url %s does not exist.", darMonitoringUrl));
		}
		
		retrievedDataAccessRequest.setMonitoringStatus(monitoringStatus);
		if(monitoringStatus == MonitoringStatus.IN_PROGRESS) {
			retrievedDataAccessRequest.setLastResponseDate(responseDate);

			if(productAccessListObject != null) {
				List<ProductAccess> productAccessList = productAccessListObject.getProductAccess();
				List<Product> newProductsAdded = new ArrayList<>();
				for (ProductAccess productAccess : productAccessList) {
					Product productInDar = findProductInDar(retrievedDataAccessRequest, productAccess.getProductAccessURL());
					if(productInDar != null) {
						//check if the product has been confirmed as notified
						Object downloadNotified = productAccess.getDownloadNotified();
						//XXX: this should be changed to pick up the date supplied
						if(downloadNotified != null && "CONFIRMED".equals(downloadNotified)) {
							LOGGER.debug("Product download has been confirmed as notified");
							productInDar.setNotified(true);
						}
					}else{
						Product newProduct = new Product(productAccess.getProductAccessURL(), productAccess.getProductDownloadDirectory());
						addNewProduct(retrievedDataAccessRequest, newProduct);
						newProductsAdded.add(newProduct);
					}
				}
	
				if(newProductsAdded.size() > 0) {
					dataAccessRequestDao.updateDataAccessRequest(retrievedDataAccessRequest);
					notifyObservers(newProductsAdded);
				}
			}
		}
	}
	
	public boolean clearActivityHistory() {
		for (Iterator<DataAccessRequest> iter = dataAccessRequestMap.values().iterator(); iter.hasNext();) {
			DataAccessRequest dataAccessRequest = iter.next();
			
		 	MonitoringStatus monitoringStatus = dataAccessRequest.getMonitoringStatus();
			if(monitoringStatus == MonitoringStatus.COMPLETED || monitoringStatus == MonitoringStatus.CANCELLED) {
				dataAccessRequest.setVisible(false);
				iter.remove();
				List<Product> productList = dataAccessRequest.getProductList();
				for (Product product: productList) {
					product.setVisible(false);
					productDarMap.remove(product.getUuid());
				}
			}else{
				List<Product> productList = dataAccessRequest.getProductList();
				for (Product product: productList) {
					EDownloadStatus productStatus = product.getProductProgress().getStatus();
					if(productStatus == EDownloadStatus.COMPLETED || productStatus == EDownloadStatus.CANCELLED || productStatus == EDownloadStatus.IN_ERROR) {
						product.setVisible(false);
					}
				}
			}
			dataAccessRequestDao.updateDataAccessRequest(dataAccessRequest);
		}
		return true;
	}
	
	public void persistProductStatusChange(String productUuid) {
		String darUuid = productDarMap.get(productUuid);
		DataAccessRequest dataAccessRequest = getDataAccessRequestByUuid(darUuid);
		for (Product product : dataAccessRequest.getProductList()) {
			if(product.getUuid().equals(productUuid)) {
				dataAccessRequestDao.updateProduct(product);
			}
		}
	}
}
