package int_.esa.eo.ngeo.downloadmanager.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;

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
	
	private IDownloadProcess downloadProcess;
	private static final int READ_LENGTH = 4096; //read 4k worth of bytes at a time.
	private static final Logger LOGGER = LoggerFactory.getLogger(Transferrer.class);
	
	public Transferrer(IDownloadProcess downloadProcess) {
		this.downloadProcess = downloadProcess;
	}

	public void doTransfer(FileChannel destination, InputStream inputStream, FileDetails fileDetails, Set<DownloadProgressListener> progressListeners)
																															throws IOException {
		ReadableByteChannel source = null;
		try {
			source = Channels.newChannel(inputStream);
			long bytesRead = -1;
			while (downloadProcess.getStatus() == EDownloadStatus.RUNNING) {
				if ((bytesRead = destination.transferFrom(source, fileDetails.getDownloadedSize(), READ_LENGTH)) == 0) {
					LOGGER.info(String.format("Server-side \"log jam\" affecting the download from %s?", fileDetails.getFileURL()));
				}
				else {
					for (DownloadProgressListener listener : progressListeners) {
						listener.notifySomeBytesTransferred(bytesRead);
					}

					// Have we just finished the download?
					if (fileDetails.getDownloadedSize() == fileDetails.getDownloadSize()) {
						break; // Don't try to read any more bytes from the source!
					}
				}
			}
		}
		finally {
			IOUtils.closeQuietly(source);
			if (destination != null && destination.isOpen()) { // Defending against the inability of Mockito to mock the close() method of 
															   // AbstractInterruptibleChannel (a parent of FileChannel), because
															   // that close() method is declared final 
				IOUtils.closeQuietly(destination);
			}
		}
	}

}