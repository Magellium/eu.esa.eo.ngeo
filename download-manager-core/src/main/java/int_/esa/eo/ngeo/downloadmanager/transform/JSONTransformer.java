package int_.esa.eo.ngeo.downloadmanager.transform;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JSONTransformer {
	private static JSONTransformer jsonTransformerSingleton = new JSONTransformer();

	private JSONTransformer() {
	}

	public static JSONTransformer getInstance() {
		return jsonTransformerSingleton;
	}

	public <T> T deserialize(InputStream in, Class<T> resultType)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(in, resultType);
	}
}
