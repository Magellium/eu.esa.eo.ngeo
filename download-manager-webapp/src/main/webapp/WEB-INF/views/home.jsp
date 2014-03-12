<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div id="actions">
	<div style="float:left;">
		<form name="manualDownloadForm" id="manualDownloadForm" action="">
			<spring:message code="label.add_product" var="label_add_product" />
			<spring:message code="label.add_dar" var="label_add_dar" />
			<ul>
				<li>
					<label for="productDownloadUrl" class="downloadLabel"><spring:message code="label.product_url" />&nbsp;</label>
					<input type="text" size="50" name="productDownloadUrl" id="productDownloadUrl" autofocus="autofocus">&nbsp;<input name="addProductDownload" id="addProductDownload" type="button" value="${label_add_product}" />
				</li>
				<li>
					<label for="darUrl" class="downloadLabel" style="text-align:right;"><spring:message code="label.dar_url" />&nbsp;</label>
					<input type="text" size="50" name="darUrl" id="darUrl">&nbsp;<input name="addDAR" id="addDAR" type="button" value="${label_add_dar}" />
				</li>
			</ul>
		</form>
	</div>
	<div style="float:right;">
		<spring:message code="label.clear_activity_history" var="label_clear_activity_history" />
		<spring:message code="label.commands" var="label_commands" />
		<spring:message code="label.commands.stop" var="label_commands_stop" />
		<spring:message code="label.commands.stop_monitoring" var="label_commands_stop_monitoring" />
		<spring:message code="label.commands.stop_monitoring_now" var="label_commands_stop_monitoring_now" />
		<spring:message code="label.commands.stop_monitoring_all" var="label_commands_stop_monitoring_all" />
		<spring:message code="tooltip.commands.stop_monitoring" var="tooltip_commands_stop_monitoring" />
		<spring:message code="tooltip.commands.stop_monitoring_now" var="tooltip_commands_stop_monitoring_now" />
		<spring:message code="tooltip.commands.stop_monitoring_all" var="tooltip_commands_stop_monitoring_all" />

		<ul id="menu">
			<li><a href="#">${label_commands}</a>
				<ul>
					<li><a href="#" class="clearActivityHistory">${label_clear_activity_history}</a></li>
					<li><a href="#">${label_commands_stop}</a>
						<ul>
							<li><a href="#" class="stopMonitoring" title="${tooltip_commands_stop_monitoring}">${label_commands_stop_monitoring}</a></li>
							<li><a href="#" class="stopMonitoringNow" title="${tooltip_commands_stop_monitoring_now}">${label_commands_stop_monitoring_now}</a></li>
							<li><a href="#" class="stopAll" title="${tooltip_commands_stop_monitoring_all}">${label_commands_stop_monitoring_all}</a></li>
						</ul>
					</li>
				</ul>
			</li>
		</ul>
	</div>
	&nbsp;
	<div style="float:right;">
		<spring:message code="label.advanced_configuration" var="label_advanced_configuration" />

		<input type="button" value="${label_advanced_configuration}" class="headerButton" onclick="document.location.href='config/advanced'"/>
	</div>
	<br /><br />
</div>
<div class="clearboth"></div>
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
<script src="resources/js/downloadMonitor.js"></script>
<script src="resources/js/darStatusTable.js"></script>
<script src="resources/js/productStatusTable.js"></script>
<script src="resources/js/messageDisplay.js"></script>
<script>
	$(document).ready(function() {
        $("#menu").menu( { icons: { submenu: "ui-icon-carat-1-s" }, position: { my: "right top", at: "left center" } });
        $(".clearActivityHistory").click(function() {
            DownloadMonitor.clearActivityHistory($('#downloadStatusTable'));
        });
        $(".stopMonitoring").click(function() {
            DownloadMonitor.stopDownloads("monitoring");
        });
        $(".stopMonitoringNow").click(function() {
            DownloadMonitor.stopDownloads("monitoring_now");
        });
        $(".stopAll").click(function() {
            DownloadMonitor.stopDownloads("all");
        });
        DownloadMonitor.initialiseDownloadForm($("#manualDownloadForm"),$("#downloadStatusTable"));

        DarStatusTable.initialiseDarStatusTable($("#downloadStatusTable"));
	});
</script>