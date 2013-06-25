package int_.esa.eo.ngeo.downloadManager.plugin.utils;

import int_.esa.eo.ngeo.schema.ngeobadrequestresponse.BadRequestResponse;
import int_.esa.eo.ngeo.schema.ngeomissingproductresponse.MissingProductResponse;
import int_.esa.eo.ngeo.schema.ngeoproductdownloadresponse.ProductDownloadResponse;

import org.metalinker.MetalinkType;

public enum ProductResponseType {
	METALINK_3_0(MetalinkType.class, "schemas/metalink/3.0/metalink.xsd"),
	BAD_REQUEST(BadRequestResponse.class, "schemas/DAGICD/ngEOBadRequestResponse.xsd"),
	MISSING_PRODUCT(MissingProductResponse.class, "schemas/DAGICD/ngEOMissingProductResponse.xsd"),
	PRODUCT_ACCEPTED(ProductDownloadResponse.class, "schemas/DAGICD/ngEOProductDownloadResponse.xsd");
	
	private Class<?> classType;
	private String schemaPath;
	
	private <T> ProductResponseType(Class<T> classType, String schemaPath) {
		this.classType = classType;
		this.schemaPath = schemaPath;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getClassType() {
		return (Class<T>) classType;
	}

	public String getSchemaPath() {
		return schemaPath;
	}
}
