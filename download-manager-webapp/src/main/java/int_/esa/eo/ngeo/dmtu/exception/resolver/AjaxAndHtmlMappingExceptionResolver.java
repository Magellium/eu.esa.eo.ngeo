package int_.esa.eo.ngeo.dmtu.exception.resolver;

import int_.esa.eo.ngeo.dmtu.builder.CommandResponse;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lkn
 * Handles JSON exceptions in a more elegant way than using @ExceptionHandler in each controller
 */
public class AjaxAndHtmlMappingExceptionResolver extends SimpleMappingExceptionResolver {
	public static final String ACCEPT_TYPE_JSON = "application/json";
	public static final String ACCEPT_TYPE_HTML = "text/html";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AjaxAndHtmlMappingExceptionResolver.class);
	
	@Override
	protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		LOGGER.error(ex.getMessage(), ex);
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		String acceptHeader = request.getHeader("Accept");
		if (acceptHeader.contains(ACCEPT_TYPE_JSON)) {
			Map<String, Object> modelMap = new ModelMap();
			CommandResponse commandResponse = new CommandResponse(false, ex.getLocalizedMessage());
			modelMap.put("response", commandResponse);
			return new ModelAndView(new MappingJacksonJsonView(), modelMap);
		} else if (acceptHeader.contains(ACCEPT_TYPE_HTML)) {
			return super.doResolveException(request, response, handler, ex);
		} else {
			return null;
		}
	}
}
