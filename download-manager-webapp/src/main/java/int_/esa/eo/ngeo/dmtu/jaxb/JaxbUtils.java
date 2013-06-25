package int_.esa.eo.ngeo.dmtu.jaxb;

import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;
import int_.esa.eo.ngeo.dmtu.exception.ParseException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

@Component
public final class JaxbUtils {
	@Autowired
	private JaxbSchemaRepository jaxbSchemaRepository;
	
	private final Logger LOGGER = LoggerFactory.getLogger(JaxbUtils.class);
	
	public void serializeAndInferSchema(Object inputObject, OutputStream out) throws ParseException {
		String objectName = inputObject.getClass().getName();
		LOGGER.debug(String.format("attempting to parse %s object into xml ",objectName));
		
		try {			
			String schemaPath = jaxbSchemaRepository.getSchema(objectName);
			
			serialize(inputObject, out, schemaPath);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public void serialize(Object inputObject, OutputStream out, String schemaPath) throws ParseException {
		Schema schema = null;
		if(schemaPath != null) {
			schema = loadSchemaFromClasspath(schemaPath);
		}
		serialize(inputObject, out, schema);
	}
		
	public void serialize(Object inputObject, OutputStream out, Schema schema) throws ParseException {
		try {
			// Create instance of the JAXBContext from the class-name
			JAXBContext jc = JAXBContext.newInstance(Class.forName(inputObject.getClass().getName()));
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			if (schema != null) {
				marshaller.setSchema(schema);
			}

			marshaller.setEventHandler(new ValidationEventHandler() {
				public boolean handleEvent(ValidationEvent event) {
					throw new NonRecoverableException(String.format("Unable to convert Object into XML; %s", event.getLinkedException()));
				}
			});
			
			marshaller.marshal(inputObject, out); // Do we need to cause output to be buffered?
		} catch (JAXBException | ClassNotFoundException e) {
			throw new ParseException(inputObject.getClass().getName(), e);
		}
	}

	/**
	 * @param resultType The type of the return value. Must be JAXB-compatible.
	 * @throws IOException 
	 */
	public <T> T deserializeAndInferSchema(InputStream in, Class<T> resultType) throws ParseException {
		LOGGER.debug(String.format("attempting to parse inputstream into resultType %s",resultType.getName()));
		
		T returnValue;
		try {			
			String schemaPath = jaxbSchemaRepository.getSchema(resultType.getName());
			
			returnValue = deserialize(resultType, in, schemaPath);
		} finally {
			IOUtils.closeQuietly(in);
		}
		return returnValue;
	}

	public <T> T deserialize(Class<T> clazz, InputStream inputStream, String schemaPath) throws ParseException {
		Schema schema = null;
		if(schemaPath != null) {
			schema = loadSchemaFromClasspath(schemaPath);
		}
		return deserialize(clazz, new StreamSource(inputStream), schema);
	}

	public <T> T deserialize(Class<T> clazz, final StreamSource xmlInputStreamSource) throws ParseException {
		return deserialize(clazz, xmlInputStreamSource, null);
	}

	public <T> T deserialize(Class<T> clazz, final StreamSource xmlInputStreamSource, Schema schema) throws ParseException {
		T resultObject = null;
		InputStream inputStream = xmlInputStreamSource.getInputStream();
		final byte[] inputStreamBytes;
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(inputStream, output);
			inputStreamBytes = output.toByteArray();
		} catch (IOException e1) {
			throw new ParseException(e1);
		}
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
					throw new NonRecoverableException(String.format("Unable to convert XML into Object; line %s, column %s. %nMessage: %s%nInput stream: %s",
							event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(), event.getMessage(), inputStreamAsString), event.getLinkedException());
				}
			});
			resultObject = unmarshaller.unmarshal(xmlInputStreamSource2, clazz).getValue();
		} catch (JAXBException | ClassNotFoundException e) {
			throw new ParseException(clazz.getName(), e);
		}
		return resultObject;
	}

	public static Schema loadSchemaFromClasspath(String schemaPath) {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
		URL schemaUrl = JaxbUtils.class.getClassLoader().getResource(schemaPath);
		try {
			return schemaFactory.newSchema(schemaUrl);
		} catch (SAXException e) {
			throw new NonRecoverableException(e);
		}
	}
}