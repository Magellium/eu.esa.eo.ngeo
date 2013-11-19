package int_.esa.eo.ngeo.dmtu.model;

import int_.esa.eo.ngeo.dmtu.exception.ProductNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveProducts {
	private final Map<String, IDownloadProcess> downloadProcessList;
	private final Map<String, Product> productToDownloadList;

	public ActiveProducts() {
		this.downloadProcessList = new ConcurrentHashMap<>();
		this.productToDownloadList = new ConcurrentHashMap<>();
	}
	
	public void addProduct(Product product, IDownloadProcess downloadProcess) {
		getProductToDownloadList().put(product.getUuid(), product);
		getDownloadProcessList().put(product.getUuid(), downloadProcess);
		
	}

	public void removeProduct(Product product) {
		getDownloadProcessList().remove(product.getUuid());
		getProductToDownloadList().remove(product.getUuid());
	}

	public IDownloadProcess getDownloadProcess(String productUuid) throws ProductNotFoundException {
		IDownloadProcess downloadProcess = getDownloadProcessList().get(productUuid);
		if(downloadProcess == null) {
			throw new ProductNotFoundException(String.format("Unable to find product with UUID %s. This product may have already been completed.", productUuid));
		}
		return downloadProcess;
	}

	public Product getProduct(String productUuid) throws ProductNotFoundException {
		Product product = getProductToDownloadList().get(productUuid);
		if(product == null) {
			throw new ProductNotFoundException(String.format("Unable to find product with UUID %s. This product may have already been completed.", productUuid));
		}
		return product;
	}
	
	public Collection<IDownloadProcess> getDownloadProcesses() {
		return getDownloadProcessList().values();
	}

	public Map<String, Product> getProductToDownloadList() {
		return productToDownloadList;
	}

	public Map<String, IDownloadProcess> getDownloadProcessList() {
		return downloadProcessList;
	}
}
