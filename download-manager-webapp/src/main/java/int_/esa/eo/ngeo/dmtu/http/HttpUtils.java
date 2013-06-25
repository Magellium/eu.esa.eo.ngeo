package int_.esa.eo.ngeo.dmtu.http;

import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ParseException;
import int_.esa.eo.ngeo.dmtu.exception.ServiceException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.springframework.stereotype.Component;

@Component
public final class HttpUtils {
	public HttpMethod postToService(URL serviceUrl, HttpClient httpClient, ByteArrayOutputStream request, String requestMimeType, String expectedResponseMimeType) throws ServiceException {
		try {
		    PostMethod method = new PostMethod(serviceUrl.toString());
		    ByteArrayRequestEntity byteArrayRequestEntity = new ByteArrayRequestEntity(request.toByteArray());
		    
		    method.setRequestEntity(byteArrayRequestEntity);
		    method.addRequestHeader("Content-Type", requestMimeType);
		    method.addRequestHeader("Accept", expectedResponseMimeType);

		    httpClient.executeMethod(method);
			return method;
		} catch (IOException e) {
			throw new ServiceException(String.format(e.getLocalizedMessage()));
		}
	}
	
	public Date getDateFromResponseHTTPHeaders(HttpMethod method) throws ParseException {
		Header responseHeaderDate = method.getResponseHeader("Date");
		if(responseHeaderDate == null) {
			throw new NonRecoverableException("Server response does not contain HTTP header \"Date\".");
		}
		try {
			return DateUtil.parseDate(responseHeaderDate.getValue());
		} catch (DateParseException e) {
			throw new ParseException(e);
		}
	}
}