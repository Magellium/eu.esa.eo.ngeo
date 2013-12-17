package int_.esa.eo.ngeo.dmtu.download.schedule;

import int_.esa.eo.ngeo.dmtu.exception.ProductNotFoundException;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DownloadScheduler {
	private ProductDownloadThreadPoolExecutor mThreadPoolExecutor;
	private List<ProductDownloadThread> activeProductDownloadThreads;
	
	public DownloadScheduler(int concurrentProductDownloadThreads) {
		mThreadPoolExecutor = new ProductDownloadThreadPoolExecutor(
				concurrentProductDownloadThreads,
				concurrentProductDownloadThreads, 0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(200));
		activeProductDownloadThreads = new ArrayList<>();
	}
	
	public void scheduleProductDownload(IDownloadProcess downloadProcess, Product product) {
		ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess, product);

		mThreadPoolExecutor.submit(productDownloadThread);
	    activeProductDownloadThreads.add(productDownloadThread);
		mThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
	}
	
	public void shutdown() {
		try {
			mThreadPoolExecutor.shutdown();
			mThreadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
	}
	
	public Product getProduct(String productUuid) throws ProductNotFoundException {
		for (ProductDownloadThread productDownloadThread : activeProductDownloadThreads) {
			Product product = productDownloadThread.getProduct();
			if(product.getUuid().equals(productUuid)) {
				return product;
			}
		}
		
		throw new ProductNotFoundException(String.format("Unable to find product with UUID %s. This product may have already been completed.", productUuid));
	}

	public void changeProductPriority(Product product) {
		ProductDownloadThread productDownloadThread = getProductDownloadThread(product);		
		if(productDownloadThread != null) {
			mThreadPoolExecutor.resubmitProductDownloadThreadAfterChangeOfPriority(productDownloadThread);
			mThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		}
	}
	
	public ProductDownloadThread getProductDownloadThread(Product product) {
		for (ProductDownloadThread productDownloadThread : activeProductDownloadThreads) {
			if(productDownloadThread.getProduct() == product) {
				return productDownloadThread;
			}
		}
		return null;
	}
}
