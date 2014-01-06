package int_.esa.eo.ngeo.downloadmanager.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

public class ResponseHeaderParserTest {
	ResponseHeaderParser responseHeaderParser = new ResponseHeaderParser();
	
	@Test
	public void searchForResponseDateTest() throws DateParseException, ParseException {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Date", "Thu, 07 Nov 2013 10:31:14 GMT");
		
		Date responseDate = responseHeaderParser.searchForResponseDate(headers);
		assertEquals(1383820274000L, responseDate.getTime());
	}

	@Test
	public void searchForResponseDateTestNoDateHeader() throws DateParseException {
		Header[] headers = new Header[0];
		
		try {
			responseHeaderParser.searchForResponseDate(headers);
			fail("call to searchForResponseDate with no headers should throw an exception.");
		}catch (IllegalArgumentException ex){
			assertEquals("dateValue is null", ex.getMessage());
		}
	}
	
	@Test
	public void searchForContentLengthTestNoHeader() {
		Header[] headers = new Header[0];
		
		long conentLength = responseHeaderParser.searchForContentLength(headers);
		assertEquals(-1L, conentLength);
	}

	@Test
	public void searchForContentLengthTestWithHeader() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Length", "698346923");
		
		long conentLength = responseHeaderParser.searchForContentLength(headers);
		assertEquals(698346923, conentLength);
	}

	@Test
	public void searchForContentLengthTestWithInvalidHeader() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Length", "Invalid");
		
		long conentLength = responseHeaderParser.searchForContentLength(headers);
		assertEquals(-1L, conentLength);
	}
	
	@Test
	public void searchForFileNameTestNoContentDispositionHeader() {
		Header[] headers = new Header[0];
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertNull(responseFileName);
	}

	@Test
	public void searchForFileNameTestNoFilename() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "inline");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertNull(responseFileName);
	}

	@Test
	public void searchForFileNameTestSimple() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "filename=\"example.html\"");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestNoQuotes() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; filename=example.html");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestWithQuotes() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; filename=\"my example.html\"");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("my example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestCaseInsensitive() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"example.html\"");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestFilePathFileSystem() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"C:\\users\\example.html\"");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForFileNameTestFilePathRelative() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"users/example.html\"");
		
		String responseFileName = responseHeaderParser.searchForFilename(headers);
		assertEquals("example.html", responseFileName);
	}

	@Test
	public void searchForResponseHeaderValueNullHeaderNameToSearchFor() {
		Header[] headers = new Header[0];
		
		String responseFileName = responseHeaderParser.searchForResponseHeaderValue(headers, null);
		assertNull(responseFileName);
	}

	@Test
	public void searchForResponseHeaderValueEmptyHeaderNameToSearchFor() {
		Header[] headers = new Header[0];
		
		String responseFileName = responseHeaderParser.searchForResponseHeaderValue(headers, "");
		assertNull(responseFileName);
	}

	@Test
	public void searchForResponseHeaderValueNoHeaders() {
		Header[] headers = new Header[0];
		
		String responseFileName = responseHeaderParser.searchForResponseHeaderValue(headers, "Content-Type");
		assertNull(responseFileName);
	}
	
	@Test
	public void searchForResponseHeaderValueWithHeadersNoMatch() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Disposition", "Attachment; FILENAME=\"users/example.html\"");
		
		String responseFileName = responseHeaderParser.searchForResponseHeaderValue(headers, "Content-Type");
		assertNull(responseFileName);
	}

	@Test
	public void searchForResponseHeaderValueWithHeadersAndMatch() {
		Header[] headers = new Header[1];
		headers[0] = new BasicHeader("Content-Type", "application/metalink+xml");
		
		String responseFileName = responseHeaderParser.searchForResponseHeaderValue(headers, "Content-Type");
		assertEquals("application/metalink+xml", responseFileName);
	}
}
