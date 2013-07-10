package int_.esa.eo.ngeo.dmtu.log;

import org.apache.log4j.PatternLayout;

public class ProductTerminationLogLayout extends PatternLayout {
	@Override
	public String getHeader() {     
        StringBuilder header = new StringBuilder();
        header.append("Status, ");
        header.append("Product URL, ");
        header.append("Data Access Request URL, ");
        header.append("Bytes downloaded, ");
        header.append("Start of first download request, ");
        header.append("Start of actual download, ");
        header.append("Stop of download, ");
        header.append("Path of product, ");
        header.append("Reason for failure");
        header.append(System.getProperty("line.separator"));
		return header.toString();
    }
}
