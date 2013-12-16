package int_.esa.eo.ngeo.dmtu.model;

import static org.junit.Assert.fail;
import int_.esa.eo.ngeo.downloadmanager.model.Product;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * Designed to ensure that mapping between data model and hibernate mapping files (hbm.xml) are complete
 */
public class HibernateMappingTest {

	@Test
	public void testDARHibernateMapping() throws ParserConfigurationException, SAXException, IOException {
		List<String> mappingNames = getMappingNames("/mapping/hibernate/DataAccessRequest.hbm.xml");
				
		List<String> fieldsWithoutCorrespondingMapping = new ArrayList<>();
		
		List<Field> declaredFields = getFieldsWithJsonPropertyAnnotationOnly(Product.class.getDeclaredFields());
		for (Field field : declaredFields) {
			String fieldName = field.getName();
			if(mappingNames.contains(fieldName)) {
				mappingNames.remove(fieldName);
			}else{
				fieldsWithoutCorrespondingMapping.add(fieldName);
			}
		}
		
		String errorMessage = buildErrorMessage(mappingNames, fieldsWithoutCorrespondingMapping);
		if(errorMessage.length() > 0) {
			fail(errorMessage.toString());
		}
	}

	@Test
	public void testProductHibernateMapping() throws ParserConfigurationException, SAXException, IOException {
		List<String> mappingNames = getMappingNames("/mapping/hibernate/Product.hbm.xml");
				
		List<String> fieldsWithoutCorrespondingMapping = new ArrayList<>();
		
		List<Field> declaredFields = getFieldsWithJsonPropertyAnnotationOnly(Product.class.getDeclaredFields());
		for (Field field : declaredFields) {
			String fieldName = field.getName();
			if(mappingNames.contains(fieldName)) {
				mappingNames.remove(fieldName);
			}else{
				fieldsWithoutCorrespondingMapping.add(fieldName);
			}
		}
		
		//manually remove field for productProgress object
		fieldsWithoutCorrespondingMapping.remove("productProgress");
		
		String errorMessage = buildErrorMessage(mappingNames, fieldsWithoutCorrespondingMapping);
		if(errorMessage.length() > 0) {
			fail(errorMessage.toString());
		}
	}
	
	/*
	 * There might be other fields introduced by some tools, just ignore them
	 */
	private List<Field> getFieldsWithJsonPropertyAnnotationOnly(Field[] classFields) {
		List<Field> filteredFields = new ArrayList<>();
		for (Field field : classFields) {
			if (field.isAnnotationPresent(org.codehaus.jackson.annotate.JsonProperty.class)) {
				filteredFields.add(field);
			}
		}
		return filteredFields;
	}

	private List<String> getMappingNames(String xmlDocumentPath) throws ParserConfigurationException, SAXException, IOException {
		URL resource = this.getClass().getResource(xmlDocumentPath);
		String urlDecodedPath = URLDecoder.decode(resource.getFile(), "UTF-8");
		File resourceFile = new File(urlDecodedPath);
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(resourceFile);
		doc.getDocumentElement().normalize();

		List<String> mappingNames = new ArrayList<>();
		mappingNames.addAll(getNamesFromMappingElements(doc));

		return mappingNames;
	}

	private String buildErrorMessage(List<String> mappingNames, List<String> fieldsWithoutCorrespondingMapping) {
		StringBuilder errorMessage = new StringBuilder();
		if(mappingNames.size() > 0) {
			errorMessage.append("mapping entries without corresponding fields: ");
			errorMessage.append(mappingNames);
			errorMessage.append("\n");
		}
		if(fieldsWithoutCorrespondingMapping.size() > 0) {
			errorMessage.append("fields without corresponding mapping entries: ");
			errorMessage.append(fieldsWithoutCorrespondingMapping);
		}
		return errorMessage.toString();
	}

	public List<String> getNamesFromMappingElements(Document doc) {
		List<String> names = new ArrayList<>();
		NodeList nList = doc.getElementsByTagName("class");
		Element item = (Element) nList.item(0);
		NodeList childNodes = item.getChildNodes();
		
		List<String> nodeNamesToRetrieveName = new ArrayList<>();
		nodeNamesToRetrieveName.add("property");
		nodeNamesToRetrieveName.add("id");
		nodeNamesToRetrieveName.add("list");
		
		for (int temp = 0; temp < childNodes.getLength(); temp++) {
			Node node = childNodes.item(temp);
			
			if(node.getNodeType() == Node.ELEMENT_NODE && nodeNamesToRetrieveName.contains(node.getNodeName())) {
				Element element = (Element) node;
				String nameAttribute = element.getAttribute("name");
				names.add(nameAttribute);
			}
		}
		
		return names;
	}
}
