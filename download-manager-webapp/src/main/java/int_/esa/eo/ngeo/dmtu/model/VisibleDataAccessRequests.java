package int_.esa.eo.ngeo.dmtu.model;

import int_.esa.eo.ngeo.dmtu.exception.ProductAlreadyExistsInDarException;
import int_.esa.eo.ngeo.downloadmanager.builder.DataAccessRequestBuilder;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;
import int_.esa.eo.ngeo.downloadmanager.model.DataAccessRequest;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VisibleDataAccessRequests {
	private Map<String, DataAccessRequest> dataAccessRequestMap;
	private Map<String, String> productDarMap;
	private String manualDataAccessRequestUuid;
	public static final String MANUAL_PRODUCT_DAR = "Manual Data Access Request";

	public VisibleDataAccessRequests() {
		this.dataAccessRequestMap = new LinkedHashMap<String, DataAccessRequest>();
		this.productDarMap = new HashMap<>();
	}
	
	public void addDAR(DataAccessRequest dataAccessRequest) {
		String uuid = dataAccessRequest.getUuid();
		dataAccessRequestMap.put(uuid, dataAccessRequest);
		if(dataAccessRequest.getDarURL().equals(MANUAL_PRODUCT_DAR)) {
			manualDataAccessRequestUuid = uuid;
		}
	}

	public void removeDAR(DataAccessRequest dataAccessRequest) {
		dataAccessRequestMap.remove(dataAccessRequest.getUuid());
	}

	public Product addManualProductDownload(String productDownloadUrl) throws ProductAlreadyExistsInDarException {
		DataAccessRequest manualDataAccessRequest;
		if(this.manualDataAccessRequestUuid == null) {
			manualDataAccessRequest = new DataAccessRequestBuilder().buildDAR(MANUAL_PRODUCT_DAR, false);
			this.manualDataAccessRequestUuid = manualDataAccessRequest.getUuid();
			dataAccessRequestMap.put(manualDataAccessRequestUuid, manualDataAccessRequest);
		}else{
			manualDataAccessRequest = dataAccessRequestMap.get(manualDataAccessRequestUuid);

			Product productInDar = findProductInDar(manualDataAccessRequest, productDownloadUrl);
			if(productInDar != null) {
				throw new ProductAlreadyExistsInDarException(String.format("Product %s already exists in DAR %s", productDownloadUrl, manualDataAccessRequest.getDarURL()));
			}
		}

		Product newProduct = new ProductBuilder().buildProduct(productDownloadUrl);
		addProductToProductDARMapping(newProduct, manualDataAccessRequest);

		addNewProduct(manualDataAccessRequest, newProduct);
		return newProduct;
	}
	
	public List<DataAccessRequest> getDARList(boolean includeManualProductDar) {
		List<DataAccessRequest> darList = new ArrayList<>();
		Collection<DataAccessRequest> dataAccessRequests = dataAccessRequestMap.values();
		for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
			//TODO: Write a unit test to ensure this is correct.
			//Include all visible DARs, except the manual DAR is specified.
			if(dataAccessRequest.isVisible() && (includeManualProductDar || !MANUAL_PRODUCT_DAR.equals(dataAccessRequest.getDarURL()))) {
				darList.add(dataAccessRequest);
			}
		}
		return darList;
	}

	public void addNewProduct(DataAccessRequest dataAccessRequest, Product newProduct) {
		dataAccessRequest.getProductList().add(newProduct);
		addProductToProductDARMapping(newProduct, dataAccessRequest);
	}

	public DataAccessRequest findDataAccessRequestByProductUuid(String productUuid) {
		String darUuid = productDarMap.get(productUuid);
		return findDataAccessRequestByUuid(darUuid);
	}

	public void addProductToProductDARMapping(Product product, DataAccessRequest dataAccessRequest) {
		productDarMap.put(product.getUuid(), dataAccessRequest.getUuid());
	}

	public void removeProductFromProductDARMapping(Product product) {
		productDarMap.remove(product.getUuid());
	}

	public DataAccessRequest findDataAccessRequestByUuid(String darUuid) {
		DataAccessRequest dataAccessRequest = this.dataAccessRequestMap.get(darUuid);
		if(dataAccessRequest == null) {
			throw new NonRecoverableException(String.format("Unable to find Data Access Request for uuid %s", darUuid));
		}
		return dataAccessRequest;
	}

	public DataAccessRequest getDataAccessRequestByMonitoringUrl(URL monitoringUrl) {
		Collection<DataAccessRequest> dataAccessRequests = this.dataAccessRequestMap.values();
		for (DataAccessRequest dataAccessRequest : dataAccessRequests) {
			if(dataAccessRequest.getDarURL().equals(monitoringUrl.toString())) {
				return dataAccessRequest;
			}
		}
		//a DAR with this monitoringUrl is not active
		return null;
	}

	public List<Product> getProductList(String darUuid) {
		List<Product> displayableProductList = new ArrayList<>();
		DataAccessRequest dataAccessRequest = findDataAccessRequestByUuid(darUuid);
		List<Product> darProductList = dataAccessRequest.getProductList();
		for (Product product : darProductList) {
			if(product.isVisible()) {
				displayableProductList.add(product);
			}
		}
		return displayableProductList;
	}
	
	public Product findProductInDar(DataAccessRequest dataAccessRequest, String productAccessUrl) {
		for (Product product : dataAccessRequest.getProductList()) {
			if(product.getProductAccessUrl().equals(productAccessUrl)) {
				return product;
			}
		}
		return null;
	}
	
	public boolean isProductDownloadManual(String productUuid) {
		//If no manual DAR has been created, then we can be certain that the product download is not manual
		if(manualDataAccessRequestUuid == null) {
			return false;
		}
		List<Product> manualProductList = getProductList(manualDataAccessRequestUuid);
		for (Product product : manualProductList) {
			if(product.getUuid().equals(productUuid)) {
				return true;
			}
		}
		return false;
	}
}
