package int_.esa.eo.ngeo.downloadmanager.download;

import static org.junit.Assert.assertEquals;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import org.junit.Test;

public class ComparableFutureTaskTest {
	private ProductBuilder productBuilder = new ProductBuilder();

	@Test
	public void samePriorityTest() throws InterruptedException {
		ComparableFutureTask<String> task1 = buildComparableFutureTask("product 1", ProductPriority.NORMAL);
		Thread.sleep(500);
		ComparableFutureTask<String> task2 = buildComparableFutureTask("product 2", ProductPriority.NORMAL);
		assertEquals(-1, task1.compareTo(task2));
	}

	@Test
	public void higherPriorityTest() {
		ComparableFutureTask<String> task1 = buildComparableFutureTask("product 1", ProductPriority.HIGH);
		ComparableFutureTask<String> task2 = buildComparableFutureTask("product 2", ProductPriority.NORMAL);
		assertEquals(-1, task1.compareTo(task2));
	}

	@Test
	public void lowerPriorityTest() {
		ComparableFutureTask<String> task1 = buildComparableFutureTask("product 1", ProductPriority.LOW);
		ComparableFutureTask<String> task2 = buildComparableFutureTask("product 2", ProductPriority.NORMAL);
		assertEquals(1, task1.compareTo(task2));
	}
	
	
	private ComparableFutureTask<String> buildComparableFutureTask(String productUrl, ProductPriority productPriority) {
		Product product1 = productBuilder.buildProduct(productUrl);
		product1.setPriority(productPriority);

		IDownloadProcess downloadProcess1 = new TestDownloadProcess();

		ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess1, product1);
		return new ComparableFutureTask<String>(productDownloadThread, "true");
	}
}
