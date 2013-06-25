<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="actions">
	<div style="float:left;">
	<form name="downloadForm" id="downloadForm" action="">
		<label for="downloadURL">Manual download URL:&nbsp;</label><input type="text" size="30" name="downloadUrl" id="downloadUrl" autofocus="autofocus">&nbsp;<input name="submit" type="submit" value="Add Manual Download" />
	</form>
	</div>
	<div style="float:right;">
		<input type="button" value="Advanced Configuration" class="headerButton" onclick="document.location.href='advancedconfig'"/>&nbsp;
		<input type="button" value="Clear Activity History" class="headerButton" onclick="DownloadMonitor.clearActivityHistory($('#downloadStatusTable'));" />
	</div>
	<br /><br />
</div>
<hr />
<table id="downloadStatusTable">
	<thead>
		<tr>
			<th></th>
<!-- 			<th>Data Access Request UUID</th> -->
			<th>Monitoring URL</th>
			<th>Monitoring Status</th>
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