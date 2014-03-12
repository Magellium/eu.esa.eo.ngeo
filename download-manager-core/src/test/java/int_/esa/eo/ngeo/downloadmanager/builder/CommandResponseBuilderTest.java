package int_.esa.eo.ngeo.downloadmanager.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponse;
import int_.esa.eo.ngeo.downloadmanager.rest.CommandResponseWithDarDetails;

import org.junit.Before;
import org.junit.Test;

public class CommandResponseBuilderTest {
    private CommandResponseBuilder commandResponseBuilder;
    
    @Before
    public void setup() {
        commandResponseBuilder = new CommandResponseBuilder();
    }
    
    @Test
    public void buildCommandResponseSuccessTest() {
        CommandResponse commandResponse = commandResponseBuilder.buildCommandResponse(true, "");
        assertTrue(commandResponse.isSuccess());
        assertNull(commandResponse.getErrorMessage());
        assertNull(commandResponse.getErrorType());
    }

    @Test
    public void buildCommandResponseFailureNoErrorTypeTest() {
        CommandResponse commandResponse = commandResponseBuilder.buildCommandResponse(false, "Error");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
        assertNull(commandResponse.getErrorType());
    }
    
    @Test
    public void buildCommandResponseFailureWithErrorTypeTest() {
        CommandResponse commandResponse = commandResponseBuilder.buildCommandResponse(false, "Error", "IllegalArgumentException");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
        assertEquals("IllegalArgumentException", commandResponse.getErrorType());
    }
    
    @Test
    public void buildCommandResponseWithDarSuccessTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarUuid("abc1234", "");
        assertTrue(commandResponse.isSuccess());
        assertEquals("abc1234", commandResponse.getDarUuid());
        assertNull(commandResponse.getProductUuid());
        assertNull(commandResponse.getErrorMessage());
        assertNull(commandResponse.getErrorType());
    }

    @Test
    public void buildCommandResponseWithDarFailureNoErrorTypeTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarUuid("", "Error");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
        assertNull(commandResponse.getErrorType());
    }

    @Test
    public void buildCommandResponseWithDarFailureAndErrorTypeTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarUuid("", "Error", "IllegalArgumentException");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
        assertEquals("IllegalArgumentException", commandResponse.getErrorType());
    }

    @Test
    public void buildCommandResponseWithDarAndProductSuccessTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarAndProductUuid("darABC", "product1234", "");
        assertTrue(commandResponse.isSuccess());
        assertEquals("darABC", commandResponse.getDarUuid());
        assertEquals("product1234", commandResponse.getProductUuid());
        assertNull(commandResponse.getErrorMessage());
        assertNull(commandResponse.getErrorType());
    }

    @Test
    public void buildCommandResponseWithDarAndProductFailureNoErrorMessageOrTypeTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarAndProductUuid("", "", "");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Unable to provide product UUID for added product.", commandResponse.getErrorMessage());
    }

    @Test
    public void buildCommandResponseWithDarAndProductFailureNoErrorTypeTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarAndProductUuid("", "", "Error");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
    }

    @Test
    public void buildCommandResponseWithDarAndProductFailureAndErrorTypeTest() {
        CommandResponseWithDarDetails commandResponse = commandResponseBuilder.buildCommandResponseWithDarAndProductUuid("", "", "Error", "IllegalArgumentException");
        assertFalse(commandResponse.isSuccess());
        assertEquals("Error", commandResponse.getErrorMessage());
        assertEquals("IllegalArgumentException", commandResponse.getErrorType());
    }
}
