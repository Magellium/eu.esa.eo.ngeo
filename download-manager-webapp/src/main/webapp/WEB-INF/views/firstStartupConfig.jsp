<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<h1><spring:message code="first_startup_config.heading" /></h1>
<form:form commandName="FIRSTSTARTUPCONFIGSETTINGS" name="settingsForm" id="settingsForm" >
	<div class="pageDescription">
		<form:errors path="*" cssStyle="color:red;"/><br />
		<spring:message code="first_startup_config.description" /><br /><br />
	</div>
	<div id="tabs">
		<ul>
			<li><a href="${pathForAnchors}#tabs-1">General</a></li>
            <li><a href="${pathForAnchors}#tabs-2">Web Proxy</a></li>
            <li><a href="${pathForAnchors}#tabs-3">Email</a></li>
		</ul>
		<div id="tabs-1">
			<table>
				<tr><td><spring:message code="label.sso_username" /> </td><td><form:input path="ssoUsername" /></td></tr>
				<tr><td><spring:message code="label.sso_password" /> </td><td><form:password path="ssoPassword" showPassword="true" /></td></tr>
				<tr><td><spring:message code="label.dm_instance_name" /> </td><td><form:input path="dmFriendlyName" size="40" /></td></tr>
				<tr><td><spring:message code="label.download_directory" /> </td><td><form:input path="baseDownloadFolder" size="40" /></td></tr>
<!--  				<tr><td><spring:message code="label.web_interface_port_no" /> </td><td><form:input path="webInterfacePortNo" size="5"/></td></tr> -->
			</table>
            <form:hidden path="webInterfacePortNo" size="5"/>
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
	</div>
	<div id="configButtons">
		<spring:message code="label.save_config_and_register" var="label_save_config_and_register" />
		<input type="submit" value="${label_save_config_and_register}" />
	</div>

	<p><b><spring:message code="label.version" /></b> <spring:message code="version"/></p>
</form:form>
<script>
	$(document).ready(function() {
		$("#tabs").tabs();
	});
</script>
