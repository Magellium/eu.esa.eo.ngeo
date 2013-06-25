package int_.esa.eo.ngeo.dmtu.jaxb;

import int_.esa.eo.ngeo.dmtu.exception.NonRecoverableException;

import java.util.HashMap;
import java.util.Map;

public class JaxbSchemaRepository {
	private Map<String,String> schemaMap = new HashMap<String,String>();

	public Map<String,String> getSchemaMap() {
		return schemaMap;
	}

	public void setSchemaMap(Map<String,String> schemaMap) {
		this.schemaMap = schemaMap;
	}
	
	public String getSchema(String className) {
		String schemaLocation = schemaMap.get(className);
		if(schemaLocation == null) {
			throw new NonRecoverableException(String.format("Unable to locate XML schema for class %s", className));
		}
		return schemaLocation;
	}
}
