package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import int_.esa.eo.ngeo.downloadmanager.plugin.EDownloadStatus;
import int_.esa.eo.ngeo.downloadmanager.plugin.FilesProgressListener;
import int_.esa.eo.ngeo.downloadmanager.plugin.ProductDownloadProgressMonitor;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Transferrer is capable of using nio (new I/O) to copy the content of an InputStream, block by block, to a destination FileChannel.
 * After each sub-transfer is complete, all listeners specified will be notified of the number of bytes that were read; this
 * allows clients to keep track of the progress of the transfer, e.g. by maintaining a progress bar within a UI.
 * <p/> 
 * The Transferrer caters for cases in which, despite the end of the InputStream not yet having been reached, an attempt
 * to transfer a block of bytes results in no bytes being transferred because the source of the InputStream's content has chosen to
 * return immediately without providing further bytes (typically motivated by wanting to avoid a significant delay that would occur
 * by blocking until further bytes are available.) 
 */
public class Transferrer {
	
	private ProductDownloadProgressMonitor productDownloadProgressMonitor;
	private int readLength;
	private long totalBytesDownloaded;
	private static final Logger LOGGER = LoggerFactory.getLogger(Transferrer.class);
	
	public Transferrer(ProductDownloadProgressMonitor productDownloadProgressMonitor, int readLength) {
		this.productDownloadProgressMonitor = productDownloadProgressMonitor;
		this.readLength = readLength;
	}

	public void doTransfer(FileChannel destination, InputStream inputStream, FileDownloadMetadata fileMetadata, long bytesAlreadyDownloaded, FilesProgressListener filesProgressListener) throws IOException {
		this.totalBytesDownloaded = bytesAlreadyDownloaded;
		ReadableByteChannel source = null;
		try {
			source = Channels.newChannel(inputStream);
			long bytesRead = -1;
			while (productDownloadProgressMonitor.getStatus() == EDownloadStatus.RUNNING) {
				if ((bytesRead = destination.transferFrom(source, totalBytesDownloaded, readLength)) == 0) {
					LOGGER.info(String.format("Server-side \"log jam\" affecting the download from %s?", fileMetadata.getFileURL()));
				} else {
					this.totalBytesDownloaded += bytesRead;
					
					filesProgressListener.notifySomeBytesTransferred(fileMetadata.getUuid(), bytesRead);

					// Have we just finished the download?
					if (totalBytesDownloaded == fileMetadata.getDownloadSize()) {
						// Don't try to read any more bytes from the source!
						break;
					}
				}
			}
		} finally {
			IOUtils.closeQuietly(source);
			// Defending against the inability of Mockito to mock the close() method of 
			// AbstractInterruptibleChannel (a parent of FileChannel), because
			// that close() method is declared final 
			if (destination != null && destination.isOpen()) { 
				IOUtils.closeQuietly(destination);
			}
		}
	}

}