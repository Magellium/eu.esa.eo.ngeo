package int_.esa.eo.ngeo.dmtu.utils;

import int_.esa.eo.ngeo.dmtu.exception.ParseException;

import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.springframework.stereotype.Component;

@Component
public final class HttpHeaderParser {
	public Date getDateFromResponseHTTPHeaders(HttpMethod method) throws ParseException {
		Header responseHeaderDate = method.getResponseHeader("Date");
		if(responseHeaderDate == null) {
			throw new ParseException("Server response does not contain HTTP header \"Date\".");
		}
		try {
			return DateUtil.parseDate(responseHeaderDate.getValue());
		} catch (DateParseException e) {
			throw new ParseException(e);
		}
	}
}