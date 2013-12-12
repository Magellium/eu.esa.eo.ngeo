package int_.esa.eo.ngeo.downloadmanager.plugin.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import int_.esa.eo.ngeo.downloadmanager.plugin.FilesDownloadListener;
import int_.esa.eo.ngeo.downloadmanager.plugin.utils.Transferrer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

import org.junit.Before;
import org.junit.Test;

public class TransferrerTest {
	ReadableByteChannel source;
	SeekableByteChannel destination;
	String fileMetadataUuid;
	FilesDownloadListener filesDownloadListener;
	
	@Before
	public void setup() {
		source = mock(ReadableByteChannel.class);
		destination = mock(SeekableByteChannel.class);
		fileMetadataUuid = "testMetadataUuid";
		filesDownloadListener = mock(FilesDownloadListener.class);
	}
	
	@Test
	public void test() throws IOException {
		Transferrer transferrer = new Transferrer(4096);
		
		int length1 = 6;
		int length2 = 0;
		int length3 = 5;
		
		when(source.read((ByteBuffer) any())).thenReturn(length1, length2, length3, -1);
		
		when(destination.size()).thenReturn(0L);

		when(destination.write((ByteBuffer) any())).thenReturn(length1, length2, length3);

		doNothing().when(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length1);
		doNothing().when(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length2);		
		doNothing().when(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length3);
		
		assertTrue(transferrer.doTransfer(source, destination, fileMetadataUuid, filesDownloadListener));
		
		verify(destination).position(0);
		verify(destination, times(3)).write((ByteBuffer) any());

		verify(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length1);
		verify(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length2);
		verify(filesDownloadListener).notifyOfBytesTransferred(fileMetadataUuid, length3);
	}

}
