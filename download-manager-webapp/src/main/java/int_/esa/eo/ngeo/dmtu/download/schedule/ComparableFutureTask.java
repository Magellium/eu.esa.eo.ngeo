package int_.esa.eo.ngeo.dmtu.download.schedule;

import java.sql.Timestamp;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ComparableFutureTask<V> extends FutureTask<V> implements
		Comparable<ComparableFutureTask<V>> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComparableFutureTask.class);

	private ProductDownloadThread productDownloadThread;

	public ComparableFutureTask(Runnable runnable, V result) {
		super(runnable, result);
		this.productDownloadThread = (ProductDownloadThread) runnable;
	}

	@Override
	public int compareTo(ComparableFutureTask<V> o) {
		int thisPriority = productDownloadThread.getProduct().getPriority().ordinal();
		int otherPriority = o.productDownloadThread.getProduct().getPriority().ordinal();

		int priorityCompare = Integer.compare(thisPriority, otherPriority);
		if(priorityCompare == 0) {
			Timestamp thisCreationTimestamp = productDownloadThread.getProduct().getCreationTimestamp();
			Timestamp otherCreationTimestamp = o.getProductDownloadThread().getProduct().getCreationTimestamp();
			return thisCreationTimestamp.before(otherCreationTimestamp) ? -1 : thisCreationTimestamp.after(otherCreationTimestamp) ? 1 : 0;
		}else{
			return priorityCompare;
		}
	}
	
	public ProductDownloadThread getProductDownloadThread() {
		return productDownloadThread;
	}
}