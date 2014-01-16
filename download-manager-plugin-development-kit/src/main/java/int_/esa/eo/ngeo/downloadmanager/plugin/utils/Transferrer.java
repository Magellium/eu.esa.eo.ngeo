package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import int_.esa.eo.ngeo.downloadmanager.plugin.FilesDownloadListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

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
    private boolean aborted;
    private static final Logger LOGGER = LoggerFactory.getLogger(Transferrer.class);

    public Transferrer(int readLength) {
        this.readLength = readLength;
    }

    /**
     * @param destination
     * @param inputStream
     * @param fileMetadata
     * @param bytesAlreadyDownloaded
     * @param filesProgressListener
     * @return true if the contents of the inputStream has been transferred to the destination completely, false if the transfer has been interrupted (by a cancel / pause command)
     * @throws IOException Occurs when reading the source stream.
     */
    public boolean doTransfer(ReadableByteChannel source, SeekableByteChannel destination, String fileMetadataUuid, FilesDownloadListener filesProgressListener) throws IOException {
        long startTime, elapsedTime;
        try {
            //start download from the end of the file - used primarily for resume scenarios
            destination.position(destination.size());

            ByteBuffer bytebuf = ByteBuffer.allocateDirect(readLength);

            startTime = System.nanoTime();
            int bytesRead;
            while ((bytesRead = source.read(bytebuf)) >= 0 && !aborted) { 
                // flip the buffer which set the limit to current position, and position to 0.
                bytebuf.flip();
                int bytesWritten = destination.write(bytebuf);
                // Clear buffer for the next read
                bytebuf.clear();

                filesProgressListener.notifyOfBytesTransferred(fileMetadataUuid, bytesWritten);
            }

            //check if we have reached the end of file, of whether we have dropped out as a result of an abort command
            if(bytesRead == -1) {
                elapsedTime = System.nanoTime() - startTime;
                LOGGER.debug("Elapsed Time is " + (elapsedTime / 1000000.0) + " msec");
                return true;
            }else{
                return false;
            }
        } finally {
            // Since the source and destination are passed in as arguments, they should be closed in the calling class
        }
        //transfer has been aborted i.e. is not complete.
    }

    public void abortTransfer() {
        aborted = true;
    }
}