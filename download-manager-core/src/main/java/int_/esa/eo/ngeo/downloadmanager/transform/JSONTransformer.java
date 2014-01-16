package int_.esa.eo.ngeo.downloadmanager.transform;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.map.ObjectMapper;

public class JSONTransformer {
    public <T> T deserialize(InputStream in, Class<T> resultType)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, resultType);
    }
}
