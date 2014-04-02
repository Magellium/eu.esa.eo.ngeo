package int_.esa.eo.ngeo.downloadmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class WebServerDateParserTest {
    private WebServerDateParser webServerDateParser;
    
    @Before
    public void setup() {
        webServerDateParser = new WebServerDateParser();
    }
    
    @Test
    public void convertDateTimeAsStringToGregorianCalendarTest() {
        assertEquals(1396011580000L, webServerDateParser.convertDateTimeAsStringToGregorianCalendar("2014-03-28T12:59:40 GMT").getTimeInMillis());
    }

    @Test
    public void convertDateTimeAsStringToGregorianCalendarInvalidDateTest() {
        try {
            webServerDateParser.convertDateTimeAsStringToGregorianCalendar("invalid date");
        }catch(NonRecoverableException ex) {
            assertNotNull(ex.getCause());
            assertEquals("Unparseable date: \"invalid date\"", ex.getCause().getLocalizedMessage());
        }
    }

    @Test
    public void convertDateToStringTest() {
        assertEquals("2014-03-28T12:59:40 GMT" ,webServerDateParser.convertDateToString(new Date(1396011580000L)));
        assertEquals("" ,webServerDateParser.convertDateToString(null));
    }
}
