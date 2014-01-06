package int_.esa.eo.ngeo.downloadmanager.transform;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import int_.esa.eo.ngeo.downloadmanager.exception.ParseException;
import int_.esa.eo.ngeo.downloadmanager.exception.SchemaNotFoundException;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DMRegistrationMgmntResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.DataAccessMonitoringResp;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLRequ;
import int_.esa.eo.ngeo.iicd_d_ws._1.MonitoringURLResp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class XMLWithSchemaTransformerTest {
    XMLWithSchemaTransformer xmlWithSchemaTransformer;
    SchemaRepository schemaRepository;

    @Before
    public void setup() {
        Map<Class<?>, String> schemaMap = new HashMap<>();
        schemaMap.put(DataAccessMonitoringRequ.class, "schemas/IICD-D-WS/IF-ngEO-DataAccessMonitoring-Requ.xsd");
        schemaMap.put(DataAccessMonitoringResp.class, "schemas/IICD-D-WS/IF-ngEO-DataAccessMonitoring-Resp.xsd");
        schemaMap.put(DMRegistrationMgmntRequ.class, "schemas/IICD-D-WS/IF-ngEO-DMRegistrationMgmnt_Requ.xsd");
        schemaMap.put(DMRegistrationMgmntResp.class, "schemas/IICD-D-WS/IF-ngEO-DMRegistrationMgmnt_Resp.xsd");
        schemaMap.put(MonitoringURLRequ.class, "schemas/IICD-D-WS/IF-ngEO-MonitoringURL-Requ.xsd");
        schemaMap.put(MonitoringURLResp.class, "schemas/IICD-D-WS/IF-ngEO-MonitoringURL-Resp.xsd");
        schemaMap.put(Error.class, "schemas/IICD-D-WS/IF-ngEO-D-WS_common.xsd");

        schemaRepository = new SchemaRepository(schemaMap);
        xmlWithSchemaTransformer = spy(new XMLWithSchemaTransformer(schemaRepository));
    }
    
    @Test
    public void deserialiseAndInferTest() throws ParseException, SchemaNotFoundException {
        String response = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ngeo:MonitoringURL-Resp xmlns:ngeo=\"http://ngeo.eo.esa.int/iicd-d-ws/1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://ngeo.eo.esa.int/iicd-d-ws/1.0 IF-ngEO-MonitoringURL-Resp_0.3.xsd\"><ngeo:RefreshPeriod>20</ngeo:RefreshPeriod><ngeo:MonitoringURLList><ngeo:MonitoringURL>http://localhost:8080/download-manager-mock-web-server/monitoringservice/000321</ngeo:MonitoringURL></ngeo:MonitoringURLList></ngeo:MonitoringURL-Resp>";
        InputStream in = new ByteArrayInputStream(response.getBytes());
        MonitoringURLResp parsedResponse = xmlWithSchemaTransformer.deserializeAndInferSchema(in, MonitoringURLResp.class);
        assertEquals(BigInteger.valueOf(20L), parsedResponse.getRefreshPeriod());
        assertEquals("http://localhost:8080/download-manager-mock-web-server/monitoringservice/000321", parsedResponse.getMonitoringURLList().getMonitoringURLs().get(0));
    }
}
