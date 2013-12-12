package int_.esa.eo.ngeo.downloadmanager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.pse.umsso.client.util.UmssoHttpResponse;

public final class ResponseHeaderParser {
	private static final int DISPOSITION_SUBSTRING_START_OFFSET = 10;
	private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHeaderParser.class);

	//Some HTTP Header constants are not included in HttpHeaders. The following constants fill in the gaps which are necessary
	public final static String HTTP_RESPONSE_HEADER_CONTENT_DISPOSITION = "Content-Disposition";

	public Date searchForResponseDate(UmssoHttpResponse response) throws DateParseException {
		String responseHeaderDate = searchForResponseHeaderValue(response, HttpHeaders.DATE);
		return DateUtils.parseDate(responseHeaderDate);
	}
	
	public long searchForContentLength(UmssoHttpResponse response) {
		String responseHeaderContentLength = searchForResponseHeaderValue(response, HttpHeaders.CONTENT_LENGTH);

		if (responseHeaderContentLength != null) {
			try {
				return Long.parseLong(responseHeaderContentLength);
			}catch(NumberFormatException ex) {
				LOGGER.warn(String.format("Unable to parse content length %s.", responseHeaderContentLength));
				return -1;
			}
		}
		return -1;
	}

	public String searchForFilename(UmssoHttpResponse response) {
		String responseHeaderContentDisposition = searchForResponseHeaderValue(response, HTTP_RESPONSE_HEADER_CONTENT_DISPOSITION);

		if(!StringUtils.isEmpty(responseHeaderContentDisposition)) {
			Pattern filenameFromHeaderPattern = Pattern.compile(".*filename=\\\"?([^\\\"]*)\\\"?", Pattern.CASE_INSENSITIVE);
			Matcher filenameFromHeaderMatcher = filenameFromHeaderPattern.matcher(responseHeaderContentDisposition);
					
			if (filenameFromHeaderMatcher.find()) {
				// Extract file name from header field
				String filenameFromHeader = filenameFromHeaderMatcher.group(1);
				Path fileNamePath = Paths.get(filenameFromHeader);
				return fileNamePath.getFileName().toString();
			}
		}

		return null;
	}
	
	public String searchForResponseHeaderValue(UmssoHttpResponse response, String headerName) {
		Header[] headers = response.getHeaders();
		for (Header header : headers) {
			if(header.getName().equals(headerName)) {
				return header.getValue();
			}
		}
		return null;
	}
}