<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<h1><spring:message code="advanced_config.heading" /></h1>
<form:form commandName="ADVANCEDCONFIGSETTINGS" name="settingsForm" id="settingsForm" >
	<p style="text-align:center;"><form:errors path="*" cssStyle="color:red;"/></p>
	<div id="tabs">
		<ul>
			<li><a href="${pathForAnchors}#tabs-1">General</a></li>
			<li><a href="${pathForAnchors}#tabs-2">Web Proxy</a></li>
           <li><a href="${pathForAnchors}#tabs-3">Email</a></li>
<!-- 			<li><a href="advancedconfig#tabs-4">Web Interface</a></li> -->
		</ul>
		<div id="tabs-1">
			<table>
				<tr><td><spring:message code="label.sso_username" /> </td><td><form:input path="ssoUsername" /></td></tr>
				<tr><td><spring:message code="label.sso_password" /> </td><td><form:password path="ssoPassword" showPassword="true" /></td></tr>
				<tr><td><spring:message code="label.dm_instance_name" /> </td><td><form:input path="dmFriendlyName" size="40" /></td></tr>
				<tr><td><spring:message code="label.download_directory" /> </td><td><form:input path="baseDownloadFolder" size="40" /></td></tr>
				<tr><td><spring:message code="label.web_interface_port_no" /> </td><td><form:input path="webInterfacePortNo" size="5"/></td></tr>
				<tr><td><spring:message code="label.parallel_download_threads" /> </td><td><form:input path="noOfParallelProductDownloadThreads" /></td></tr>
				<tr><td><spring:message code="label.product_download_complete_command" /> </td><td><form:input path="productDownloadCompleteCommand" /></td></tr>
			</table>
		</div>
		<div id="tabs-2">
			<table>
				<tr><td><spring:message code="label.proxy.host" /> </td><td><form:input path="webProxyHost" /></td></tr>
				<tr><td><spring:message code="label.proxy.port" /> </td><td><form:input path="webProxyPort" /></td></tr>
				<tr><td><spring:message code="label.proxy.username" /> </td><td><form:input path="webProxyUsername" /></td></tr>
				<tr><td><spring:message code="label.proxy.password" /> </td><td><form:password path="webProxyPassword" showPassword="true" /></td></tr>
			</table>
		</div>
        <div id="tabs-3">
            <table>
                <tr><td><spring:message code="label.email.recipients" /> </td><td><form:input path="emailRecipients" size="40" /></td></tr>
                <tr><td><spring:message code="label.email.send_level" /> </td><td><form:select path="emailSendLevel"><form:options /></form:select></td></tr>
                <tr><td><spring:message code="label.email.smtp.server" /> </td><td><form:input path="smtpServer" size="30" /></td></tr>
                <tr><td><spring:message code="label.email.smtp.port" /> </td><td><form:input path="smtpPort" size="5" /></td></tr>
                <tr><td><spring:message code="label.email.smtp.username" /> </td><td><form:input path="smtpUsername" size="40" /></td></tr>
                <tr><td><spring:message code="label.email.smtp.password" /> </td><td><form:password path="smtpPassword" showPassword="true" /></td></tr>
                <tr><td><spring:message code="label.email.smtp.security" /> </td><td><form:select path="smtpSecurity"><form:options /></form:select></td></tr>
            </table>
        </div>
<!-- 		<div id="tabs-4"> -->
<!-- 			<table> -->
<!-- 		<tr> -->
<%-- 			<td><spring:message code="label.web_interface_username" /></td> --%>
<!-- 			<td> -->
				<form:hidden path="webInterfaceUsername" />
<!-- 			</td> -->
<!-- 		</tr> -->
<!-- 		<tr> -->
<%-- 			<td><spring:message code="label.web_interface_password" /></td> --%>
<!-- 			<td> -->
				<form:hidden path="webInterfacePassword" />
<!-- 			</td> -->
<!-- 		</tr> -->
<!-- 		<tr> -->
<%-- 			<td><spring:message --%>
<%-- 					code="label.web_interface_remote_access_enabled" /></td> --%>
<!-- 			<td> -->
				<form:hidden path="webInterfaceRemoteAccessEnabled" />
<!-- 			</td> -->
<!-- 		</tr> -->
		<!-- 			</table> -->
<!-- 		</div> -->
<!-- 		<div id="tabs-4"> -->
<!-- 			TODO -->
<!-- 		</div> -->
	</div>
	<div id="configButtons">
		<spring:message code="label.back" var="label_back" />
		<spring:message code="label.save_config" var="label_save_config" />
		<input type="button" value="${label_back}" class="headerButton" onclick="document.location.href=''"/>&nbsp;
		<input type="submit" value="${label_save_config}" />
	</div>
	<p><b><spring:message code="label.version" /></b> <spring:message code="version"/></p>
</form:form>
<script>
	$(document).ready(function() {
		$("#tabs").tabs();
	});
</script>
