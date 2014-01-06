package int_.esa.eo.ngeo.downloadmanager.http;

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

public final class ResponseHeaderParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHeaderParser.class);

    //Some HTTP Header constants are not included in HttpHeaders. The following constants fill in the gaps which are necessary
    public static final String HTTP_RESPONSE_HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    public Date searchForResponseDate(Header[] headers) throws DateParseException {
        String responseHeaderDate = searchForResponseHeaderValue(headers, HttpHeaders.DATE);
        return DateUtils.parseDate(responseHeaderDate);
    }

    public long searchForContentLength(Header[] headers) {
        String responseHeaderContentLength = searchForResponseHeaderValue(headers, HttpHeaders.CONTENT_LENGTH);

        if (responseHeaderContentLength != null) {
            try {
                return Long.parseLong(responseHeaderContentLength);
            }catch(NumberFormatException ex) {
                LOGGER.error(String.format("Unable to parse content length %s.", responseHeaderContentLength));
                return -1;
            }
        }
        return -1;
    }

    public String searchForFilename(Header[] headers) {
        String responseHeaderContentDisposition = searchForResponseHeaderValue(headers, HTTP_RESPONSE_HEADER_CONTENT_DISPOSITION);

        if(StringUtils.isNotEmpty(responseHeaderContentDisposition)) {
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

    public String searchForResponseHeaderValue(Header[] headers, String headerName) {
        if(StringUtils.isNotEmpty(headerName)) {
            for (Header header : headers) {
                if(headerName.equals(header.getName())) {
                    return header.getValue();
                }
            }
        }
        return null;
    }
}