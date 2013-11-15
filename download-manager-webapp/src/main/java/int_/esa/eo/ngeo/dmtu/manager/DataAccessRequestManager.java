package int_.esa.eo.ngeo.dmtu.manager;

import int_.esa.eo.ngeo.dmtu.exception.DataAccessRequestAlreadyExistsException;
import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.dmtu.model.VisibleDataAccessRequests;
import int_.esa.eo.ngeo.dmtu.model.dao.DataAccessRequestDAO;
import int_.esa.eo.ngeo.dmtu.observer.ProductObserver;
import int_.esa.eo.ngeo.dmtu.observer.ProductSubject;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.status.ValidDownloadStatusForUserAction;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringStatus;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccess;
import int_.esa.eo.ngeo.iicd_d_ws._1.ProductAccessList;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataAccessRequestManager implements ProductSubject {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessRequestManager.class);

	@Autowired
	private DataAccessRequestDAO dataAccessRequestDao;
	private VisibleDataAccessRequests visibleDataAccessRequests;
	
	private List<ProductObserver> observers;

	public DataAccessRequestManager() {
		this.visibleDataAccessRequests = new VisibleDataAccessRequests();
		this.observers = new ArrayList<ProductObserver>();
	}

	public void loadDARs() {
		LOGGER.debug("Loading DARs from the database");
		List<DataAccessRequest> loadVisibleDars = this.dataAccessRequestDao.loadVisibleDars();
		for (DataAccessRequest dataAccessRequest : loadVisibleDars) {
			LOGGER.debug(String.format("DAR: %s %s", dataAccessRequest.getUuid(), dataAccessRequest.getDarURL()));
			visibleDataAccessRequests.addDAR(dataAccessRequest);
			
			for (Product product : dataAccessRequest.getProductList()) {
				visibleDataAccessRequests.addProductToProduct_DARMapping(product, dataAccessRequest);
				if(product.isVisible() && !product.isNotified()) {
					switch (product.getProductProgress().getStatus()) {
						case NOT_STARTED:
						case IDLE:
						case RUNNING:
						case PAUSED:
						case IN_ERROR:
						case CANCELLED:
							notifyObserversOfNewProduct(product);
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
	public void notifyObserversOfNewProduct(Product product) {
		for (ProductObserver o : observers) {
			o.newProduct(product);
		}
	}

	@Override
	public void notifyObserversOfProductStatusUpdate(Product product,	EDownloadStatus downloadStatus) {
		for (ProductObserver o : observers) {
			o.updateProductDownloadStatus(product, downloadStatus);
		}
	}

	public void notifyObserversOfChangeOfDARProductsStatus(List<Product> productList, EDownloadStatus downloadStatus) {
		for (Product product : productList) {
			notifyObserversOfProductStatusUpdate(product, downloadStatus);
		}
	}

	private void notifyObserversOfProductsAdded(List<Product> productsAdded) {
		for (Product product : productsAdded) {
			notifyObserversOfNewProduct(product);
		}
	}

	public boolean addDataAccessRequest(URL monitoringUrl, boolean monitored) throws DataAccessRequestAlreadyExistsException {
		DataAccessRequest retrievedDataAccessRequest = getDataAccessRequestByMonitoringUrl(monitoringUrl);
		if(retrievedDataAccessRequest != null) {
			throw new DataAccessRequestAlreadyExistsException(String.format("Data Access Request for url %s already exists.", monitoringUrl));
		}else{
			DataAccessRequest dataAccessRequest = new DataAccessRequestBuilder().buildDAR(monitoringUrl.toString(), monitored);
			visibleDataAccessRequests.addDAR(dataAccessRequest);
			dataAccessRequestDao.updateDataAccessRequest(dataAccessRequest);
			return true;
		}
	}
	
	public DataAccessRequest getDataAccessRequestByMonitoringUrl(URL monitoringUrl) {
		DataAccessRequest dataAccessRequestFromVisibleList = visibleDataAccessRequests.getDataAccessRequestByMonitoringUrl(monitoringUrl);
		if(dataAccessRequestFromVisibleList != null) {
			return dataAccessRequestFromVisibleList;
		}
		
		//a DAR with this monitoringUrl is not active, check if it is in the database
		return dataAccessRequestDao.getDarByMonitoringUrl(monitoringUrl.toString());
	}


	public boolean addManualProductDownload(String productDownloadUrl) throws ProductAlreadyExistsInDarException {
		Product newProduct = visibleDataAccessRequests.addManualProductDownload(productDownloadUrl);
		
		DataAccessRequest manualDataAccessRequest = findDataAccessRequestByProduct(newProduct);
		dataAccessRequestDao.updateDataAccessRequest(manualDataAccessRequest);
		notifyObserversOfNewProduct(newProduct);
		return true;
	}
	
	public DataAccessRequest findDataAccessRequestByProduct(Product product) {
		return visibleDataAccessRequests.findDataAccessRequestByProductUuid(product.getUuid());
	}

	public void updateDataAccessRequest(URL darMonitoringUrl, MonitoringStatus monitoringStatus, Date responseDate, ProductAccessList productAccessListObject) {
		DataAccessRequest retrievedDataAccessRequest = getDataAccessRequestByMonitoringUrl(darMonitoringUrl);
		if(retrievedDataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Data Access Request for url %s does not exist.", darMonitoringUrl));
		}
		
		MonitoringStatus previousMonitoringStatus = retrievedDataAccessRequest.getMonitoringStatus();
		retrievedDataAccessRequest.setLastResponseDate(new Timestamp(responseDate.getTime()));
		retrievedDataAccessRequest.setMonitoringStatus(monitoringStatus);

		List<Product> productsWhichCanBeUpdatedToNewStatus;
		switch (monitoringStatus) {
		case IN_PROGRESS:
			if(previousMonitoringStatus != MonitoringStatus.IN_PROGRESS) {
				productsWhichCanBeUpdatedToNewStatus = searchForProductsWhichCanBeUpdatedToNewStatus(retrievedDataAccessRequest.getProductList(), EDownloadStatus.NOT_STARTED);
				notifyObserversOfChangeOfDARProductsStatus(productsWhichCanBeUpdatedToNewStatus, EDownloadStatus.NOT_STARTED);
			}
			
			if(productAccessListObject != null) {
				List<ProductAccess> productAccessList = productAccessListObject.getProductAccesses();
				List<Product> newProductsAdded = new ArrayList<>();
				for (ProductAccess productAccess : productAccessList) {
					Product productInDar = visibleDataAccessRequests.findProductInDar(retrievedDataAccessRequest, productAccess.getProductAccessURL());
					if(productInDar != null) {
						//check if the product has been confirmed as notified
						Object downloadNotified = productAccess.getDownloadNotified();
						//XXX: this should be changed to pick up the date supplied
						if(downloadNotified != null && "CONFIRMED".equals(downloadNotified)) {
							LOGGER.debug("Product download has been confirmed as notified");
							productInDar.setNotified(true);
						}
					}else{
						Product newProduct = new ProductBuilder().buildProduct(productAccess.getProductAccessURL(), productAccess.getProductDownloadDirectory());
						visibleDataAccessRequests.addNewProduct(retrievedDataAccessRequest, newProduct);
						newProductsAdded.add(newProduct);
					}
				}
	
				if(newProductsAdded.size() > 0) {
					notifyObserversOfProductsAdded(newProductsAdded);
				}
			}
			dataAccessRequestDao.updateDataAccessRequest(retrievedDataAccessRequest);
			break;
		case PAUSED:
			productsWhichCanBeUpdatedToNewStatus = searchForProductsWhichCanBeUpdatedToNewStatus(retrievedDataAccessRequest.getProductList(), EDownloadStatus.PAUSED);
			notifyObserversOfChangeOfDARProductsStatus(productsWhichCanBeUpdatedToNewStatus, EDownloadStatus.PAUSED);
			break;
		case CANCELLED:
			productsWhichCanBeUpdatedToNewStatus = searchForProductsWhichCanBeUpdatedToNewStatus(retrievedDataAccessRequest.getProductList(), EDownloadStatus.CANCELLED);
			notifyObserversOfChangeOfDARProductsStatus(productsWhichCanBeUpdatedToNewStatus, EDownloadStatus.CANCELLED);
			break;
		case COMPLETED:
			break;
		}
	}
	
	private List<Product> searchForProductsWhichCanBeUpdatedToNewStatus(List<Product> darProductList, EDownloadStatus downloadStatus) {
		List<EDownloadStatus> validStatusesForUpdateToNewStatus = new ValidDownloadStatusForUserAction().getValidDownloadStatusesForNewStatus(downloadStatus);
		List<Product> productsToBeUpdated = new ArrayList<>();
		for (Product product : darProductList) {
			if(validStatusesForUpdateToNewStatus.contains(product.getProductProgress().getStatus())) {
				productsToBeUpdated.add(product);
			}
		}
		
		return productsToBeUpdated;
	}
		
	public boolean clearActivityHistory() {
		for (DataAccessRequest dataAccessRequest : visibleDataAccessRequests.getDARList(true)) {
		 	MonitoringStatus monitoringStatus = dataAccessRequest.getMonitoringStatus();
			if(monitoringStatus == MonitoringStatus.COMPLETED || monitoringStatus == MonitoringStatus.CANCELLED) {
				dataAccessRequest.setVisible(false);
				visibleDataAccessRequests.removeDAR(dataAccessRequest);
				List<Product> productList = dataAccessRequest.getProductList();
				for (Product product: productList) {
					product.setVisible(false);
					visibleDataAccessRequests.removeProductFromProduct_DARMapping(product);
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
	
	public void persistProduct(Product product) {
		dataAccessRequestDao.updateProduct(product);
	}
	
	public List<DataAccessRequest> getVisibleDARList(boolean includeManualProductDar) {
		return visibleDataAccessRequests.getDARList(includeManualProductDar);
	}
		
	public boolean isProductDownloadManual(String productUuid) {
		return visibleDataAccessRequests.isProductDownloadManual(productUuid);
	}

	public List<Product> getProductList(String darUuid) {
		return visibleDataAccessRequests.getProductList(darUuid);
	}
}
