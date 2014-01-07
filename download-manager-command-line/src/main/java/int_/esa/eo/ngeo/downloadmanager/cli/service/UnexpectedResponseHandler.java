package int_.esa.eo.ngeo.downloadmanager.cli.service;

import int_.esa.eo.ngeo.downloadmanager.exception.ServiceException;

public abstract class UnexpectedResponseHandler<T> {
    public abstract ServiceException createServiceExceptionForUnexpectedResponse(int httpResponseCode, T responseObject);
}
