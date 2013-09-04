package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import int_.esa.eo.ngeo.downloadmanager.plugin.FilesDownloadProgressListener;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

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
 * Since the source and destination are passed in as arguments, they should be closed in the calling class
 */
public class Transferrer {
	
	private int readLength;
	private long totalBytesDownloaded;
	private boolean aborted;
	private static final Logger LOGGER = LoggerFactory.getLogger(Transferrer.class);
	
	public Transferrer(int readLength) {
		this.readLength = readLength;
	}

	public boolean doTransfer(FileChannel destination, InputStream inputStream, FileDownloadMetadata fileMetadata, long bytesAlreadyDownloaded, FilesDownloadProgressListener filesProgressListener) throws IOException {
		this.totalBytesDownloaded = bytesAlreadyDownloaded;
		ReadableByteChannel source = null;
		try {
			source = Channels.newChannel(inputStream);
			long bytesRead = -1;
			while (!aborted) {
				if ((bytesRead = destination.transferFrom(source, totalBytesDownloaded, readLength)) == 0) {
					LOGGER.debug(String.format("Server-side \"log jam\" affecting the download from %s?", fileMetadata.getFileURL()));
				} else {
					this.totalBytesDownloaded += bytesRead;
					
					filesProgressListener.notifySomeBytesTransferred(fileMetadata.getUuid(), bytesRead);

					// Have we just finished the download?
					if (totalBytesDownloaded == fileMetadata.getDownloadSize()) {
						// Don't try to read any more bytes from the source!
						return true;
					}
				}
			}
		} finally {
			// Since the source and destination are passed in as arguments, they should be closed in the calling class
		}
		//transfer has been aborted i.e. is not complete.
		return false;
	}

	public void abortTransfer() {
		aborted = true;
	}
}