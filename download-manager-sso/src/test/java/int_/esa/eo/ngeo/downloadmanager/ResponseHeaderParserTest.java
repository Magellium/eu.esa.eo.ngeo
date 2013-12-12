package int_.esa.eo.ngeo.downloadmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public class ResponseHeaderParserTest {
	ResponseHeaderParser responseHeaderParser = new ResponseHeaderParser();
	UmssoHttpResponse umssoHttpReponse;
	
	@Before
	public void setup() {
		umssoHttpReponse = mock(UmssoHttpResponse.class);
	}
	
	@Test
	public void searchForResponseDateTest() throws DateParseException, ParseException {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Date", "Thu, 07 Nov 2013 10:31:14 GMT");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		Date responseDate = responseHeaderParser.searchForResponseDate(umssoHttpReponse);
		assertEquals(1383820274000L, responseDate.getTime());
	}

	@Test
	public void searchForResponseDateTestNoDateHeader() throws DateParseException {
		Header[] headers = new Header[0];
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		try {
			responseHeaderParser.searchForResponseDate(umssoHttpReponse);
			fail("call to searchForResponseDate with no headers should throw an exception.");
		}catch (IllegalArgumentException ex){
			assertEquals("dateValue is null", ex.getMessage());
		}
	}
	
	@Test
	public void searchForFileNameTestNoContentDispositionHeader() {
		Header[] headers = new Header[0];
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertNull(responseFileName);
	}

	@Test
	public void searchForFileNameTestSimple() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "filename=\"example.html\"");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestNoQuotes() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; filename=example.html");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestWithQuotes() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; filename=\"my example.html\"");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("my example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestCaseInsensitive() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"example.html\"");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestFilePathFileSystem() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"C:\\users\\example.html\"");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestFilePathRelative() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"users/example.html\"");
		
		when(umssoHttpReponse.getHeaders()).thenReturn(headers);
		
		String responseFileName = responseHeaderParser.searchForFilename(umssoHttpReponse);
		assertEquals("example.html", responseFileName);
	}
}
