<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<h1><spring:message code="advanced_config.heading" /></h1>
<form:form commandName="ADVANCEDCONFIGSETTINGS" name="settingsForm" id="settingsForm" >
	<p style="text-align:center;"><form:errors path="*" cssStyle="color:red;"/></p>
	<div style="width:550px;margin-left:auto;margin-right:auto;">
		<table>
			<tr><td><spring:message code="label.sso_username" /> </td><td><form:input path="ssoUsername" /></td></tr>
			<tr><td><spring:message code="label.sso_password" /> </td><td><form:password path="ssoPassword" showPassword="true" /></td></tr>
			<tr><td><spring:message code="label.dm_instance_name" /> </td><td><form:input path="dmFriendlyName" size="40" /></td></tr>
			<tr><td><spring:message code="label.download_directory" /> </td><td><form:input path="baseDownloadFolder" size="40" /></td></tr>
			<tr><td><spring:message code="label.web_interface_port_no" /> </td><td><form:input path="webInterfacePortNo" size="5"/></td></tr>
			<tr><td><spring:message code="label.parallel_download_threads" /> </td><td><form:input path="noOfParallelProductDownloadThreads" /></td></tr>
			<tr><td><spring:message code="label.proxy.url" /> </td><td><form:input path="webProxyUrl" /></td></tr>
			<tr><td><spring:message code="label.proxy.port" /> </td><td><form:input path="webProxyPort" /></td></tr>
			<tr><td><spring:message code="label.proxy.username" /> </td><td><form:input path="webProxyUsername" /></td></tr>
			<tr><td><spring:message code="label.proxy.password" /> </td><td><form:input path="webProxyPassword" showPassword="true" /></td></tr>
			<tr><td><spring:message code="label.product_download_complete_command" /> </td><td><form:input path="productDownloadCompleteCommand" /></td></tr>
			<tr><td><spring:message code="label.web_interface_username" /> </td><td><form:input path="webInterfaceUsername" /></td></tr>
			<tr><td><spring:message code="label.web_interface_password" /> </td><td><form:input path="webInterfacePassword" showPassword="true"/></td></tr>
			<tr><td><spring:message code="label.web_interface_remote_access_enabled" /> </td><td><form:checkbox path="webInterfaceRemoteAccessEnabled" /></td></tr>
			<tr>
				<td colspan="2" style="text-align:center;">
					<br />
					<spring:message code="label.back" var="label_back" />
					<spring:message code="label.save_config" var="label_save_config" />
					<input type="button" value="${label_back}" class="headerButton" onclick="document.location.href=''"/>&nbsp;
					<input type="submit" value="${label_save_config}" />
				</td>
			</tr>
		</table>	
	</div>
	<p><b><spring:message code="label.version" /></b> <spring:message code="version"/></p>
</form:form>
