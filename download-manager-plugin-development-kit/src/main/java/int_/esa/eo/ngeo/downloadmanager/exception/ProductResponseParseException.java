package int_.esa.eo.ngeo.downloadmanager.exception;

import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

public class ProductResponseParseException extends DMPluginException {
	private static final long serialVersionUID = 7917867989020486916L;

	public ProductResponseParseException(String message, Throwable cause) {
		super(message,cause);
	}

	public ProductResponseParseException(Throwable cause) {
		super(cause);
	}

	public ProductResponseParseException(String message) {
		super(message);
	}

}
