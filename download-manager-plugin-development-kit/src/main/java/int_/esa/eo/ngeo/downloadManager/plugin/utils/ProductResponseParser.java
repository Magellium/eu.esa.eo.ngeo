package int_.esa.eo.ngeo.downloadManager.plugin.utils;

import int_.esa.eo.ngeo.downloadManager.exception.ProductResponseParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.DMPluginException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProductResponseParser {
	private final Logger LOGGER = LoggerFactory.getLogger(ProductResponseParser.class);
	
	public <T> T parse(InputStream in, ProductResponseType responseType) throws IOException, DMPluginException {
		LOGGER.debug(String.format("attempting to parse inputstream for response type %s",responseType));
		
		T returnValue;
		try {			
			String schemaPath = responseType.getSchemaPath();
			Class<T> classType = responseType.getClassType();
			returnValue = deserialize(classType, in, schemaPath);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return returnValue;
	}
	
	public <T> T deserialize(Class<T> clazz, InputStream inputStream, String schemaPath) throws IOException, DMPluginException {
		Schema schema = null;
		return deserialize(clazz, new StreamSource(inputStream), schema);
	}

	public <T> T deserialize(Class<T> clazz, final StreamSource xmlInputStreamSource, Schema schema) throws IOException, DMPluginException {
		T resultObject = null;
		InputStream inputStream = xmlInputStreamSource.getInputStream();
		final byte[] inputStreamBytes;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, output);
		inputStreamBytes = output.toByteArray();

		StreamSource xmlInputStreamSource2 = new StreamSource(new ByteArrayInputStream(inputStreamBytes));
		try {
			// Create instance of the JAXBContext from the class-name
			JAXBContext jc = JAXBContext.newInstance(Class.forName(clazz.getName()));
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			if (schema != null) {
				unmarshaller.setSchema(schema);
			}
			unmarshaller.setEventHandler(new ValidationEventHandler() {
				public boolean handleEvent(ValidationEvent event) {
					String inputStreamAsString = new String(inputStreamBytes);
					LOGGER.error(String.format("Unable to convert XML into Object; line %s, column %s. Error is %s%nInput stream was %s",
							event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(), event.getMessage(), inputStreamAsString), event.getLinkedException());
					return false;
				}
			});
			resultObject = unmarshaller.unmarshal(xmlInputStreamSource2, clazz).getValue();
		} catch (JAXBException | ClassNotFoundException ex) {
			throw new ProductResponseParseException(String.format("Unable to parse XML for class %s", clazz.getName()), ex);
		}
		return resultObject;
	}
}