package int_.esa.eo.ngeo.downloadmanager.download;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;
import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductDownloadThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProductDownloadThreadPoolExecutor.class);

    public ProductDownloadThreadPoolExecutor(int corePoolSize,
            int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    private final Set<ComparableFutureTask<Long>> currentlyRunningTasks = new LinkedHashSet<ComparableFutureTask<Long>>();

    @Override
    public synchronized <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        LOGGER.debug("New task");
        return new ComparableFutureTask<>(runnable, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized void beforeExecute(Thread t, Runnable r) {
        LOGGER.debug("Perform beforeExecute() logic");
        ComparableFutureTask<Long> task = (ComparableFutureTask<Long>) r;
        if(task.getProductDownloadThread().getProduct().isPausedByDownloadManager()) {
            task.getProductDownloadThread().getProduct().setPausedByDownloadManager(false);
        }
        getCurrentlyRunningTasks().add(task);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized void afterExecute(Runnable r, Throwable t) {
        LOGGER.debug("Perform afterExecute() logic");
        ComparableFutureTask<Long> task = (ComparableFutureTask<Long>) r;
        if(task.getProductDownloadThread().getProduct().isPausedByDownloadManager()) {
            LOGGER.debug("Paused by Download Manager, adding to queue");
            this.submit(task.getProductDownloadThread());
            try {
                task.getProductDownloadThread().getDownloadProcess().resumeDownload();
            } catch (DMPluginException e) {
                LOGGER.error("Unable to resume product download from pause state", e);
            }
        }
        getCurrentlyRunningTasks().remove(task);
    }

    @SuppressWarnings("unchecked")
    public synchronized void checkPrioritiesOfCurrentlyRunningDownloads(int concurrentDownloads) {
        //Retrieve a copy of the current queue - using toArray() does not guarantee order
        BlockingQueue<Runnable> combinedQueue = new PriorityBlockingQueue<>();
        combinedQueue.addAll(getCurrentlyRunningTasks());
        //Add all currently running tasks into the copy of the queue, so we now have one big queue with all tasks
        combinedQueue.addAll(getQueue());

        LOGGER.debug(String.format("Items in the queue %s, concurrent downloads %s", combinedQueue.size(), concurrentDownloads));

        //Remove all items in the combined queue which will be the running tasks after this process.
        for(int i=1; i <= concurrentDownloads; i++) {
            ComparableFutureTask<Long> willBeRunningTask = (ComparableFutureTask<Long>) combinedQueue.poll();
            if(willBeRunningTask != null) {
                LOGGER.debug(String.format("Task which will be running: %s", willBeRunningTask.getProductDownloadThread().getProduct().getProductAccessUrl()));
            }
        }

        /* 
         * For all other tasks i.e. the ones which will be in the queue after this process, check if they are
         * running. If they are, pause them.
         */
        ComparableFutureTask<Long> queueTask;
        while((queueTask = (ComparableFutureTask<Long>) combinedQueue.poll()) != null) {
            if(queueTask.getProductDownloadThread().getProduct().getProductProgress().getStatus() == EDownloadStatus.RUNNING) {
                LOGGER.debug(String.format("Task to be paused: %s", queueTask.getProductDownloadThread().getProduct().getProductAccessUrl()));
                queueTask.getProductDownloadThread().pauseDownloadThread();
            }
        }
    }
    
    public synchronized void setConcurrentDownloads(int concurrentDownloads) {
        checkPrioritiesOfCurrentlyRunningDownloads(concurrentDownloads);
        super.setCorePoolSize(concurrentDownloads);
        super.setMaximumPoolSize(concurrentDownloads);
    }

    @SuppressWarnings("unchecked")
    public synchronized void resubmitProductDownloadThreadAfterChangeOfPriority(ProductDownloadThread productDownloadThread) {
        BlockingQueue<Runnable> queue = getQueue();
        LOGGER.debug("Removing product from queue");
        for (Runnable runnable : queue) {
            ComparableFutureTask<Long> task = (ComparableFutureTask<Long>) runnable;
            if(task.getProductDownloadThread() == productDownloadThread) {
                queue.remove(task);
                queue.add(task);
                break;
            }
        }
    }

    public Set<ComparableFutureTask<Long>> getCurrentlyRunningTasks() {
        return currentlyRunningTasks;
    }
}