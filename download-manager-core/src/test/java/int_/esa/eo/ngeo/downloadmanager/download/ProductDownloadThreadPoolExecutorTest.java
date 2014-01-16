package int_.esa.eo.ngeo.downloadmanager.download;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.builder.ProductBuilder;
import int_.esa.eo.ngeo.downloadmanager.model.Product;
import int_.esa.eo.ngeo.downloadmanager.model.ProductPriority;
import int_.esa.eo.ngeo.downloadmanager.model.ProductProgress;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.IDownloadProcess;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ProductDownloadThreadPoolExecutorTest<V, T> {
	private ProductDownloadThreadPoolExecutor productDownloadThreadPoolExecutor;
	private static final int CONCURRENT_PRODUCT_DOWNLOAD_THREADS = 1;
	
	@Before
	public void setup() {
		productDownloadThreadPoolExecutor = spy(new ProductDownloadThreadPoolExecutor(
				CONCURRENT_PRODUCT_DOWNLOAD_THREADS,
				CONCURRENT_PRODUCT_DOWNLOAD_THREADS, 0L, TimeUnit.MILLISECONDS,
				new PriorityBlockingQueue<Runnable>(200)));
	}

	private void setConcurrentDownloads(int concurrentDownloads) {
		productDownloadThreadPoolExecutor.setCorePoolSize(concurrentDownloads);
		productDownloadThreadPoolExecutor.setMaximumPoolSize(concurrentDownloads);
	}

	@Test
	public void checkPrioritiesOfOneCurrentlyRunningDownloadOneHigherPriorityTest() throws InterruptedException {
		setConcurrentDownloads(1);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.HIGH);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}

	@Test
	public void checkPrioritiesOfOneCurrentlyRunningDownloadOneEqualPriorityTest() throws InterruptedException {
		setConcurrentDownloads(1);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.NORMAL);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}

	@Test
	public void checkPrioritiesOfOneCurrentlyRunningDownloadOneLowerPriorityTest() throws InterruptedException {
		setConcurrentDownloads(1);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);
		
		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}

	@Test
	public void checkPrioritiesOfTwoCurrentlyRunningDownloadsWithTwoHigherQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.HIGH);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.HIGH);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(1)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoCurrentlyRunningDownloadsWithOneHigherOneEqualQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.HIGH);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.NORMAL);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoCurrentlyRunningDownloadsWithTwoEqualQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.NORMAL);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.NORMAL);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoCurrentlyRunningDownloadsWithOneEqualOneLowerQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.NORMAL);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}

	@Test
	public void checkPrioritiesOfTwoCurrentlyRunningDownloadsWithTwoLowerQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.LOW);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoDifferentPriorityCurrentlyRunningDownloadsWithTwoHigherQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.LOW, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.HIGH);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.HIGH);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(1)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoDifferentPriorityCurrentlyRunningDownloadsWithOneHigherOneEqualQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.LOW, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.HIGH);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.NORMAL);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoDifferentPriorityCurrentlyRunningDownloadsWithTwoEqualQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.LOW, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.NORMAL);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.NORMAL);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}
	
	@Test
	public void checkPrioritiesOfTwoDifferentPriorityCurrentlyRunningDownloadsWithOneEqualOneLowerQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.LOW, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.NORMAL);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(1)).pauseDownloadThread();
	}

	@Test
	public void checkPrioritiesOfTwoDifferentPriorityCurrentlyRunningDownloadsWithTwoLowerQueuedTest() throws InterruptedException {
		setConcurrentDownloads(2);

		Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();
		ComparableFutureTask<Long> currentlyRunningTask1 = buildMockComparableFutureTask("currentlyRunningTask1", ProductPriority.NORMAL, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask1);
		ComparableFutureTask<Long> currentlyRunningTask2 = buildMockComparableFutureTask("currentlyRunningTask2", ProductPriority.LOW, EDownloadStatus.RUNNING);
		currentlyRunningTasks.add(currentlyRunningTask2);

		when(productDownloadThreadPoolExecutor.getCurrentlyRunningTasks()).thenReturn(currentlyRunningTasks);

		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.LOW);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockQueue);

		productDownloadThreadPoolExecutor.checkPrioritiesOfCurrentlyRunningDownloads();
		verify(currentlyRunningTask1.getProductDownloadThread(), times(0)).pauseDownloadThread();
		verify(currentlyRunningTask2.getProductDownloadThread(), times(0)).pauseDownloadThread();
	}

	@Test
	public void resubmitProductDownloadThreadAfterChangeOfPriorityTest() {
		BlockingQueue<Runnable> mockedQueue = new PriorityBlockingQueue<>();
		ComparableFutureTask<Long> task1 = buildComparableFutureTask("product 1", ProductPriority.NORMAL);
		mockedQueue.add(task1);
		ComparableFutureTask<Long> task2 = buildComparableFutureTask("product 2", ProductPriority.LOW);
		mockedQueue.add(task2);
		ComparableFutureTask<Long> task3 = buildComparableFutureTask("product 3", ProductPriority.VERY_HIGH);
		mockedQueue.add(task3);
		
		when(productDownloadThreadPoolExecutor.getQueue()).thenReturn(mockedQueue);
		task2.getProductDownloadThread().getProduct().setPriority(ProductPriority.HIGH);
		
		productDownloadThreadPoolExecutor.resubmitProductDownloadThreadAfterChangeOfPriority(task2.getProductDownloadThread());

		assertEquals(mockedQueue.poll(), task3);
		assertEquals(mockedQueue.poll(), task2);
		assertEquals(mockedQueue.poll(), task1);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void queueTest() throws InterruptedException {
		Map<String, ProductPriority> mockQueueEntryMap = new LinkedHashMap<>();
		mockQueueEntryMap.put("queuedTask1", ProductPriority.VERY_LOW);
		mockQueueEntryMap.put("queuedTask2", ProductPriority.LOW);
		mockQueueEntryMap.put("queuedTask3", ProductPriority.VERY_HIGH);
		mockQueueEntryMap.put("queuedTask4", ProductPriority.NORMAL);
		mockQueueEntryMap.put("queuedTask5", ProductPriority.LOW);
		BlockingQueue<Runnable> mockQueue = createMockQueue(mockQueueEntryMap);

		BlockingQueue<Runnable> copyOfMockQueue = new PriorityBlockingQueue<>(mockQueue);
		
		assertEquals("queuedTask3", ((ComparableFutureTask<Long>) copyOfMockQueue.poll()).getProductDownloadThread().getProduct().getProductAccessUrl());
		assertEquals("queuedTask4", ((ComparableFutureTask<Long>) copyOfMockQueue.poll()).getProductDownloadThread().getProduct().getProductAccessUrl());
		assertEquals("queuedTask2", ((ComparableFutureTask<Long>) copyOfMockQueue.poll()).getProductDownloadThread().getProduct().getProductAccessUrl());
		assertEquals("queuedTask5", ((ComparableFutureTask<Long>) copyOfMockQueue.poll()).getProductDownloadThread().getProduct().getProductAccessUrl());
		assertEquals("queuedTask1", ((ComparableFutureTask<Long>) copyOfMockQueue.poll()).getProductDownloadThread().getProduct().getProductAccessUrl());
		
		assertEquals(5, mockQueue.size());
	}
	
	private ComparableFutureTask<Long> buildComparableFutureTask(String productUrl, ProductPriority productPriority) {
		ProductBuilder productBuilder = new ProductBuilder();
		Product product1 = productBuilder.buildProduct(productUrl);
		product1.setPriority(productPriority);

		IDownloadProcess downloadProcess1 = new TestDownloadProcess();

		ProductDownloadThread productDownloadThread = new ProductDownloadThread(downloadProcess1, product1);
		return new ComparableFutureTask<Long>(productDownloadThread, 1L);
	}

	private ComparableFutureTask<Long> buildMockComparableFutureTask(String productUrl, ProductPriority productPriority, EDownloadStatus downloadStatus) {
		ProductDownloadThread mockProductDownloadThread = mock(ProductDownloadThread.class);
		Product mockProduct = mock(Product.class);
		when(mockProductDownloadThread.getProduct()).thenReturn(mockProduct);

		when(mockProduct.getProductAccessUrl()).thenReturn(productUrl);
		when(mockProduct.getPriority()).thenReturn(productPriority);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		when(mockProduct.getCreationTimestamp()).thenReturn(new Timestamp(new Date().getTime()));

		ProductProgress productProgress = mock(ProductProgress.class);
		when(mockProduct.getProductProgress()).thenReturn(productProgress);
		
		when(productProgress.getStatus()).thenReturn(downloadStatus);

		ComparableFutureTask<Long> mockTask = new ComparableFutureTask<Long>(mockProductDownloadThread, 1L);

		return mockTask;
	}

	private BlockingQueue<Runnable> createMockQueue(Map<String, ProductPriority> mockQueueMap) {
		BlockingQueue<Runnable> mockedQueue = new PriorityBlockingQueue<>();
		for (Entry<String, ProductPriority> mockQueueEntry : mockQueueMap.entrySet()) {
			ComparableFutureTask<Long> queuedTask = buildMockComparableFutureTask(mockQueueEntry.getKey(), mockQueueEntry.getValue(), EDownloadStatus.NOT_STARTED);
			mockedQueue.add(queuedTask);
		}
		return mockedQueue;
	}
}
