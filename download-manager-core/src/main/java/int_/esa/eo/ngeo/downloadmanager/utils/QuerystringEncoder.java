package int_.esa.eo.ngeo.downloadmanager.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuerystringEncoder {
    public String encodeQueryString(String inputUrl) throws UnsupportedEncodingException {
        Pattern queryStringPattern = Pattern.compile("(.*\\?)(.*)", Pattern.CASE_INSENSITIVE);
        Matcher queryStringMatcher = queryStringPattern.matcher(inputUrl);

        String encodedUrl = inputUrl;
        if (queryStringMatcher.find()) {
            String queryString = queryStringMatcher.group(2);
            
            encodedUrl = queryStringMatcher.replaceAll("$1" + URLEncoder.encode(queryString, StandardCharsets.UTF_8.displayName()));
        }
        
        return encodedUrl;
    }
}
