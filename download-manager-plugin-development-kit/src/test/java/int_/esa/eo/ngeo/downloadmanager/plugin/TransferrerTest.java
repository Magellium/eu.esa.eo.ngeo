package int_.esa.eo.ngeo.downloadmanager.plugin;

import int_.esa.eo.ngeo.downloadmanager.plugin.FilesProgressListener;
import int_.esa.eo.ngeo.downloadmanager.plugin.model.FileDownloadMetadata;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.Transferrer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class TransferrerTest {

	private static final long _4096L = 4096l;
	private static final String HELLO = "Hello";
	private static final String WORLD = " world";
	
	@Test
	public void test() throws IOException {
		ProductDownloadProgressMonitor productDownloadProgressMonitor = mock(ProductDownloadProgressMonitor.class);
		when(productDownloadProgressMonitor.getStatus()).thenReturn(EDownloadStatus.RUNNING);
		Transferrer transferrer = new Transferrer(productDownloadProgressMonitor, 4096);
		
		FileChannel mockDestinationChannel = mock(FileChannel.class);
		final long length1 = (long)HELLO.length();
		final long length2 = (long)WORLD.length();
		
		when(mockDestinationChannel.transferFrom((ReadableByteChannel) any(), eq(0l), eq(_4096L))).thenReturn(length1);
		when(mockDestinationChannel.transferFrom((ReadableByteChannel) any(), eq(length1), eq(_4096L))).thenReturn((long)0); // Simulate zero bytes being returned, i.e. the scenario where 
																													  // we're in the middle of transferring the bytes from the source 
		  																											  // but the FileChannel can't currently supply more bytes 
		when(mockDestinationChannel.transferFrom((ReadableByteChannel) any(), eq(length1), eq(_4096L))).thenReturn(length2); // Simulate the scenario where bytes representing the string " world" are transferred.
		
		InputStream inputStream = new ByteArrayInputStream((HELLO + WORLD).getBytes("UTF-8"));
		FileDownloadMetadata fileDetails = new FileDownloadMetadata(new URL("http://dummyurl"), "dummyFileName", length1 + length2, new File("dummyDownloadPath").toPath());
		FilesProgressListener progressListener = mock(FilesProgressListener.class);
		String fileDownloadMetadataUuid = fileDetails.getUuid();
		doNothing().when(progressListener).notifySomeBytesTransferred(fileDownloadMetadataUuid, length1);
		doNothing().when(progressListener).notifySomeBytesTransferred(fileDownloadMetadataUuid, 0);		
		doNothing().when(progressListener).notifySomeBytesTransferred(fileDownloadMetadataUuid, length2);
		
		transferrer.doTransfer(mockDestinationChannel, inputStream, fileDetails, 0, progressListener);
		
		verify(mockDestinationChannel).transferFrom((ReadableByteChannel) any(), eq(0l), eq(_4096L));
		verify(mockDestinationChannel).transferFrom((ReadableByteChannel) any(), eq(length1), eq(_4096L)); 
		verify(mockDestinationChannel).transferFrom((ReadableByteChannel) any(), eq(length1), eq(_4096L));
		verify(progressListener).notifySomeBytesTransferred(fileDownloadMetadataUuid, length1);
		verify(progressListener).notifySomeBytesTransferred(fileDownloadMetadataUuid, length2);
	}

}
