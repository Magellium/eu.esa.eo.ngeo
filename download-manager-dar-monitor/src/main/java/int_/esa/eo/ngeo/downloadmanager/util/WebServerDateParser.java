package int_.esa.eo.ngeo.downloadmanager.util;

import int_.esa.eo.ngeo.downloadmanager.exception.NonRecoverableException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class WebServerDateParser {
    private static final String NGEO_IICD_D_WS_DATE_REQUEST_FORMAT = "yyyy-MM-dd'T'HH:mm:ss zzz";

    public GregorianCalendar convertDateTimeAsStringToGregorianCalendar(String downloadManagerSetTimeAsString) {
        TimeZone utc = TimeZone.getTimeZone("UTC");

        DateFormat df = new SimpleDateFormat(NGEO_IICD_D_WS_DATE_REQUEST_FORMAT);
        df.setTimeZone(utc);
        Date finalTime = null;

        try {
            finalTime = df.parse(downloadManagerSetTimeAsString);            
        } catch (java.text.ParseException e) {
            throw new NonRecoverableException(e);
        }

        GregorianCalendar calendar = new GregorianCalendar(utc);
        calendar.setTime(finalTime);

        return calendar;
    }

    public String convertDateToString(Date date) {
        if(date == null) return "";
        DateFormat df = new SimpleDateFormat(NGEO_IICD_D_WS_DATE_REQUEST_FORMAT);
        return df.format(date);
    }
}
