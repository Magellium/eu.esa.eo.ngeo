<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div id="actions">
	<div style="float:left;">
	<form name="downloadForm" id="downloadForm" action="">
		<spring:message code="label.add_manual_download" var="label_add_manual_download" />
		<label for="downloadURL"><spring:message code="label.manual_download_url" />&nbsp;</label><input type="text" size="30" name="downloadUrl" id="downloadUrl" autofocus="autofocus">&nbsp;<input name="submit" type="submit" value="${label_add_manual_download}" />
	</form>
	</div>
	<div style="float:right;">
		<spring:message code="label.advanced_configuration" var="label_advanced_configuration" />
		<spring:message code="label.clear_activity_history" var="label_clear_activity_history" />
		<input type="button" value="${label_advanced_configuration}" class="headerButton" onclick="document.location.href='advancedconfig'"/>&nbsp;
		<input type="button" value="${label_clear_activity_history}" class="headerButton" onclick="DownloadMonitor.clearActivityHistory($('#downloadStatusTable'));" />
	</div>
	<br /><br />
</div>
<hr />
<table id="downloadStatusTable">
	<thead>
		<tr>
			<th></th>
			<th><spring:message code="dar_table.heading.monitoring_url" /></th>
			<th><spring:message code="dar_table.heading.monitoring_status" /></th>
		</tr>
	</thead>
	<tbody>
	</tbody>
</table>
<p><br /><br /></p>
<div id="message" class="errorConsole">
	<p>AJAX Error console: <br /></p>
</div>
<script>
	$(document).ready(function() {
		DownloadMonitor.initialiseDownloadForm($("#downloadForm"),$("#downloadStatusTable"));
		DownloadMonitor.initialiseDownloadStatusTable($("#downloadStatusTable"));
	});
</script>