<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<script>
	var messages = new Array();
	messages['error.unable_to_add_manual_download'] = "<spring:message code='error.unable_to_add_manual_download' />";
	messages['error.download_failed'] = "<spring:message code='error.download_failed' />";
	messages['error.product_download'] = "<spring:message code='error.product_download' />";
	messages['error.sending_product_download_command'] = "<spring:message code='error.sending_product_download_command' />";
	messages['error.retrieve_product_status'] = "<spring:message code='error.retrieve_product_status' />";
	messages['error.retrieve_dar_status'] = "<spring:message code='error.retrieve_dar_status' />";
	messages['error.clear_activity_history'] = "<spring:message code='error.clear_activity_history' />";
	messages['error.heading.communication_lost'] = "<spring:message code='error.heading.communication_lost' />";
	messages['error.message.communication_lost'] = "<spring:message code='error.message.communication_lost' />";
	messages['product_table.heading.product_access_url'] = "<spring:message code='product_table.heading.product_access_url' />";
	messages['product_table.heading.total_size'] = "<spring:message code='product_table.heading.total_size' />";
	messages['product_table.heading.downloaded_size'] = "<spring:message code='product_table.heading.downloaded_size' />";
	messages['product_table.heading.progress'] = "<spring:message code='product_table.heading.progress' />";
	messages['product_table.heading.product_status'] = "<spring:message code='product_table.heading.product_status' />";
	messages['product_table.heading.message'] = "<spring:message code='product_table.heading.message' />";
	messages['product_table.heading.actions'] = "<spring:message code='product_table.heading.actions' />";
	messages['label.unknown'] = "<spring:message code='label.unknown' />";
	messages['dar_status.IN_PROGRESS'] = "<spring:message code='dar_status.IN_PROGRESS' />";
	messages['dar_status.COMPLETED'] = "<spring:message code='dar_status.COMPLETED' />";
	messages['dar_status.PAUSED'] = "<spring:message code='dar_status.PAUSED' />";
	messages['dar_status.CANCELLED'] = "<spring:message code='dar_status.CANCELLED' />";
	messages['download_status.NOT_STARTED'] = "<spring:message code='download_status.NOT_STARTED' />";
	messages['download_status.IDLE'] = "<spring:message code='download_status.IDLE' />";
	messages['download_status.RUNNING'] = "<spring:message code='download_status.RUNNING' />";
	messages['download_status.PAUSED'] = "<spring:message code='download_status.PAUSED' />";
	messages['download_status.CANCELLED'] = "<spring:message code='download_status.CANCELLED' />";
	messages['download_status.IN_ERROR'] = "<spring:message code='download_status.IN_ERROR' />";
	messages['download_status.COMPLETED'] = "<spring:message code='download_status.COMPLETED' />";

</script>
