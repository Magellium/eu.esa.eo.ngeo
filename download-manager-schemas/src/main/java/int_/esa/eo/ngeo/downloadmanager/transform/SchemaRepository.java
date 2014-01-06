package int_.esa.eo.ngeo.downloadmanager.transform;

import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class SchemaRepository {
    private Map<Class<?>, String> schemaMap = new HashMap<>();

    public SchemaRepository(Map<Class<?>, String> schemaMap) {
        this.schemaMap = schemaMap;
    }

    public <T> String getSchema(Class<T> clazz) throws SchemaNotFoundException {
        String schemaLocation = schemaMap.get(clazz);
        if(schemaLocation == null) {
            throw new SchemaNotFoundException(String.format("Unable to locate XML schema for class %s", clazz.getName()));
        }
        return schemaLocation;
    }
}
